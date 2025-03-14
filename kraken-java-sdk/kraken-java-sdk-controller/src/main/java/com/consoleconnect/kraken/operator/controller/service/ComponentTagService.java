package com.consoleconnect.kraken.operator.controller.service;

import static com.consoleconnect.kraken.operator.controller.service.ApiComponentService.ASSET_NOT_FOUND;
import static com.consoleconnect.kraken.operator.core.toolkit.AssetsConstants.FIELD_CREATE_AT_ORIGINAL;
import static com.consoleconnect.kraken.operator.core.toolkit.LabelConstants.*;

import com.consoleconnect.kraken.operator.controller.dto.*;
import com.consoleconnect.kraken.operator.controller.model.ComponentTag;
import com.consoleconnect.kraken.operator.controller.model.ComponentTagFacet;
import com.consoleconnect.kraken.operator.controller.model.DeploymentFacet;
import com.consoleconnect.kraken.operator.controller.tools.VersionHelper;
import com.consoleconnect.kraken.operator.core.dto.ApiUseCaseDto;
import com.consoleconnect.kraken.operator.core.dto.AssetLinkDto;
import com.consoleconnect.kraken.operator.core.dto.UnifiedAssetDto;
import com.consoleconnect.kraken.operator.core.entity.UnifiedAssetEntity;
import com.consoleconnect.kraken.operator.core.enums.AssetKindEnum;
import com.consoleconnect.kraken.operator.core.enums.DeployStatusEnum;
import com.consoleconnect.kraken.operator.core.enums.MappingStatusEnum;
import com.consoleconnect.kraken.operator.core.enums.ReleaseKindEnum;
import com.consoleconnect.kraken.operator.core.event.IngestionDataResult;
import com.consoleconnect.kraken.operator.core.exception.KrakenException;
import com.consoleconnect.kraken.operator.core.model.*;
import com.consoleconnect.kraken.operator.core.repo.UnifiedAssetRepository;
import com.consoleconnect.kraken.operator.core.service.UnifiedAssetService;
import com.consoleconnect.kraken.operator.core.toolkit.*;
import com.consoleconnect.kraken.operator.core.toolkit.LabelConstants;
import com.fasterxml.jackson.core.type.TypeReference;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@AllArgsConstructor
@Service
public class ComponentTagService implements TargetMappingChecker, LatestDeploymentCalculator {
  public static final String TAG = ".tag.";
  @Getter private final UnifiedAssetService unifiedAssetService;
  private final UnifiedAssetRepository unifiedAssetRepository;
  private final ApiComponentService apiComponentService;
  private final AppProperty appProperty;

  @Transactional
  public IngestionDataResult createTag(
      String componentId, CreateTagRequest request, String createdBy) {
    // create a snapshot of the component
    // current component
    long currentTime = System.currentTimeMillis();
    UnifiedAssetDto componentAsset = unifiedAssetService.findOne(componentId);
    Optional<UnifiedAssetEntity> latestTagOpt =
        unifiedAssetService.findLatest(componentAsset.getId(), AssetKindEnum.COMPONENT_API_TAG);
    List<AssetLinkDto> links =
        unifiedAssetService.findAssetLinks(componentId, null, 0, Integer.MAX_VALUE).getData();
    // check whether to allow creating
    String tagVersion = Constants.INIT_VERSION;
    if (latestTagOpt.isPresent()) {
      UnifiedAssetEntity unifiedAssetEntity = latestTagOpt.get();
      tagVersion = VersionHelper.generateVersion(unifiedAssetEntity);
      Optional<UnifiedAssetDto> any =
          links.stream()
              .map(AssetLinkDto::getTargetAsset)
              .filter(Objects::nonNull)
              .filter(
                  target ->
                      DateTime.of(target.getCreatedAt()).isAfter(unifiedAssetEntity.getCreatedAt())
                          || target.getUpdatedAt() != null
                              && DateTime.of(target.getUpdatedAt())
                                  .isAfter(unifiedAssetEntity.getCreatedAt()))
              .findAny();
      if (any.isEmpty())
        throw KrakenException.badRequest(
            "new version creation failed due to the mapping has no change since last version");
    }

    if (request.getName() == null) {
      request.setName("Version." + DateTime.formatCompact(ZonedDateTime.now()));
    }
    String key = componentAsset.getMetadata().getKey() + TAG + currentTime;
    UnifiedAsset tagAsset =
        UnifiedAsset.of(AssetKindEnum.COMPONENT_API_TAG.getKind(), key, request.getName());
    tagAsset.getMetadata().setDescription(request.getDescription());
    tagAsset.getMetadata().getLabels().put(LabelConstants.LABEL_VERSION_NAME, tagVersion);
    tagAsset
        .getMetadata()
        .getLabels()
        .put(LabelConstants.LABEL_RELEASE_KIND, ReleaseKindEnum.COMPONENT_LEVEL.getKind());

    List<UnifiedAssetDto> childAssets = links.stream().map(AssetLinkDto::getTargetAsset).toList();
    changeParentId(childAssets, componentAsset);
    Map<String, Object> facets = new HashMap<>();
    facets.put(ComponentTagFacet.KEY_COMPONENT, componentAsset);
    facets.put(ComponentTagFacet.KEY_CHILDREN, childAssets);

    tagAsset.setFacets(facets);
    SyncMetadata syncMetadata = new SyncMetadata("", "", DateTime.nowInUTCString(), createdBy);
    return unifiedAssetService.syncAsset(
        componentAsset.getMetadata().getKey(), tagAsset, syncMetadata, true);
  }

  public IngestionDataResult createMappingTag(
      String componentId, String mapperKey, String createdBy) {
    UnifiedAssetEntity mapperEntity =
        unifiedAssetRepository.findOneByKey(mapperKey).orElseThrow(() -> ASSET_NOT_FOUND);
    String newVersion = newVersion(mapperEntity);
    // upgrade version for mapper
    mapperEntity.getLabels().put(LABEL_VERSION_NAME, newVersion);
    mapperEntity.getLabels().put(LABEL_SUB_VERSION_NAME, NumberUtils.INTEGER_ONE.toString());
    unifiedAssetRepository.save(mapperEntity);
    UnifiedAssetDto componentAsset = unifiedAssetService.findOne(componentId);
    List<String> memberKeys =
        apiComponentService
            .findRelatedApiUse(mapperKey)
            .map(ApiUseCaseDto::membersExcludeApiKey)
            .orElse(List.of());
    List<UnifiedAssetDto> childAssets = unifiedAssetService.findByAllKeysIn(memberKeys, true);
    changeParentId(childAssets, componentAsset);
    String key = mapperKey + TAG + System.currentTimeMillis();
    UnifiedAsset tagAsset =
        UnifiedAsset.of(
            AssetKindEnum.COMPONENT_API_TARGET_MAPPER_TAG.getKind(), key, generateVersionName());
    tagAsset.getMetadata().getLabels().put(LabelConstants.LABEL_VERSION_NAME, newVersion);
    tagAsset
        .getMetadata()
        .getLabels()
        .put(
            LABEL_SUB_VERSION_NAME,
            mapperEntity
                .getLabels()
                .getOrDefault(LABEL_SUB_VERSION_NAME, NumberUtils.INTEGER_ONE.toString()));
    tagAsset.getMetadata().getLabels().put(LabelConstants.MAPPER_KEY, mapperKey);

    Map<String, Object> facets = new HashMap<>();
    facets.put(ComponentTagFacet.KEY_COMPONENT, componentAsset);
    facets.put(ComponentTagFacet.KEY_CHILDREN, childAssets);

    tagAsset.setFacets(facets);
    SyncMetadata syncMetadata = new SyncMetadata("", "", DateTime.nowInUTCString(), createdBy);
    // here has the important point: parent key is mapper
    return unifiedAssetService.syncAsset(mapperKey, tagAsset, syncMetadata, true);
  }

  private void changeParentId(List<UnifiedAssetDto> childList, UnifiedAssetDto componentAsset) {
    List<UnifiedAssetDto> finalAssetDtos = new ArrayList<>();
    finalAssetDtos.add(componentAsset);
    finalAssetDtos.addAll(childList);
    List<UUID> parentIds =
        finalAssetDtos.stream()
            .filter(Objects::nonNull)
            .map(UnifiedAssetDto::getParentId)
            .map(UUID::fromString)
            .toList();
    Map<UUID, UnifiedAssetEntity> assetMap =
        unifiedAssetRepository.findAllByIdIn(parentIds).stream()
            .collect(Collectors.toMap(UnifiedAssetEntity::getId, t -> t));
    //  reset the parent in key format not uuid,so that the data plane reconstruct the relationship
    finalAssetDtos.stream()
        .filter(Objects::nonNull)
        .forEach(
            param ->
                Optional.ofNullable(assetMap.get(UUID.fromString(param.getParentId())))
                    .ifPresent(pramEntity -> param.setParentId(pramEntity.getKey())));
  }

  @Transactional
  public void checkMapperModification(String mapperKey) {
    UnifiedAssetEntity mapperEntity =
        unifiedAssetRepository.findOneByKey(mapperKey).orElseThrow(() -> ASSET_NOT_FOUND);
    Optional<UnifiedAssetEntity> latestTagOpt =
        unifiedAssetService.findLatest(
            mapperEntity.getId().toString(), AssetKindEnum.COMPONENT_API_TARGET_MAPPER_TAG);
    if (latestTagOpt.isPresent()
        && latestTagOpt.get().getCreatedAt().compareTo(mapperEntity.getUpdatedAt()) > 0) {
      throw KrakenException.badRequest("No difference with currently running version ");
    }
    UnifiedAssetDto assetDto = UnifiedAssetService.toAsset(mapperEntity, true);
    fillMappingStatus(assetDto, appProperty.getNoRequiredMappingKeys());
    if (MappingStatusEnum.INCOMPLETE.getDesc().equals(assetDto.getMappingStatus())) {
      throw KrakenException.badRequest(
          "deployed failed due to the mapping was incomplete:" + mapperKey);
    }
  }

  @Transactional
  public Map<String, String> checkProductionDeployRequest(DeployToProductionRequest request) {
    Map<String, String> mapperKeyMap = new HashMap<>();
    for (TagInfoDto tagInfoDto : request.getTagInfos()) {
      Page<UnifiedAssetEntity> deployments =
          unifiedAssetRepository.findDeployments(
              request.getSourceEnvId(),
              DeployStatusEnum.SUCCESS.name(),
              ReleaseKindEnum.API_LEVEL.getKind(),
              AssetKindEnum.PRODUCT_DEPLOYMENT.getKind(),
              JsonToolkit.toJson(List.of(tagInfoDto.getTagId())),
              PageRequest.of(0, 10, Sort.Direction.DESC, FIELD_CREATE_AT_ORIGINAL));
      if (null == deployments || CollectionUtils.isEmpty(deployments.getContent())) {
        log.error("No successful deployments found with tagIds");
        throw KrakenException.badRequest(
            "No successful deployments found with tagIds:"
                + String.join(
                    ",", request.getTagInfos().stream().map(TagInfoDto::getTagId).toList()));
      }
      UnifiedAssetEntity releaseEntity = deployments.getContent().get(0);
      if (!DeployStatusEnum.SUCCESS.name().equals(releaseEntity.getStatus())) {
        log.error("The unsuccessful deployment is not allowed to push to production environment");
        throw KrakenException.badRequest(
            "The unsuccessful deployment is not allowed to push to production environment");
      }
      UnifiedAssetDto releaseAsset = UnifiedAssetService.toAsset(releaseEntity, true);
      String envId = releaseAsset.getMetadata().getLabels().getOrDefault(LABEL_ENV_ID, null);
      if (envId != null && !envId.equals(request.getSourceEnvId())) {
        log.error("Lacking of source envId in request");
        throw KrakenException.badRequest("Lacking of source envId in request");
      }

      // The deployment on stage should be verified.
      validateTag(tagInfoDto.getTagId(), request.getTargetEnvId());

      DeploymentFacet deploymentFacet =
          UnifiedAsset.getFacets(releaseAsset, new TypeReference<>() {});
      List<ComponentTag> componentTags = deploymentFacet.getComponentTags();
      for (ComponentTag componentTag : componentTags) {
        mapperKeyMap.put(componentTag.getParentComponentKey(), Boolean.TRUE.toString());
      }
    }

    return mapperKeyMap;
  }

  public String extractMapperKeyFromTagAsset(UnifiedAssetEntity tagAsset) {
    if (!AssetKindEnum.COMPONENT_API_TARGET_MAPPER_TAG.getKind().equals(tagAsset.getKind())) {
      log.warn("The asset kind is not {}", AssetKindEnum.COMPONENT_API_TARGET_MAPPER_TAG.getKind());
      return "";
    }
    String mapperKey = tagAsset.getLabels().getOrDefault("mapperKey", "");
    if (StringUtils.isNotBlank(mapperKey)) {
      return mapperKey;
    }
    int loc = tagAsset.getKey().indexOf(TAG);
    if (loc <= 0) {
      return "";
    }
    return tagAsset.getKey().substring(0, loc);
  }

  public void validateTag(String tagId, String envId) {
    UnifiedAssetEntity tagAsset = unifiedAssetService.findOneByIdOrKey(tagId);
    String verified = tagAsset.getLabels().get(VERIFIED_STATUS);
    if (!"true".equals(verified)) {
      throw KrakenException.badRequest(
          "The unverified deployment is not allowed to push to production environment");
    }

    String mapperKey = extractMapperKeyFromTagAsset(tagAsset);
    Optional<UnifiedAssetDto> latestDeploymentAssetOpt =
        queryLatestSuccessDeploymentAsset(mapperKey, envId);
    if (latestDeploymentAssetOpt.isEmpty()) {
      return;
    }

    UnifiedAssetDto latestDeploymentAsset = latestDeploymentAssetOpt.get();
    double runningVersion = computeMaximumRunningVersion(latestDeploymentAsset);
    String currentVersion = tagAsset.getLabels().getOrDefault("version", "1.0");
    if (Double.parseDouble(currentVersion) <= runningVersion) {
      String errorMsg =
          String.format(
              "The request version is not greater than the latest running version, tagId:%s, currentVersion:%s, latestRunningVersion:%s",
              tagAsset.getKey(), currentVersion, runningVersion);
      throw KrakenException.badRequest(errorMsg);
    }
  }

  public Paging<UnifiedAssetDto> search(
      String componentId, boolean facetIncluded, String q, PageRequest pageRequest) {
    return this.unifiedAssetService.search(
        componentId, AssetKindEnum.COMPONENT_API_TAG.getKind(), facetIncluded, q, pageRequest);
  }

  private String generateVersionName() {
    String name = DateTimeFormatter.ofPattern("yyyyMMddHHmm").format(ZonedDateTime.now());
    return String.format("Version:%s", name);
  }

  @Transactional
  public List<ComponentWitheVersionDTO> listVersions() {

    List<UnifiedAssetEntity> tags =
        unifiedAssetRepository.findByKindOrderByCreatedAtDesc(
            AssetKindEnum.COMPONENT_API_TAG.getKind());
    if (CollectionUtils.isEmpty(tags)) {
      return Collections.emptyList();
    }
    List<UUID> componentKeys =
        tags.stream()
            .map(UnifiedAssetEntity::getParentId)
            .map(UUID::fromString)
            .distinct()
            .toList();
    Map<String, UnifiedAssetDto> assetEntityMap =
        unifiedAssetRepository.findAllByIdIn(componentKeys).stream()
            .collect(
                Collectors.toMap(
                    t -> t.getId().toString(), t -> UnifiedAssetService.toAsset(t, false)));
    return tags.stream()
        .collect(Collectors.groupingBy(UnifiedAssetEntity::getParentId))
        .entrySet()
        .stream()
        .map(
            entry -> {
              List<ComponentVersionDto> componentVersionDtos =
                  entry.getValue().stream()
                      .sorted(Comparator.comparing(UnifiedAssetEntity::getCreatedAt).reversed())
                      .limit(100)
                      .map(
                          entity -> {
                            ComponentVersionDto componentVersionDto = new ComponentVersionDto();
                            componentVersionDto.setId(entity.getId().toString());
                            componentVersionDto.setVersion(
                                entity.getLabels().get(LabelConstants.LABEL_VERSION_NAME));
                            return componentVersionDto;
                          })
                      .toList();
              return Map.entry(entry.getKey(), componentVersionDtos);
            })
        .map(
            entry -> {
              ComponentWitheVersionDTO componentWitheVersionDTO = new ComponentWitheVersionDTO();
              componentWitheVersionDTO.setComponentVersions(entry.getValue());
              componentWitheVersionDTO.setKey(entry.getKey());
              String componentName = assetEntityMap.get(entry.getKey()).getMetadata().getName();
              componentWitheVersionDTO.setName(componentName);
              return componentWitheVersionDTO;
            })
        .toList();
  }

  public IngestionDataResult createSystemTemplateTag(
      String productId, List<UnifiedAssetDto> systemTemplates) {
    long currentTime = System.currentTimeMillis();
    String key = "mef.sonata.product.system-template" + TAG + currentTime;
    UnifiedAsset tagAsset =
        UnifiedAsset.of(AssetKindEnum.COMPONENT_API_SYSTEM_TEMPLATE_TAG.getKind(), key, key);
    tagAsset.getMetadata().setDescription("application system template");
    tagAsset.getMetadata().getLabels().put(LabelConstants.LABEL_VERSION_NAME, "" + currentTime);
    tagAsset
        .getMetadata()
        .getLabels()
        .put(LabelConstants.LABEL_RELEASE_KIND, ReleaseKindEnum.SYSTEM_TEMPLATE_MIXED.getKind());

    Map<String, Object> facets = new HashMap<>();
    facets.put(ComponentTagFacet.KEY_CHILDREN, systemTemplates);

    tagAsset.setFacets(facets);

    SyncMetadata syncMetadata = new SyncMetadata("", "", DateTime.nowInUTCString());
    return unifiedAssetService.syncAsset(productId, tagAsset, syncMetadata, true);
  }

  protected String newVersion(UnifiedAssetEntity mapperEntity) {
    return unifiedAssetService
        .findLatest(mapperEntity.getId().toString(), AssetKindEnum.COMPONENT_API_TARGET_MAPPER_TAG)
        .map(tag -> tag.getLabels().get(LABEL_VERSION_NAME))
        .map(VersionHelper::upgradeVersionByZeroPointOne)
        .orElse(Constants.INIT_VERSION);
  }
}
