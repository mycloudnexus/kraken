package com.consoleconnect.kraken.operator.controller.service;

import static com.consoleconnect.kraken.operator.core.toolkit.AssetsConstants.*;
import static com.consoleconnect.kraken.operator.core.toolkit.LabelConstants.*;

import com.consoleconnect.kraken.operator.auth.security.UserContext;
import com.consoleconnect.kraken.operator.controller.dto.*;
import com.consoleconnect.kraken.operator.controller.model.ComponentTag;
import com.consoleconnect.kraken.operator.controller.model.ComponentTagFacet;
import com.consoleconnect.kraken.operator.controller.model.DeploymentFacet;
import com.consoleconnect.kraken.operator.core.dto.Tuple2;
import com.consoleconnect.kraken.operator.core.dto.UnifiedAssetDto;
import com.consoleconnect.kraken.operator.core.entity.*;
import com.consoleconnect.kraken.operator.core.enums.*;
import com.consoleconnect.kraken.operator.core.event.IngestionDataResult;
import com.consoleconnect.kraken.operator.core.exception.KrakenException;
import com.consoleconnect.kraken.operator.core.model.*;
import com.consoleconnect.kraken.operator.core.model.facet.ComponentAPITargetFacets;
import com.consoleconnect.kraken.operator.core.repo.AssetFacetRepository;
import com.consoleconnect.kraken.operator.core.repo.UnifiedAssetRepository;
import com.consoleconnect.kraken.operator.core.service.DataIngestionService;
import com.consoleconnect.kraken.operator.core.service.UnifiedAssetService;
import com.consoleconnect.kraken.operator.core.toolkit.*;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@AllArgsConstructor
@Service
public class ApiComponentService implements TargetMappingChecker {
  private static final Logger log = LoggerFactory.getLogger(ApiComponentService.class);
  public static final KrakenException ASSET_NOT_FOUND = KrakenException.notFound("asset not found");
  private final UnifiedAssetService unifiedAssetService;
  private final UnifiedAssetRepository unifiedAssetRepository;
  private final AssetFacetRepository assetFacetRepository;
  private final DataIngestionService ingestionService;
  private final AppProperty appProperty;

  public static final Pattern MAPPER_PATTERN = Pattern.compile("\\S+mapper.(\\S+)");

  @Transactional
  public IngestionDataResult updateApiTargetMapper(
      UnifiedAsset asset, String id, String updatedBy) {
    if (asset == null) {
      throw KrakenException.badRequest("request can not be null");
    }
    log.info("update mapper: {}", id);
    UnifiedAssetDto assetDto = unifiedAssetService.findOne(id);
    validateMapper(asset, assetDto);

    log.info("assert: {}", assetDto.getId());
    // update endpoint
    UnifiedAssetEntity assetEntity = updateUnifiedAssetEntity(asset, id, updatedBy);
    return IngestionDataResult.of(HttpStatus.OK.value(), "success", assetEntity);
  }

  public UnifiedAssetEntity updateUnifiedAssetEntity(
      UnifiedAsset asset, String id, String updatedBy) {
    UnifiedAssetEntity assetEntity = unifiedAssetService.findOneByIdOrKey(id);
    assetEntity.setUpdatedBy(updatedBy);
    Set<AssetFacetEntity> facets = assetEntity.getFacets();
    Optional<AssetFacetEntity> endpointsFacets =
        facets.stream().filter(v -> Objects.equals(END_POINTS, v.getKey())).findAny();
    endpointsFacets.ifPresent(
        facet -> {
          facet.setPayload(asset.getFacets().get(END_POINTS));
          assetFacetRepository.save(facet);
        });
    if (asset.getMetadata() != null && asset.getMetadata().getLabels() != null) {
      Map<String, String> labels =
          assetEntity.getLabels() == null ? new HashMap<>() : assetEntity.getLabels();
      labels.putAll(asset.getMetadata().getLabels());
    }
    // add labels not deployed
    assetEntity
        .getLabels()
        .put(LabelConstants.LABEL_DEPLOYED_STATUS, VALUE_DEPLOYED_STATUS_NOT_DEPLOYED);
    assetEntity.setVersion(assetEntity.getVersion() + 1);
    unifiedAssetRepository.save(assetEntity);
    return assetEntity;
  }

  private void validateMapper(UnifiedAsset request, UnifiedAssetDto origin) {
    if (!Objects.equals(origin.getMetadata().getKey(), request.getMetadata().getKey())) {
      throw KrakenException.badRequest("Updated mapper key should be same with key in metadata.");
    }
    ComponentAPITargetFacets requestFacets =
        UnifiedAsset.getFacets(request, ComponentAPITargetFacets.class);
    ComponentAPITargetFacets originFacets =
        UnifiedAsset.getFacets(origin, ComponentAPITargetFacets.class);
    if (requestFacets.getEndpoints() == null) {
      throw KrakenException.badRequest("endpoint can not be null");
    }
    ComponentAPITargetFacets.Endpoint endpoint = requestFacets.getEndpoints().get(0);
    ComponentAPITargetFacets.Mappers requestMapper = endpoint.getMappers();
    ComponentAPITargetFacets.Mappers originMapper = originFacets.getEndpoints().get(0).getMappers();
    if (originMapper == null) {
      return;
    }
    Pair<Map<String, ComponentAPITargetFacets.Mapper>, Map<String, ComponentAPITargetFacets.Mapper>>
        updatePair = constructMapperMap(requestMapper);
    Pair<Map<String, ComponentAPITargetFacets.Mapper>, Map<String, ComponentAPITargetFacets.Mapper>>
        originPair = constructMapperMap(originMapper);

    checkMapper(originPair.getLeft(), updatePair.getLeft(), false);
    checkMapper(originPair.getRight(), updatePair.getRight(), true);
  }

  private void checkMapper(
      Map<String, ComponentAPITargetFacets.Mapper> originMap,
      Map<String, ComponentAPITargetFacets.Mapper> updateMap,
      boolean isResponse) {
    originMap
        .entrySet()
        .forEach(
            entry -> {
              if (!updateMap.containsKey(entry.getKey())) {
                throw KrakenException.badRequest(
                    "The pre-defined field: "
                        + entry.getKey()
                        + " not existed in the update request! Please check your request payload.");
              }
              ComponentAPITargetFacets.Mapper updateMapper = updateMap.get(entry.getKey());
              ComponentAPITargetFacets.Mapper originMapper = entry.getValue();

              if (isResponse) {
                compareProperty(updateMapper.getTarget(), originMapper.getTarget());
                compareProperty(updateMapper.getTargetType(), originMapper.getTargetType());
                compareProperty(updateMapper.getTargetLocation(), originMapper.getTargetLocation());
                compareProperty(updateMapper.getTargetValues(), originMapper.getTargetValues());
              } else {
                compareProperty(updateMapper.getSource(), originMapper.getSource());
                compareProperty(updateMapper.getSourceLocation(), originMapper.getSourceLocation());
              }
              compareProperty(updateMapper.getTitle(), originMapper.getTitle());
              compareProperty(updateMapper.getRequiredMapping(), originMapper.getRequiredMapping());
              compareProperty(updateMapper.getReplaceStar(), originMapper.getReplaceStar());
              compareProperty(updateMapper.getFunction(), originMapper.getFunction());
              compareProperty(updateMapper.getCheckPath(), originMapper.getCheckPath());
              compareProperty(updateMapper.getDeletePath(), originMapper.getDeletePath());
            });
  }

  private void compareProperty(Object o1, Object o2) {
    if (!Objects.deepEquals(o1, o2)) {
      throw KrakenException.badRequest(
          "should not change pre-defined properties:" + o1 + ", " + o2);
    }
  }

  private Pair<
          Map<String, ComponentAPITargetFacets.Mapper>,
          Map<String, ComponentAPITargetFacets.Mapper>>
      constructMapperMap(ComponentAPITargetFacets.Mappers mappers) {
    Map<String, ComponentAPITargetFacets.Mapper> requestMap = new HashMap<>();
    Map<String, ComponentAPITargetFacets.Mapper> responseMap = new HashMap<>();

    List<ComponentAPITargetFacets.Mapper> request = mappers.getRequest();
    if (CollectionUtils.isNotEmpty(request)) {
      requestMap =
          request.stream()
              .filter(v -> Objects.equals(Boolean.FALSE, v.getCustomizedField()))
              .collect(
                  Collectors.toMap(ComponentAPITargetFacets.Mapper::getName, Function.identity()));
    }
    List<ComponentAPITargetFacets.Mapper> response = mappers.getResponse();
    if (CollectionUtils.isNotEmpty(response)) {
      responseMap =
          response.stream()
              .filter(v -> Objects.equals(Boolean.FALSE, v.getCustomizedField()))
              .collect(
                  Collectors.toMap(ComponentAPITargetFacets.Mapper::getName, Function.identity()));
    }
    return Pair.of(requestMap, responseMap);
  }

  @Transactional
  public ComponentExpandDTO queryComponentExpandInfo(
      String productId, String componentId, String envId) {
    unifiedAssetService.findOne(productId);
    UnifiedAssetDto unifiedAssetDto = unifiedAssetService.findOne(componentId);
    List<AssetLink> links = unifiedAssetDto.getLinks();
    if (CollectionUtils.isEmpty(links)) {
      throw KrakenException.badRequest(
          "The current componentId has no links, componentId:" + componentId);
    }
    List<String> queryExcludeAssets = appProperty.getQueryExcludeAssetKeys();
    List<String> assetKeyList =
        links.stream()
            .filter(
                link ->
                    AssetLinkKindEnum.IMPLEMENTATION_TARGET_MAPPER
                        .getKind()
                        .equalsIgnoreCase(link.getRelationship()))
            .map(AssetLink::getTargetAssetKey)
            .filter(targetAssetKey -> !queryExcludeAssets.contains(targetAssetKey))
            .distinct()
            .toList();
    Map<String, UnifiedAssetDto> assetEntityMap =
        unifiedAssetRepository.findAllByKeyIn(assetKeyList).stream()
            .collect(
                Collectors.toMap(
                    UnifiedAssetEntity::getKey, t -> UnifiedAssetService.toAsset(t, true)));

    List<ComponentExpandDTO.TargetMappingDetail> details = new ArrayList<>();
    assetEntityMap.forEach(
        (k, assetDto) -> {
          fillMappingStatus(assetDto);
          ComponentAPITargetFacets mapperFacets =
              UnifiedAsset.getFacets(assetDto, ComponentAPITargetFacets.class);
          ComponentAPITargetFacets.Trigger trigger = mapperFacets.getTrigger();
          ComponentExpandDTO.TargetMappingDetail detail =
              new ComponentExpandDTO.TargetMappingDetail();
          if (Objects.nonNull(trigger)) {
            detail.setPath(trigger.getPath());
            detail.setMethod(trigger.getMethod());
            detail.setProductType(trigger.getProductType());
            detail.setActionType(trigger.getActionType());
            ComponentExpandDTO.MappingMatrix mappingMatrix = new ComponentExpandDTO.MappingMatrix();
            BeanUtils.copyProperties(trigger, mappingMatrix);
            detail.setMappingMatrix(mappingMatrix);
          }
          if (containsKeywords(assetDto.getMetadata().getKey())) {
            detail.setRequiredMapping(false);
          }
          detail.setDescription(assetDto.getMetadata().getDescription());
          detail.setTargetMapperKey(k);
          detail.setTargetKey(extractTargetKey(k));
          detail.setMappingStatus(assetDto.getMappingStatus());
          detail.setUpdatedAt(assetDto.getUpdatedAt());
          detail.setUpdatedBy(assetDto.getUpdatedBy());
          detail.setOrderBy(
              appProperty.getApiTargetMapperOrderBy().getOrDefault(k, "<1000, 1000>"));
          mergeLastDeployment(detail, assetDto, envId);
          details.add(detail);
        });

    ComponentExpandDTO componentExpandDTO = new ComponentExpandDTO();
    componentExpandDTO.setDetails(
        details.stream()
            .sorted(Comparator.comparing(ComponentExpandDTO.TargetMappingDetail::getOrderBy))
            .toList());
    return componentExpandDTO;
  }

  private void mergeLastDeployment(
      ComponentExpandDTO.TargetMappingDetail detail, UnifiedAssetDto assetDto, String envId) {
    List<Tuple2> eqConditions = new ArrayList<>();
    eqConditions.add(Tuple2.of(KIND, AssetKindEnum.PRODUCT_DEPLOYMENT.getKind()));
    List<Tuple2> labelConditions = new ArrayList<>();
    if (StringUtils.isNotBlank(envId)) {
      labelConditions.add(Tuple2.of(LABEL_ENV_ID, envId));
    }
    labelConditions.add(Tuple2.of(detail.getTargetMapperKey(), Boolean.TRUE.toString()));
    Paging<UnifiedAssetDto> deployments =
        unifiedAssetService.findBySpecification(
            eqConditions,
            labelConditions,
            null,
            PageRequest.of(0, 10, Sort.Direction.DESC, FIELD_UPDATED_AT),
            null);
    if (null == deployments || CollectionUtils.isEmpty(deployments.getData())) {
      detail.setLastDeployedAt("");
      detail.setLastDeployedBy("");
      detail.setLastDeployedStatus("");
      return;
    }
    // filter success or failed
    UnifiedAssetDto lastDeployMapper = null;
    UnifiedAssetDto deployAssetDto = deployments.getData().get(0);
    DeploymentFacet deploymentFacet = UnifiedAsset.getFacets(deployAssetDto, DeploymentFacet.class);
    ComponentTag componentTag =
        deploymentFacet.getComponentTags().stream()
            .filter(item -> item.getParentComponentKey().equals(detail.getTargetMapperKey()))
            .findFirst()
            .orElse(null);
    ComponentAPITargetFacets currentFacet =
        UnifiedAsset.getFacets(assetDto, ComponentAPITargetFacets.class);

    if (null != componentTag) {
      UnifiedAssetDto tagAsset = unifiedAssetService.findOne(componentTag.getTagId());
      ComponentTagFacet tagFacet = UnifiedAsset.getFacets(tagAsset, ComponentTagFacet.class);
      lastDeployMapper =
          tagFacet.getChildren().stream()
              .filter(
                  asset ->
                      asset
                          .getKind()
                          .equalsIgnoreCase(AssetKindEnum.COMPONENT_API_TARGET_MAPPER.getKind()))
              .findFirst()
              .orElse(null);
    }

    if (null != lastDeployMapper) {
      ComponentAPITargetFacets lastFacet =
          UnifiedAsset.getFacets(lastDeployMapper, ComponentAPITargetFacets.class);
      detail.setDiffWithStage(
          JsonDiffTool.diff(JsonToolkit.toJson(lastFacet), JsonToolkit.toJson(currentFacet)));
    }
    detail.setLastDeployedAt(deployAssetDto.getUpdatedAt());
    detail.setLastDeployedBy(
        deployAssetDto.getCreatedBy() == null
            ? UserContext.ANONYMOUS
            : deployAssetDto.getCreatedBy());
    // last completed one
    detail.setLastDeployedStatus(
        deployAssetDto.getMetadata().getStatus() == null
            ? ""
            : deployAssetDto.getMetadata().getStatus());
  }
}
