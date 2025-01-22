package com.consoleconnect.kraken.operator.controller.service;

import static com.consoleconnect.kraken.operator.core.toolkit.AssetsConstants.*;
import static com.consoleconnect.kraken.operator.core.toolkit.LabelConstants.*;

import com.consoleconnect.kraken.operator.auth.security.UserContext;
import com.consoleconnect.kraken.operator.controller.dto.*;
import com.consoleconnect.kraken.operator.controller.model.ComponentTag;
import com.consoleconnect.kraken.operator.controller.model.ComponentTagFacet;
import com.consoleconnect.kraken.operator.controller.model.DeploymentFacet;
import com.consoleconnect.kraken.operator.controller.model.Environment;
import com.consoleconnect.kraken.operator.controller.tools.VersionHelper;
import com.consoleconnect.kraken.operator.core.dto.ApiUseCaseDto;
import com.consoleconnect.kraken.operator.core.dto.Tuple2;
import com.consoleconnect.kraken.operator.core.dto.UnifiedAssetDto;
import com.consoleconnect.kraken.operator.core.entity.*;
import com.consoleconnect.kraken.operator.core.enums.*;
import com.consoleconnect.kraken.operator.core.event.IngestionDataResult;
import com.consoleconnect.kraken.operator.core.exception.KrakenException;
import com.consoleconnect.kraken.operator.core.model.*;
import com.consoleconnect.kraken.operator.core.model.facet.ComponentAPITargetFacets;
import com.consoleconnect.kraken.operator.core.repo.AssetFacetRepository;
import com.consoleconnect.kraken.operator.core.repo.EnvironmentClientRepository;
import com.consoleconnect.kraken.operator.core.repo.UnifiedAssetRepository;
import com.consoleconnect.kraken.operator.core.service.ApiUseCaseSelector;
import com.consoleconnect.kraken.operator.core.service.UnifiedAssetService;
import com.consoleconnect.kraken.operator.core.toolkit.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
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
public class ApiComponentService
    implements TargetMappingChecker, EndPointUsageCalculator, ApiUseCaseSelector {
  private static final Logger log = LoggerFactory.getLogger(ApiComponentService.class);
  public static final KrakenException ASSET_NOT_FOUND = KrakenException.notFound("asset not found");
  @Getter private final UnifiedAssetService unifiedAssetService;
  @Getter private final EnvironmentClientRepository environmentClientRepository;
  @Getter private final EnvironmentService environmentService;
  private final UnifiedAssetRepository unifiedAssetRepository;
  private final AssetFacetRepository assetFacetRepository;
  private final AppProperty appProperty;

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

  @Transactional
  public IngestionDataResult updateWorkflowTemplate(
      SaveWorkflowTemplateRequest template, String id, String updatedBy) {
    log.info("update mapping template id: {}", id);
    if (template.getWorkflowTemplate() == null) {
      throw KrakenException.badRequest("workflow template can not be null");
    }
    if (template.getMappingTemplate() == null) {
      throw KrakenException.badRequest("mapping template can not be null");
    }
    UnifiedAssetDto existingAsset = unifiedAssetService.findOne(id);
    validateMapper(template.getMappingTemplate(), existingAsset);

    ComponentAPITargetFacets facets =
        UnifiedAsset.getFacets(existingAsset, ComponentAPITargetFacets.class);
    if (facets.getWorkflow() == null
        || !Objects.equals(
            facets.getWorkflow().getKey(), template.getWorkflowTemplate().getMetadata().getKey())) {
      throw KrakenException.badRequest("workflow template key not match");
    }
    updateUnifiedAssetEntity(template.getMappingTemplate(), id, updatedBy);
    updateUnifiedAssetEntity(
        template.getWorkflowTemplate(), facets.getWorkflow().getKey(), updatedBy);
    return IngestionDataResult.of(HttpStatus.OK.value(), "success", null);
  }

  public UnifiedAssetEntity updateUnifiedAssetEntity(
      UnifiedAsset asset, String id, String updatedBy) {
    UnifiedAssetEntity assetEntity = unifiedAssetService.findOneByIdOrKey(id);
    assetEntity.setUpdatedBy(updatedBy);
    Set<AssetFacetEntity> facets = assetEntity.getFacets();

    List<AssetFacetEntity> endpointsFacets =
        facets.stream()
            .filter(
                v ->
                    Objects.equals(END_POINTS, v.getKey())
                        || Objects.equals(META_DATA, v.getKey())
                        || Objects.equals(PREPARATION_STAGE, v.getKey())
                        || Objects.equals(EXECUTION_STAGE, v.getKey())
                        || Objects.equals(VALIDATION_STAGE, v.getKey()))
            .toList();
    endpointsFacets.stream()
        .forEach(facet -> updateFacetsByKeyIfExist(facet.getKey(), asset, facet));
    if (asset.getMetadata() != null && asset.getMetadata().getLabels() != null) {
      Map<String, String> labels =
          assetEntity.getLabels() == null ? new HashMap<>() : assetEntity.getLabels();
      labels.putAll(asset.getMetadata().getLabels());
    }
    String version = assetEntity.getLabels().get(LABEL_VERSION_NAME);
    String subVersion = assetEntity.getLabels().get(LABEL_SUB_VERSION_NAME);
    if (StringUtils.isBlank(version)) {
      String mapperId = assetEntity.getId().toString();
      UnifiedAssetEntity tagEntity =
          unifiedAssetService
              .findLatest(mapperId, AssetKindEnum.COMPONENT_API_TARGET_MAPPER_TAG)
              .orElse(null);
      assetEntity.getLabels().put(LABEL_VERSION_NAME, VersionHelper.generateVersion(tagEntity));
      assetEntity.getLabels().put(LABEL_SUB_VERSION_NAME, NumberUtils.INTEGER_ONE.toString());
    } else {
      assetEntity
          .getLabels()
          .put(LABEL_SUB_VERSION_NAME, String.valueOf(Integer.parseInt(subVersion) + 1));
    }
    // add labels not deployed
    assetEntity
        .getLabels()
        .put(LabelConstants.LABEL_DEPLOYED_STATUS, VALUE_DEPLOYED_STATUS_NOT_DEPLOYED);
    assetEntity.setVersion(assetEntity.getVersion() + 1);
    unifiedAssetRepository.save(assetEntity);
    return assetEntity;
  }

  private void updateFacetsByKeyIfExist(String key, UnifiedAsset asset, AssetFacetEntity facet) {
    if (asset.getFacets().containsKey(key)) {
      facet.setPayload(asset.getFacets().get(key));
      assetFacetRepository.save(facet);
    }
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
          ComponentExpandDTO.TargetMappingDetail detail = getTargetMappingDetail(k, assetDto);
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

  @Transactional(readOnly = true)
  public List<ComponentExpandDTO> listAllApiUseCase() {
    List<UnifiedAssetDto> componentAssetList =
        unifiedAssetRepository
            .findByKindOrderByCreatedAtDesc(AssetKindEnum.COMPONENT_API.getKind())
            .stream()
            .map(t -> UnifiedAssetService.toAsset(t, true))
            .toList();
    List<UnifiedAssetDto> mapperAssetList =
        unifiedAssetService.findByKind(AssetKindEnum.COMPONENT_API_TARGET_MAPPER.getKind());
    return convert(appProperty.getQueryExcludeAssetKeys(), componentAssetList, mapperAssetList);
  }

  public List<ComponentExpandDTO> convert(
      List<String> queryExcludeAssets,
      List<UnifiedAssetDto> componentList,
      List<UnifiedAssetDto> mapperList) {
    Map<String, UnifiedAssetDto> mapper2ComponentMap = new HashMap<>();
    Map<String, UnifiedAssetDto> mapperAssetMap = new HashMap<>();
    Map<String, ComponentExpandDTO> componentExpandDTOMap = new HashMap<>();
    componentList.forEach(
        component ->
            component
                .getLinks()
                .forEach(link -> mapper2ComponentMap.put(link.getTargetAssetKey(), component)));
    mapperList.stream()
        .filter(entity -> !queryExcludeAssets.contains(entity.getMetadata().getKey()))
        .forEach(mapper -> mapperAssetMap.put(mapper.getMetadata().getKey(), mapper));
    mapperAssetMap.forEach(
        (k, assetDto) -> {
          ComponentExpandDTO.TargetMappingDetail detail = getTargetMappingDetail(k, assetDto);
          UnifiedAssetDto componentAsset = mapper2ComponentMap.get(detail.getTargetMapperKey());
          if (componentAsset == null) {
            return;
          }
          componentExpandDTOMap.putIfAbsent(
              componentAsset.getMetadata().getKey(), new ComponentExpandDTO());
          ComponentExpandDTO componentExpandDTO =
              componentExpandDTOMap.get(componentAsset.getMetadata().getKey());
          if (CollectionUtils.isEmpty(componentExpandDTO.getDetails())) {
            componentExpandDTO.setDetails(new ArrayList<>());
          }
          componentExpandDTO.setComponentName(componentAsset.getMetadata().getName());
          componentExpandDTO.setComponentKey(componentAsset.getMetadata().getKey());
          List<ComponentExpandDTO.TargetMappingDetail> details = componentExpandDTO.getDetails();
          details.add(detail);
        });
    return componentExpandDTOMap.values().stream()
        .filter(t -> !queryExcludeAssets.contains(t.getComponentKey()))
        .toList();
  }

  private ComponentExpandDTO.TargetMappingDetail getTargetMappingDetail(
      String mapperKey, UnifiedAssetDto mapperAsset) {
    fillMappingStatus(mapperAsset);
    ComponentAPITargetFacets mapperFacets =
        UnifiedAsset.getFacets(mapperAsset, ComponentAPITargetFacets.class);
    ComponentAPITargetFacets.Trigger trigger = mapperFacets.getTrigger();
    ComponentExpandDTO.TargetMappingDetail detail = new ComponentExpandDTO.TargetMappingDetail();
    if (Objects.nonNull(trigger)) {
      detail.setPath(trigger.getPath());
      detail.setMethod(trigger.getMethod());
      detail.setProductType(trigger.getProductType());
      detail.setActionType(trigger.getActionType());
      ComponentExpandDTO.MappingMatrix mappingMatrix = new ComponentExpandDTO.MappingMatrix();
      BeanUtils.copyProperties(trigger, mappingMatrix);
      detail.setMappingMatrix(mappingMatrix);
    }
    if (containsKeywords(mapperAsset.getMetadata().getKey())) {
      detail.setRequiredMapping(false);
    }
    detail.setDescription(mapperAsset.getMetadata().getDescription());
    detail.setTargetMapperKey(mapperKey);
    detail.setTargetKey(extractTargetKey(mapperKey));
    detail.setMappingStatus(mapperAsset.getMappingStatus());
    detail.setUpdatedAt(mapperAsset.getUpdatedAt());
    detail.setUpdatedBy(mapperAsset.getUpdatedBy());
    return detail;
  }

  public Optional<ApiUseCaseDto> findRelatedApiUse(String key) {
    return findRelatedApiUse(key, findApiUseCase());
  }

  public EndPointUsageDTO queryEndPointUsageDetail(String productId, String componentId) {
    unifiedAssetService.findOne(productId);
    UnifiedAssetDto unifiedAssetDto = unifiedAssetService.findOne(componentId);
    if (!AssetKindEnum.COMPONENT_API_TARGET_SPEC.getKind().equals(unifiedAssetDto.getKind())) {
      throw KrakenException.badRequest(
          "the component kind should be " + AssetKindEnum.COMPONENT_API_TARGET_SPEC.getKind());
    }
    List<Environment> environments =
        environmentService.search(productId, PageRequest.of(0, 10)).getData();
    return calculate(unifiedAssetDto, environments);
  }

  public ComponentProductCategoryDTO listProductCategories(String productId) {
    unifiedAssetService.findOne(productId);
    ComponentProductCategoryDTO componentProductCategoryDTO = new ComponentProductCategoryDTO();
    List<ComponentProductCategoryDTO.ComponentProductMetadata> metadataList =
        unifiedAssetService.findByKind(AssetKindEnum.COMPONENT_API.getKind()).stream()
            .map(UnifiedAssetDto::getMetadata)
            .filter(item -> item.getKey().contains(QUOTE_KEY) || item.getKey().contains(ORDER_KEY))
            .map(
                item -> {
                  ComponentProductCategoryDTO.ComponentProductMetadata metadata =
                      new ComponentProductCategoryDTO.ComponentProductMetadata();
                  metadata.setKey(item.getKey());
                  metadata.setId(item.getId());
                  return metadata;
                })
            .toList();
    componentProductCategoryDTO.setComponentProducts(metadataList);
    componentProductCategoryDTO.setProductCategories(
        Arrays.stream(ProductCategoryEnum.values())
            .map(
                item -> {
                  ComponentProductCategoryDTO.ProductCategoryMetaData metadata =
                      new ComponentProductCategoryDTO.ProductCategoryMetaData();
                  metadata.setKind(item.getKind());
                  metadata.setName(item.getName());
                  return metadata;
                })
            .toList());
    return componentProductCategoryDTO;
  }
}
