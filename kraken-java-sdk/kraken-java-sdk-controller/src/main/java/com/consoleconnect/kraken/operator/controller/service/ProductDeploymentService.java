package com.consoleconnect.kraken.operator.controller.service;

import static com.consoleconnect.kraken.operator.controller.model.DeploymentFacet.*;
import static com.consoleconnect.kraken.operator.core.enums.AssetLinkKindEnum.DEPLOYMENT_API_TAG;
import static com.consoleconnect.kraken.operator.core.enums.AssetLinkKindEnum.DEPLOYMENT_COMPONENT_API;
import static com.consoleconnect.kraken.operator.core.toolkit.AssetsConstants.*;
import static com.consoleconnect.kraken.operator.core.toolkit.LabelConstants.*;

import com.consoleconnect.kraken.operator.auth.repo.UserRepository;
import com.consoleconnect.kraken.operator.auth.security.UserContext;
import com.consoleconnect.kraken.operator.controller.dto.*;
import com.consoleconnect.kraken.operator.controller.entity.EnvironmentEntity;
import com.consoleconnect.kraken.operator.controller.enums.SystemStateEnum;
import com.consoleconnect.kraken.operator.controller.event.SingleMapperReportEvent;
import com.consoleconnect.kraken.operator.controller.model.*;
import com.consoleconnect.kraken.operator.controller.repo.EnvironmentRepository;
import com.consoleconnect.kraken.operator.controller.service.upgrade.UpgradeSourceServiceFactory;
import com.consoleconnect.kraken.operator.core.dto.DeployComponentError;
import com.consoleconnect.kraken.operator.core.dto.Tuple2;
import com.consoleconnect.kraken.operator.core.dto.UnifiedAssetDto;
import com.consoleconnect.kraken.operator.core.entity.EnvironmentClientEntity;
import com.consoleconnect.kraken.operator.core.entity.UnifiedAssetEntity;
import com.consoleconnect.kraken.operator.core.enums.*;
import com.consoleconnect.kraken.operator.core.event.IngestionDataResult;
import com.consoleconnect.kraken.operator.core.exception.KrakenException;
import com.consoleconnect.kraken.operator.core.model.*;
import com.consoleconnect.kraken.operator.core.model.facet.ComponentAPITargetFacets;
import com.consoleconnect.kraken.operator.core.repo.EnvironmentClientRepository;
import com.consoleconnect.kraken.operator.core.repo.UnifiedAssetRepository;
import com.consoleconnect.kraken.operator.core.service.UnifiedAssetService;
import com.consoleconnect.kraken.operator.core.toolkit.*;
import com.consoleconnect.kraken.operator.core.toolkit.AssetsConstants;
import com.consoleconnect.kraken.operator.core.toolkit.LabelConstants;
import com.fasterxml.jackson.core.type.TypeReference;
import jakarta.validation.constraints.NotNull;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.BeanUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@AllArgsConstructor
@Service
@Slf4j
public class ProductDeploymentService implements LatestDeploymentCalculator {
  @Getter private final UnifiedAssetService unifiedAssetService;
  private final UnifiedAssetRepository unifiedAssetRepository;
  private final ComponentTagService componentTagService;
  private final EnvironmentService environmentService;
  private final EnvironmentRepository environmentRepository;
  private final UserRepository userRepository;
  private final MgmtProperty mgmtProperty;
  private final UpgradeSourceServiceFactory upgradeSourceServiceFactory;
  private final ApplicationEventPublisher applicationEventPublisher;
  private final EnvironmentClientRepository environmentClientRepository;
  private final SystemInfoService systemInfoService;

  @Transactional(readOnly = true)
  public Paging<UnifiedAssetDto> search(
      String productId,
      String envId,
      String componentId,
      boolean facetIncluded,
      String q,
      PageRequest pageRequest) {
    UnifiedAssetEntity productEntity = unifiedAssetService.findOneByIdOrKey(productId);
    if (envId == null && componentId == null) {
      return unifiedAssetService.search(
          productEntity.getId().toString(),
          AssetKindEnum.PRODUCT_DEPLOYMENT.getKind(),
          facetIncluded,
          q,
          pageRequest);
    }
    Paging<UnifiedAssetDto> deploymentEntities =
        unifiedAssetService.search(
            productEntity.getId().toString(),
            AssetKindEnum.PRODUCT_DEPLOYMENT.getKind(),
            facetIncluded,
            q,
            PageRequest.of(0, 1000, pageRequest.getSort()));

    List<UnifiedAssetDto> data = deploymentEntities.getData();
    if (envId != null) {
      data =
          data.stream()
              .filter(it -> envId.equalsIgnoreCase(it.getMetadata().getLabels().get(LABEL_ENV_ID)))
              .toList();
    }
    if (componentId != null) {
      UnifiedAssetDto component = unifiedAssetService.findOne(componentId);
      data =
          data.stream()
              .filter(
                  it ->
                      it.getLinks().stream()
                          .anyMatch(
                              x ->
                                  x.getRelationship().equalsIgnoreCase("deployment.component.api")
                                      && x.getTargetAssetKey()
                                          .equalsIgnoreCase(component.getMetadata().getKey())))
              .toList();
    }

    return PagingHelper.toPage(data, pageRequest.getPageNumber(), pageRequest.getPageSize());
  }

  @Transactional
  public UnifiedAssetDto createMapperVersionAndDeploy(
      String productId,
      CreateAPIMapperDeploymentRequest request,
      ReleaseKindEnum releaseKindEnum,
      String createdBy,
      boolean allowConcurrent) {
    Map<String, String> mapperKeyMap = new HashMap<>();
    List<UnifiedAssetEntity> tagAssetList =
        request.getMapperKeys().stream()
            .map(
                mapperKey -> {
                  UnifiedAssetEntity data =
                      componentTagService
                          .createMappingTag(request.getComponentId(), mapperKey, createdBy)
                          .getData();
                  mapperKeyMap.put(mapperKey, Boolean.TRUE.toString());
                  return data;
                })
            .toList();
    List<String> tagIds =
        tagAssetList.stream().map(UnifiedAssetEntity::getId).map(UUID::toString).toList();
    CreateProductDeploymentRequest createProductDeploymentRequest =
        createDeploymentRequest(request.getEnvId(), tagIds);
    createProductDeploymentRequest.setTagIdMappers(mapperKeyMap);
    return create(
        productId, createProductDeploymentRequest, releaseKindEnum, createdBy, allowConcurrent);
  }

  @Transactional
  public UnifiedAssetDto deployStageToProduction(
      String productId,
      DeployToProductionRequest request,
      String createdBy,
      Map<String, String> tagIdMappers) {
    List<String> tagIds = request.getTagInfos().stream().map(TagInfoDto::getTagId).toList();
    CreateProductDeploymentRequest createProductDeploymentRequest =
        createDeploymentRequest(request.getTargetEnvId(), tagIds);
    createProductDeploymentRequest.setTagIdMappers(tagIdMappers);
    return create(
        productId, createProductDeploymentRequest, ReleaseKindEnum.API_LEVEL, createdBy, true);
  }

  private CreateProductDeploymentRequest createDeploymentRequest(
      String envId, List<String> tagIds) {
    CreateProductDeploymentRequest createProductDeploymentRequest =
        new CreateProductDeploymentRequest();
    createProductDeploymentRequest.setEnvId(envId);
    createProductDeploymentRequest.setTagIds(tagIds);
    return createProductDeploymentRequest;
  }

  @Transactional
  public UnifiedAssetDto deployComponents(
      String productId,
      CreateProductDeploymentRequest request,
      ReleaseKindEnum releaseKindEnum,
      String createdBy,
      boolean allowConcurrent) {
    return create(productId, request, releaseKindEnum, createdBy, allowConcurrent);
  }

  public UnifiedAssetDto create(
      String productId,
      CreateProductDeploymentRequest request,
      ReleaseKindEnum releaseKindEnum,
      String createdBy,
      boolean allowConcurrent) {
    UnifiedAssetDto productEntity = unifiedAssetService.findOne(productId);
    Environment environment = environmentService.findOne(request.getEnvId());
    Page<UnifiedAssetEntity> all =
        unifiedAssetRepository.findDeployments(
            request.getEnvId(),
            DeployStatusEnum.IN_PROCESS.name(),
            releaseKindEnum.getKind(),
            AssetKindEnum.PRODUCT_DEPLOYMENT.getKind(),
            null,
            PageRequest.of(0, 1, Sort.Direction.DESC, FIELD_CREATE_AT_ORIGINAL));
    if (!allowConcurrent && all.hasContent()) {
      log.error("Last deployment is still in progress");
      throw KrakenException.badRequest(
          "Last deployment to "
              + environment.getName()
              + " is still in progress.Please wait for a moment.");
    }
    if (request.getName() == null) {
      request.setName("Release." + DateTime.formatCompact(ZonedDateTime.now()));
    }

    String deploymentKey =
        productEntity.getMetadata().getKey() + ".product-release." + System.currentTimeMillis();
    UnifiedAsset productReleaseAsset =
        UnifiedAsset.of(
            AssetKindEnum.PRODUCT_DEPLOYMENT.getKind(), deploymentKey, request.getName());

    productReleaseAsset.getMetadata().setDescription(request.getDescription());
    productReleaseAsset.getMetadata().setVersion(1);
    productReleaseAsset.getMetadata().setStatus(DeployStatusEnum.IN_PROCESS.name());
    productReleaseAsset.getMetadata().getLabels().put(LABEL_ENV_ID, request.getEnvId());
    productReleaseAsset.getMetadata().getLabels().put(LABEL_ENV_NAME, environment.getName());
    if (MapUtils.isNotEmpty(request.getTagIdMappers())) {
      productReleaseAsset.getMetadata().getLabels().putAll(request.getTagIdMappers());
    }
    productReleaseAsset
        .getMetadata()
        .getLabels()
        .put(LabelConstants.LABEL_RELEASE_KIND, releaseKindEnum.getKind());
    Set<String> tags = new HashSet<>(request.getTagIds());
    productReleaseAsset.getMetadata().setTags(tags);
    List<UnifiedAssetEntity> componentTagEntities =
        unifiedAssetService.findAllByIdIn(new ArrayList<>(tags));
    List<UnifiedAssetEntity> componentEntities =
        unifiedAssetService.findAllByIdIn(
            componentTagEntities.stream().map(UnifiedAssetEntity::getParentId).distinct().toList());

    List<ComponentTag> componentTags =
        componentTagEntities.stream()
            .map(
                tag -> {
                  ComponentTag componentTag = new ComponentTag();
                  componentEntities.stream()
                      .filter(it -> it.getId().toString().equalsIgnoreCase(tag.getParentId()))
                      .findFirst()
                      .ifPresent(
                          component -> {
                            componentTag.setParentComponentName(component.getName());
                            componentTag.setParentComponentId(component.getId().toString());
                            componentTag.setTagId(tag.getId().toString());
                            componentTag.setParentComponentKey(component.getKey());
                            componentTag.setVersion(
                                tag.getLabels().get(LabelConstants.LABEL_VERSION_NAME));
                          });
                  return componentTag;
                })
            .toList();

    productReleaseAsset.getFacets().put(KEY_COMPONENT_TAGS, componentTags);

    List<AssetLink> links =
        Stream.concat(
                componentTagEntities.stream()
                    .map(entity -> createAssetLink(entity, DEPLOYMENT_API_TAG.getKind())),
                componentEntities.stream()
                    .map(entity -> createAssetLink(entity, DEPLOYMENT_COMPONENT_API.getKind())))
            .toList();

    productReleaseAsset.setLinks(links);

    SyncMetadata syncMetadata = new SyncMetadata("", "", DateTime.nowInUTCString(), createdBy);
    IngestionDataResult result =
        unifiedAssetService.syncAsset(
            productEntity.getId(), productReleaseAsset, syncMetadata, true);
    if (result.getCode() != 200) {
      throw new KrakenException(result.getCode(), result.getMessage());
    }

    return unifiedAssetService.findOne(result.getData().getId().toString());
  }

  private AssetLink createAssetLink(UnifiedAssetEntity entity, String relationship) {
    AssetLink link = new AssetLink();
    link.setTargetAssetKey(entity.getKey());
    link.setRelationship(relationship);
    return link;
  }

  public String findLatestInProcessDeployment(String envId) {
    Page<UnifiedAssetEntity> page =
        unifiedAssetRepository.findDeployments(
            envId,
            DeployStatusEnum.IN_PROCESS.name(),
            null,
            AssetKindEnum.PRODUCT_DEPLOYMENT.getKind(),
            null,
            PageRequest.of(
                0, 1, Sort.by(Sort.Order.desc(AssetsConstants.FIELD_CREATE_AT_ORIGINAL))));
    if (!page.hasContent()) {
      return null;
    }
    return page.getContent().get(0).getId().toString();
  }

  @Transactional
  public UnifiedAssetDto verifyMapperInLabels(
      String productId, VerifyMapperRequest request, String createdBy) {
    UnifiedAssetEntity tagAssetEntity = unifiedAssetService.findOneByIdOrKey(request.getTagId());
    UnifiedAssetDto tagDto = UnifiedAssetService.toAsset(tagAssetEntity, true);
    if (request.isVerified()) {
      tagDto.getMetadata().getLabels().put(LabelConstants.VERIFIED_STATUS, "true");
      tagDto.getMetadata().getLabels().put(LabelConstants.VERIFIED_BY, createdBy);
      tagDto
          .getMetadata()
          .getLabels()
          .put(LabelConstants.VERIFIED_AT, DateTime.nowInUTCFormatted());
    } else {
      tagDto.getMetadata().getLabels().put(LabelConstants.VERIFIED_STATUS, "false");
      tagDto.getMetadata().getLabels().remove(LabelConstants.VERIFIED_BY);
      tagDto.getMetadata().getLabels().remove(LabelConstants.VERIFIED_AT);
    }
    SyncMetadata syncMetadata = new SyncMetadata("", "", DateTime.nowInUTCString(), createdBy);
    IngestionDataResult result =
        unifiedAssetService.syncAsset(tagAssetEntity.getParentId(), tagDto, syncMetadata, true);
    if (result.getCode() != 200) {
      throw new KrakenException(result.getCode(), result.getMessage());
    }

    return unifiedAssetService.findOne(result.getData().getId().toString());
  }

  @Transactional(readOnly = true)
  public List<UnifiedAssetDto> queryDeployedAssets(String assetId) {
    UnifiedAssetEntity unifiedAssetEntity = unifiedAssetService.findOneByIdOrKey(assetId);
    UnifiedAssetDto assetDto = UnifiedAssetService.toAsset(unifiedAssetEntity, true);
    DeploymentFacet facets = UnifiedAsset.getFacets(assetDto, new TypeReference<>() {});
    List<String> tagIds = facets.getComponentTags().stream().map(ComponentTag::getTagId).toList();
    List<UnifiedAssetDto> deployedAssetDtos = new ArrayList<>();
    unifiedAssetService.findAllByIdIn(tagIds).stream()
        .map(ent -> UnifiedAssetService.toAsset(ent, true))
        .forEach(
            tag -> {
              ComponentTagFacet tagFacet = UnifiedAsset.getFacets(tag, new TypeReference<>() {});
              if (Objects.nonNull(tagFacet.getComponent())) {
                deployedAssetDtos.add(tagFacet.getComponent());
              }
              if (CollectionUtils.isNotEmpty(tagFacet.getChildren()))
                deployedAssetDtos.addAll(tagFacet.getChildren());
            });
    return deployedAssetDtos;
  }

  @Transactional
  public void reportConfigurationReloadingResult(
      String assetId, String status, List<DeployComponentError> errors) {
    log.info("report asset {} configuration reloading result, status: {}", assetId, status);

    UnifiedAssetEntity unifiedAssetEntity =
        unifiedAssetRepository
            .findById(UUID.fromString(assetId))
            .orElseThrow(
                () -> KrakenException.notFound(String.format("Asset %s not found", assetId)));
    if (DeployStatusEnum.FAILED.name().equals(status)) {
      unifiedAssetEntity.setStatus(DeployStatusEnum.FAILED.name());
      updateErrors(unifiedAssetEntity, errors);
    } else {
      unifiedAssetEntity.setStatus(DeployStatusEnum.SUCCESS.name());
    }
    log.info("report asset {} configuration reloading result, status: {}", assetId, status);
    unifiedAssetRepository.save(unifiedAssetEntity);
    if (unifiedAssetEntity
        .getLabels()
        .getOrDefault(LabelConstants.LABEL_ENV_NAME, "")
        .equalsIgnoreCase(mgmtProperty.getDefaultEnv())) {
      updateDeployedMapperStatus(assetId);
    }
    handleDeploymentCallback(unifiedAssetEntity);
  }

  private void updateErrors(UnifiedAssetEntity assetEntity, List<DeployComponentError> errors) {
    UnifiedAssetDto assetDto = UnifiedAssetService.toAsset(assetEntity, true);
    DeploymentFacet deploymentFacet = UnifiedAsset.getFacets(assetDto, new TypeReference<>() {});
    Map<String, Object> facets = new HashMap<>();
    facets.put(KEY_COMPONENT_TAGS, deploymentFacet.getComponentTags());
    facets.put(KEY_FAILURE_REASON, DeploymentErrorHelper.extractFailReason(errors));
    facets.put(KEY_ERRORS, errors);
    unifiedAssetService.syncFacets(assetEntity, facets);
  }

  private void handleDeploymentCallback(UnifiedAssetEntity mapperDeployment) {
    // handle mapper version report
    handlerMapperVersionReport(mapperDeployment);
    log.info("handlerMapperVersionReport end ok");
    if (MapUtils.isEmpty(mapperDeployment.getLabels())) {
      return;
    }
    String appTemplateDeploymentId =
        mapperDeployment.getLabels().get(LabelConstants.LABEL_APP_TEMPLATE_DEPLOYMENT_ID);
    if (StringUtils.isBlank(appTemplateDeploymentId)) {
      return;
    }
    handleTemplateDeploymentCallback(appTemplateDeploymentId);
  }

  private void handleTemplateDeploymentCallback(String appTemplateDeploymentId) {
    Optional<UnifiedAssetEntity> templateDeploymentOpt =
        unifiedAssetRepository.findById(UUID.fromString(appTemplateDeploymentId));
    templateDeploymentOpt.ifPresent(
        templateDeployment -> {
          TemplateUpgradeDeploymentFacets templateUpgradeDeploymentFacets =
              UnifiedAsset.getFacets(
                  UnifiedAssetService.toAsset(templateDeployment, true), new TypeReference<>() {});
          TemplateUpgradeDeploymentFacets.EnvDeployment envDeployment =
              templateUpgradeDeploymentFacets.getEnvDeployment();
          log.info("handleTemplateDeploymentCallback, before union");
          List<UUID> list =
              CollectionUtils.union(
                      envDeployment.getMapperDeployment(), envDeployment.getSystemDeployments())
                  .stream()
                  .map(UUID::fromString)
                  .toList();
          log.info("handleTemplateDeploymentCallback, after union, list size:{}", list.size());
          unifiedAssetRepository.findAllByIdIn(list).stream()
              .filter(ent -> DeployStatusEnum.IN_PROCESS.name().equalsIgnoreCase(ent.getStatus()))
              .findFirst()
              .ifPresentOrElse(
                  ent -> {
                    templateDeployment.setStatus(DeployStatusEnum.IN_PROCESS.name());
                    unifiedAssetRepository.save(templateDeployment);
                  },
                  () -> {
                    templateDeployment.setStatus(DeployStatusEnum.SUCCESS.name());
                    unifiedAssetRepository.save(templateDeployment);
                    String templateUpgradeId =
                        templateDeployment
                            .getLabels()
                            .getOrDefault(LABEL_APP_TEMPLATE_UPGRADE_ID, "");
                    upgradeSourceServiceFactory
                        .getUpgradeSourceService(templateUpgradeId)
                        .reportResult(templateUpgradeId, appTemplateDeploymentId);
                    updateSystemStatus(templateDeployment);
                  });
        });
  }

  public void updateSystemStatus(UnifiedAssetEntity templateDeployment) {
    String templateUpgradeId =
        templateDeployment.getLabels().getOrDefault(LABEL_APP_TEMPLATE_UPGRADE_ID, "");
    UnifiedAssetDto templateUpgrade = unifiedAssetService.findOne(templateUpgradeId);
    String envName = templateDeployment.getLabels().get(LABEL_ENV_NAME);
    String originVersion = templateUpgrade.getMetadata().getLabels().get(LABEL_PRODUCT_VERSION);
    String version = Constants.formatVersionUsingV(originVersion);
    if (EnvNameEnum.STAGE.name().equalsIgnoreCase(envName)) {
      systemInfoService.updateProductVersion(
          SystemStateEnum.STAGE_UPGRADE_DONE, null, version, null);
    } else {
      systemInfoService.updateProductVersion(SystemStateEnum.RUNNING, null, null, version);
    }
  }

  protected void updateDeployedMapperStatus(String deploymentId) {
    UnifiedAssetDto assetDto = unifiedAssetService.findOne(deploymentId);
    DeploymentFacet deploymentFacet = UnifiedAsset.getFacets(assetDto, new TypeReference<>() {});
    deploymentFacet
        .getComponentTags()
        .forEach(
            componentTag ->
                unifiedAssetRepository
                    .findOneByKey(componentTag.getParentComponentKey())
                    .ifPresent(
                        entity -> {
                          if (entity
                              .getKind()
                              .equalsIgnoreCase(
                                  AssetKindEnum.COMPONENT_API_TARGET_MAPPER.getKind())) {
                            Map<String, String> labels = entity.getLabels();
                            labels.put(
                                LabelConstants.LABEL_DEPLOYED_STATUS,
                                LabelConstants.VALUE_DEPLOYED_STATUS_DEPLOYED);
                            labels.put(LABEL_STAGE_DEPLOYED_STATUS, VALUE_DEPLOYED_STATUS_DEPLOYED);
                            unifiedAssetRepository.save(entity);
                          }
                        }));
  }

  @Transactional(readOnly = true)
  public Paging<EnvironmentComponentDto> retrieveDeployedComponents(
      String envId, String status, boolean history, PageRequest pageRequest) {
    Paging<UnifiedAssetEntity> latestDeploymentsPage =
        queryDeployments(envId, status, ReleaseKindEnum.COMPONENT_LEVEL, pageRequest);
    if (CollectionUtils.isEmpty(latestDeploymentsPage.getData())) {
      return PagingHelper.toPage(
          Collections.emptyList(),
          latestDeploymentsPage.getPage(),
          latestDeploymentsPage.getSize());
    }
    List<EnvironmentComponentDto> list =
        latestDeploymentsPage.getData().stream()
            .map(ent -> UnifiedAssetService.toAsset(ent, true))
            .map(
                deploymentEntity -> {
                  DeploymentFacet facets =
                      UnifiedAsset.getFacets(deploymentEntity, new TypeReference<>() {});
                  List<ComponentVersionDto> componentVersionDtos =
                      facets.getComponentTags().stream()
                          .map(
                              facet -> {
                                ComponentVersionDto componentVersionDto = new ComponentVersionDto();
                                componentVersionDto.setVersion(facet.getVersion());
                                componentVersionDto.setId(facet.getTagId());
                                componentVersionDto.setKey(facet.getParentComponentKey());
                                componentVersionDto.setComponentName(
                                    facet.getParentComponentName());
                                return componentVersionDto;
                              })
                          .toList();
                  return genEnvironmentComponentDto(deploymentEntity, componentVersionDtos);
                })
            .toList();
    return PagingHelper.toPage(
        list, latestDeploymentsPage.getPage(), latestDeploymentsPage.getSize());
  }

  private static EnvironmentComponentDto genEnvironmentComponentDto(
      UnifiedAssetDto deploymentEntity, List<ComponentVersionDto> componentVersionDtos) {
    EnvironmentComponentDto environmentComponentDto = new EnvironmentComponentDto();
    environmentComponentDto.setComponents(componentVersionDtos);
    environmentComponentDto.setId(deploymentEntity.getMetadata().getLabels().get(LABEL_ENV_ID));
    environmentComponentDto.setName(deploymentEntity.getMetadata().getLabels().get(LABEL_ENV_NAME));
    environmentComponentDto.setStatus(deploymentEntity.getMetadata().getStatus());
    BeanCopyUtil.copyOperationInfo(deploymentEntity, environmentComponentDto);
    return environmentComponentDto;
  }

  private @NotNull Paging<UnifiedAssetEntity> queryDeployments(
      String envId, String status, ReleaseKindEnum kind, PageRequest pageRequest) {
    Page<UnifiedAssetEntity> entities =
        unifiedAssetRepository.findDeployments(
            envId,
            status,
            kind.getKind(),
            AssetKindEnum.PRODUCT_DEPLOYMENT.getKind(),
            null,
            PageRequest.of(
                pageRequest.getPageNumber(),
                pageRequest.getPageSize(),
                Sort.Direction.DESC,
                FIELD_CREATE_AT_ORIGINAL));
    return PagingHelper.toPaging(entities, t -> t);
  }

  @Transactional(readOnly = true)
  public Paging<EnvironmentComponentDto> queryRunningComponent(String envId) {
    PageRequest pageRequest =
        PageRequest.of(0, 1000, Sort.Direction.DESC, FIELD_CREATE_AT_ORIGINAL);
    Page<UnifiedAssetEntity> allDeploymentsPage =
        unifiedAssetRepository.findDeployments(
            envId,
            DeployStatusEnum.SUCCESS.name(),
            ReleaseKindEnum.COMPONENT_LEVEL.getKind(),
            AssetKindEnum.PRODUCT_DEPLOYMENT.getKind(),
            null,
            pageRequest);
    if (CollectionUtils.isEmpty(allDeploymentsPage.getContent())) {
      return PagingHelper.toPage(Collections.emptyList(), 0, 10);
    }

    List<UnifiedAssetEntity> allDeployments = allDeploymentsPage.getContent();
    Map<String, Optional<UnifiedAssetEntity>> latestDeploymentMap =
        allDeployments.stream()
            .collect(
                Collectors.groupingBy(
                    this::getEnvId,
                    Collectors.maxBy(Comparator.comparing(UnifiedAssetEntity::getCreatedAt))));
    Map<String, EnvironmentEntity> environmentEntityMap =
        environmentRepository.findAll().stream()
            .collect(Collectors.toMap(t -> t.getId().toString(), Function.identity()));

    Map<String, UnifiedAssetDto> assetEntityMap =
        unifiedAssetRepository
            .findByKindOrderByCreatedAtDesc(AssetKindEnum.COMPONENT_API.getKind())
            .stream()
            .collect(
                Collectors.toMap(
                    UnifiedAssetEntity::getKey, t -> UnifiedAssetService.toAsset(t, false)));
    // env->(componentKey,version)
    Map<String, Map<String, String>> resultmap = new HashMap<>();
    allDeployments.forEach(
        deploymentEntity -> {
          resultmap.putIfAbsent(getEnvId(deploymentEntity), new HashMap<>());
          UnifiedAssetDto deploymentAsset = UnifiedAssetService.toAsset(deploymentEntity, true);
          DeploymentFacet deploymentFacet =
              UnifiedAsset.getFacets(deploymentAsset, new TypeReference<>() {});
          deploymentFacet
              .getComponentTags()
              .forEach(
                  componentTag -> {
                    Map<String, String> componentsMap = resultmap.get(getEnvId(deploymentEntity));
                    componentsMap.putIfAbsent(
                        componentTag.getParentComponentKey(), componentTag.getVersion());
                  });
        });
    List<EnvironmentComponentDto> list =
        resultmap.entrySet().stream()
            .map(
                entry -> {
                  String envIdKey = entry.getKey();
                  Map<String, String> componentKeyMap = entry.getValue();
                  List<ComponentVersionDto> componentVersionDtos =
                      componentKeyMap.entrySet().stream()
                          .map(
                              component -> {
                                ComponentVersionDto componentVersionDto = new ComponentVersionDto();
                                componentVersionDto.setVersion(component.getValue());
                                componentVersionDto.setKey(component.getKey());
                                String componentName =
                                    assetEntityMap.get(component.getKey()).getMetadata().getName();
                                componentVersionDto.setComponentName(componentName);
                                return componentVersionDto;
                              })
                          .toList();
                  EnvironmentComponentDto environmentComponentDto = new EnvironmentComponentDto();
                  environmentComponentDto.setComponents(componentVersionDtos);
                  environmentComponentDto.setId(envIdKey);
                  latestDeploymentMap
                      .get(envIdKey)
                      .ifPresent(
                          entity -> {
                            environmentComponentDto.setUpdatedAt(
                                DateTime.format(entity.getUpdatedAt()));
                            environmentComponentDto.setCreatedAt(
                                DateTime.format(entity.getCreatedAt()));
                          });
                  environmentComponentDto.setName(
                      environmentEntityMap.get(envIdKey) == null
                          ? ""
                          : environmentEntityMap.get(envIdKey).getName());
                  return environmentComponentDto;
                })
            .toList();

    return PagingHelper.toPage(list, 0, 10);
  }

  String getEnvId(UnifiedAssetEntity entity) {
    return entity.getLabels().get(LABEL_ENV_ID);
  }

  @Transactional(readOnly = true)
  public Paging<ApiMapperDeploymentDTO> retrieveApiMapperDeployments(
      String envId, String mapperKey, DeployStatusEnum deployStatusEnum, PageRequest pageRequest) {
    List<Tuple2> eqConditions = new ArrayList<>();
    eqConditions.add(Tuple2.of(KIND, AssetKindEnum.PRODUCT_DEPLOYMENT.getKind()));
    String status = Optional.ofNullable(deployStatusEnum).map(DeployStatusEnum::name).orElse(null);
    if (StringUtils.isNotBlank(status)) {
      eqConditions.add(Tuple2.of(STATUS, status));
    }
    List<Tuple2> labelConditions = new ArrayList<>();
    labelConditions.add(Tuple2.of(LABEL_RELEASE_KIND, ReleaseKindEnum.API_LEVEL.getKind()));
    if (StringUtils.isNotBlank(envId)) {
      labelConditions.add(Tuple2.of(LABEL_ENV_ID, envId));
    }
    if (StringUtils.isNotBlank(mapperKey)) {
      labelConditions.add(Tuple2.of(mapperKey, Boolean.TRUE.toString()));
    }
    Paging<UnifiedAssetDto> deployments =
        unifiedAssetService.findBySpecification(
            eqConditions,
            labelConditions,
            null,
            PageRequest.of(
                pageRequest.getPageNumber(),
                pageRequest.getPageSize(),
                Sort.Direction.DESC,
                FIELD_CREATE_AT),
            null);
    if (null == deployments || CollectionUtils.isEmpty(deployments.getData())) {
      return PagingHelper.toPage(Collections.emptyList(), 0, 10);
    }
    Map<String, Pair<String, String>> mapper2Component = getMapper2Component();
    Map<String, UnifiedAssetDto> mapperAssetMap = getMapperAssetMap();

    List<ApiMapperDeploymentDTO> result =
        getApiMapperDeploymentDTOS(deployments, mapperAssetMap, mapper2Component, envId);
    return PagingHelper.toPageNoSubList(
        result, deployments.getPage(), deployments.getSize(), deployments.getTotal());
  }

  private Map<String, UnifiedAssetDto> getMapperAssetMap() {
    return unifiedAssetService
        .search(
            null,
            AssetKindEnum.COMPONENT_API_TARGET_MAPPER.getKind(),
            true,
            null,
            PageRequest.of(0, 100))
        .getData()
        .stream()
        .collect(Collectors.toMap(t -> t.getMetadata().getKey(), t -> t));
  }

  private Map<String, Pair<String, String>> getMapper2Component() {
    // <mapperKey,<componentKey,componentName>>
    Map<String, Pair<String, String>> mapper2Component = new HashMap<>();
    unifiedAssetService
        .search(null, AssetKindEnum.COMPONENT_API.getKind(), true, null, PageRequest.of(0, 100))
        .getData()
        .forEach(
            comAsset ->
                comAsset.getLinks().stream()
                    .filter(
                        link ->
                            link.getRelationship()
                                .equalsIgnoreCase(
                                    AssetLinkKindEnum.IMPLEMENTATION_TARGET_MAPPER.getKind()))
                    .forEach(
                        link ->
                            mapper2Component.put(
                                link.getTargetAssetKey(),
                                Pair.of(
                                    comAsset.getMetadata().getKey(),
                                    comAsset.getMetadata().getName()))));
    return mapper2Component;
  }

  private @NotNull List<ApiMapperDeploymentDTO> getApiMapperDeploymentDTOS(
      Paging<UnifiedAssetDto> allDeploymentsPage,
      Map<String, UnifiedAssetDto> mapperAssetMap,
      Map<String, Pair<String, String>> mapper2Component,
      String envId) {
    List<ApiMapperDeploymentDTO> result = new ArrayList<>();
    List<UnifiedAssetDto> allDeployments = allDeploymentsPage.getData();
    allDeployments.forEach(
        assetDto -> {
          Map<String, String> labels = assetDto.getMetadata().getLabels();
          DeploymentFacet facets = UnifiedAsset.getFacets(assetDto, new TypeReference<>() {});
          facets
              .getComponentTags()
              .forEach(
                  dto -> {
                    ApiMapperDeploymentDTO deploymentDTO = new ApiMapperDeploymentDTO();
                    if (mapperAssetMap.containsKey(dto.getParentComponentKey())) {
                      UnifiedAssetDto mapperAsset = mapperAssetMap.get(dto.getParentComponentKey());
                      ComponentAPITargetFacets componentAPITargetFacets =
                          UnifiedAsset.getFacets(mapperAsset, ComponentAPITargetFacets.class);
                      ComponentAPITargetFacets.Trigger trigger =
                          componentAPITargetFacets.getTrigger();
                      if (trigger != null) {
                        BeanUtils.copyProperties(trigger, deploymentDTO);
                        ComponentExpandDTO.MappingMatrix mappingMatrix =
                            new ComponentExpandDTO.MappingMatrix();
                        BeanUtils.copyProperties(trigger, mappingMatrix);
                        deploymentDTO.setMappingMatrix(mappingMatrix);
                      }
                      deploymentDTO.setTargetMapperKey(mapperAsset.getMetadata().getKey());
                    }

                    deploymentDTO.setCreateAt(assetDto.getCreatedAt());
                    deploymentDTO.setCreateBy(assetDto.getCreatedBy());
                    setUserName(deploymentDTO);
                    deploymentDTO.setStatus(assetDto.getMetadata().getStatus());
                    deploymentDTO.setFailureReason(facets.getFailureReason());
                    deploymentDTO.setErrors(facets.getErrors());
                    deploymentDTO.setVersion(dto.getVersion());
                    deploymentDTO.setReleaseKey(assetDto.getMetadata().getKey());
                    deploymentDTO.setReleaseId(assetDto.getId());
                    deploymentDTO.setTagId(dto.getTagId());

                    deploymentDTO.setEnvId(labels.getOrDefault(LABEL_ENV_ID, ""));
                    deploymentDTO.setEnvName(labels.getOrDefault(LABEL_ENV_NAME, ""));
                    fillVerifiedInfo(dto.getTagId(), deploymentDTO, envId);
                    calculateCanDeployToTargetEnv(deploymentDTO);
                    deploymentDTO.setComponentKey(
                        mapper2Component.get(dto.getParentComponentKey()).getKey());
                    deploymentDTO.setComponentName(
                        mapper2Component.get(dto.getParentComponentKey()).getValue());
                    result.add(deploymentDTO);
                  });
        });
    return result;
  }

  public void fillVerifiedInfo(String tagId, ApiMapperDeploymentDTO deploymentDTO, String envId) {
    if (StringUtils.isBlank(tagId)) {
      return;
    }
    Optional<UnifiedAssetEntity> optionalTagAsset =
        unifiedAssetRepository.findById(UUID.fromString(tagId));
    if (optionalTagAsset.isEmpty()) {
      return;
    }
    Map<String, String> labels = optionalTagAsset.get().getLabels();
    if (MapUtils.isEmpty(labels)) {
      return;
    }
    if (StringUtils.isBlank(envId) || deploymentDTO.getEnvId().equalsIgnoreCase(envId)) {
      deploymentDTO.setVerifiedBy(labels.getOrDefault(LabelConstants.VERIFIED_BY, ""));
      deploymentDTO.setVerifiedAt(labels.getOrDefault(LabelConstants.VERIFIED_AT, ""));
      deploymentDTO.setVerifiedStatus(
          Boolean.parseBoolean(labels.getOrDefault(LabelConstants.VERIFIED_STATUS, "false")));
    }
  }

  private void setUserName(ApiMapperDeploymentDTO deploymentDTO) {
    deploymentDTO.setUserName(UserContext.ANONYMOUS);
    if (StringUtils.isNotBlank(deploymentDTO.getCreateBy())) {
      try {
        UUID uuid = UUID.fromString(deploymentDTO.getCreateBy());
        userRepository
            .findById(uuid)
            .ifPresent(userEntity -> deploymentDTO.setUserName(userEntity.getName()));
      } catch (Exception e) {
        deploymentDTO.setUserName(deploymentDTO.getCreateBy());
      }
      log.info("setUserName, createdBy{}", deploymentDTO.getCreateBy());
    }
  }

  @Transactional
  public List<ComponentVersionWithEnvDto> listComponentVersionWithEnv(String componentKey) {
    UnifiedAssetEntity componentEntity =
        unifiedAssetRepository.findOneByKey(componentKey).orElse(null);
    Specification<UnifiedAssetEntity> specification =
        (root, query, criteriaBuilder) ->
            criteriaBuilder.equal(root.get(FIELD_PARENT_ID), componentEntity.getId().toString());
    List<UnifiedAssetEntity> allComponentTags =
        unifiedAssetRepository
            .findAll(specification, PageRequest.of(0, 500, Sort.Direction.DESC, FIELD_CREATE_AT))
            .getContent();
    if (CollectionUtils.isEmpty(allComponentTags)) {
      return Collections.emptyList();
    }
    List<String> tagIds =
        allComponentTags.stream().map(UnifiedAssetEntity::getId).map(UUID::toString).toList();
    Page<UnifiedAssetEntity> deployments =
        unifiedAssetRepository.findDeployments(
            null,
            DeployStatusEnum.SUCCESS.name(),
            ReleaseKindEnum.COMPONENT_LEVEL.getKind(),
            AssetKindEnum.PRODUCT_DEPLOYMENT.getKind(),
            JsonToolkit.toJson(tagIds),
            PageRequest.of(0, 500));

    if (CollectionUtils.isEmpty(deployments.getContent())) {
      return Collections.emptyList();
    }
    Map<String, EnvironmentEntity> envMetaMap =
        environmentRepository.findAll().stream()
            .collect(Collectors.toMap(t -> t.getId().toString(), t -> t));
    List<ComponentVersionWithEnvDto> result = new ArrayList<>();
    // <componentKey,<envId,version>>
    Map<String, Map<String, String>> componentEnvMap = new HashMap<>();
    deployments.stream()
        .forEach(
            deployEntity -> {
              String envId = getEnvId(deployEntity);
              UnifiedAssetDto deployAsset = UnifiedAssetService.toAsset(deployEntity, true);
              DeploymentFacet deploymentFacet =
                  UnifiedAsset.getFacets(deployAsset, new TypeReference<>() {});
              deploymentFacet.getComponentTags().stream()
                  .filter(tag -> tag.getParentComponentKey().equalsIgnoreCase(componentKey))
                  .forEach(
                      tag -> {
                        componentEnvMap.putIfAbsent(tag.getParentComponentKey(), new HashMap<>());
                        Map<String, String> envMap =
                            componentEnvMap.get(tag.getParentComponentKey());
                        if (!envMap.containsKey(envId)) {
                          ComponentVersionWithEnvDto componentVersionWithEnvDto =
                              new ComponentVersionWithEnvDto();
                          EnvironmentDto environmentDto = new EnvironmentDto();
                          environmentDto.setId(envId);
                          EnvironmentEntity environmentEntity = envMetaMap.get(envId);
                          environmentDto.setName(
                              environmentEntity == null ? "" : environmentEntity.getName());
                          componentVersionWithEnvDto.setEnv(environmentDto);
                          componentVersionWithEnvDto.setVersion(tag.getVersion());
                          componentVersionWithEnvDto.setName(tag.getParentComponentName());
                          componentVersionWithEnvDto.setId(tag.getTagId());
                          result.add(componentVersionWithEnvDto);
                        }
                        envMap.putIfAbsent(envId, tag.getVersion());
                      });
            });
    return result;
  }

  public List<LatestDeploymentDTO> queryLatestApiMapperDeployment(
      String productId, String mapperKey) {
    unifiedAssetService.findOne(productId);
    return environmentService.findAll().stream()
        .map(
            env -> {
              LatestDeploymentDTO latestDeploymentDTO = new LatestDeploymentDTO();
              latestDeploymentDTO.setEnvId(env.getId());
              latestDeploymentDTO.setEnvName(env.getName());
              List<Tuple2> inConditions =
                  List.of(
                      Tuple2.of(STATUS, DeployStatusEnum.SUCCESS.name()),
                      Tuple2.of(STATUS, DeployStatusEnum.FAILED.name()));
              queryLatestDeploymentAsset(mapperKey, env.getId(), inConditions)
                  .ifPresent(
                      deploymentAssetDto -> {
                        double runningVersion =
                            computeMaximumRunningVersion(
                                deploymentAssetDto, mapperKey, env.getId());
                        latestDeploymentDTO.setRunningVersion(
                            INIT_VERSION == runningVersion ? "" : String.valueOf(runningVersion));
                        latestDeploymentDTO.setStatus(deploymentAssetDto.getMetadata().getStatus());
                        latestDeploymentDTO.setCreateAt(deploymentAssetDto.getCreatedAt());
                        latestDeploymentDTO.setCreateBy(deploymentAssetDto.getCreatedBy());
                        latestDeploymentDTO.setMapperKey(
                            deploymentAssetDto.getMetadata().getLabels().entrySet().stream()
                                .filter(entry -> entry.getKey().contains("target-mapper"))
                                .findFirst()
                                .map(Map.Entry::getKey)
                                .orElse(null));
                      });
              return latestDeploymentDTO;
            })
        .toList();
  }

  private void handlerMapperVersionReport(UnifiedAssetEntity mapperDeployment) {

    UnifiedAssetDto assetDto = UnifiedAssetService.toAsset(mapperDeployment, true);
    DeploymentFacet deploymentFacet = UnifiedAsset.getFacets(assetDto, DeploymentFacet.class);
    String envId = mapperDeployment.getLabels().get(LABEL_ENV_ID);
    List<ComponentTag> componentTags = deploymentFacet.getComponentTags();
    if (CollectionUtils.isNotEmpty(componentTags)) {
      componentTags.forEach(
          componentTag -> {
            UnifiedAssetEntity tag = unifiedAssetService.findOneByIdOrKey(componentTag.getTagId());
            String version = tag.getLabels().get(LABEL_VERSION_NAME);
            String subVersion = tag.getLabels().get(LABEL_SUB_VERSION_NAME);
            log.info("handlerMapperVersionReport, version:{}, subVersion:{}", version, subVersion);
            applicationEventPublisher.publishEvent(
                new SingleMapperReportEvent(
                    envId, componentTag.getParentComponentKey(), version, subVersion));
          });
    }
  }

  @Transactional(readOnly = true)
  public List<ApiMapperDeploymentDTO> listRunningApiMapperDeploymentV3(String envId) {
    Environment environment = environmentService.findOne(envId);
    List<EnvironmentClientEntity> allRunningMappers =
        environmentClientRepository.findAllByEnvIdAndKind(
            envId, ClientReportTypeEnum.CLIENT_MAPPER_VERSION.name());
    Map<String, Pair<String, String>> mapper2Component = getMapper2Component();
    Map<String, UnifiedAssetDto> mapperAssetMap = getMapperAssetMap();
    Map<String, ClientMapperVersionPayloadDto> mapper2PayLoadMap =
        allRunningMappers.stream()
            .sorted(Comparator.comparing(EnvironmentClientEntity::getUpdatedAt).reversed())
            .map(t -> JsonToolkit.fromJson(t.getPayload(), ClientMapperVersionPayloadDto.class))
            .collect(Collectors.toMap(ClientMapperVersionPayloadDto::getMapperKey, t -> t));
    List<String> tagIds =
        mapper2PayLoadMap.values().stream()
            .map(ClientMapperVersionPayloadDto::getTagId)
            .filter(Objects::nonNull)
            .toList();
    if (CollectionUtils.isEmpty(tagIds)) {
      return List.of();
    }
    Map<String, UnifiedAssetEntity> tagEntityMap =
        unifiedAssetService.findAllByIdIn(tagIds).stream()
            .collect(Collectors.toMap(t -> t.getId().toString(), t -> t));
    return mapper2PayLoadMap.entrySet().stream()
        .map(
            entry -> {
              String mapperKey = entry.getKey();
              ClientMapperVersionPayloadDto payload = entry.getValue();
              UnifiedAssetEntity tagEntity = tagEntityMap.get(payload.getTagId());
              UnifiedAssetDto tagAsset = UnifiedAssetService.toAsset(tagEntity, false);
              Map<String, String> labels = tagAsset.getMetadata().getLabels();
              UnifiedAssetDto mapperAsset = mapperAssetMap.get(mapperKey);
              ComponentAPITargetFacets componentAPITargetFacets =
                  UnifiedAsset.getFacets(mapperAsset, ComponentAPITargetFacets.class);
              ComponentAPITargetFacets.Trigger trigger = componentAPITargetFacets.getTrigger();
              ApiMapperDeploymentDTO deploymentDTO = new ApiMapperDeploymentDTO();
              if (trigger != null) {
                BeanUtils.copyProperties(trigger, deploymentDTO);
                ComponentExpandDTO.MappingMatrix mappingMatrix =
                    new ComponentExpandDTO.MappingMatrix();
                BeanUtils.copyProperties(trigger, mappingMatrix);
                deploymentDTO.setMappingMatrix(mappingMatrix);
              }
              deploymentDTO.setCreateAt(tagAsset.getCreatedAt());
              deploymentDTO.setCreateBy(tagAsset.getCreatedBy());
              setUserName(deploymentDTO);
              deploymentDTO.setStatus(DeployStatusEnum.SUCCESS.name());
              deploymentDTO.setTargetMapperKey(mapperKey);
              deploymentDTO.setVersion(labels.get(LABEL_VERSION_NAME));
              deploymentDTO.setSubVersion(labels.get(LABEL_SUB_VERSION_NAME));
              deploymentDTO.setTagId(payload.getTagId());
              deploymentDTO.setEnvId(environment.getId());
              deploymentDTO.setEnvName(environment.getName());
              fillVerifiedInfo(tagAsset.getId(), deploymentDTO, envId);
              calculateCanDeployToTargetEnv(deploymentDTO);
              deploymentDTO.setComponentKey(mapper2Component.get(mapperKey).getKey());
              deploymentDTO.setComponentName(mapper2Component.get(mapperKey).getValue());
              return deploymentDTO;
            })
        .toList();
  }
}
