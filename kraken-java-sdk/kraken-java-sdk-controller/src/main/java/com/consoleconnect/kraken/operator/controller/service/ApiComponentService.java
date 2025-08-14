package com.consoleconnect.kraken.operator.controller.service;

import static com.consoleconnect.kraken.operator.core.service.UnifiedAssetService.toAsset;
import static com.consoleconnect.kraken.operator.core.toolkit.AssetsConstants.*;
import static com.consoleconnect.kraken.operator.core.toolkit.LabelConstants.*;

import com.consoleconnect.kraken.operator.auth.security.UserContext;
import com.consoleconnect.kraken.operator.controller.dto.ComponentExpandDTO;
import com.consoleconnect.kraken.operator.controller.dto.ComponentProductCategoryDTO;
import com.consoleconnect.kraken.operator.controller.dto.EndPointUsageDTO;
import com.consoleconnect.kraken.operator.controller.dto.SaveWorkflowTemplateRequest;
import com.consoleconnect.kraken.operator.controller.entity.ApiAvailabilityChangeHistoryEntity;
import com.consoleconnect.kraken.operator.controller.mapper.ApiAvailabilityMapper;
import com.consoleconnect.kraken.operator.controller.model.*;
import com.consoleconnect.kraken.operator.controller.repo.ApiAvailabilityChangeHistoryRepository;
import com.consoleconnect.kraken.operator.controller.tools.VersionHelper;
import com.consoleconnect.kraken.operator.core.dto.ApiUseCaseDto;
import com.consoleconnect.kraken.operator.core.dto.Tuple2;
import com.consoleconnect.kraken.operator.core.dto.UnifiedAssetDto;
import com.consoleconnect.kraken.operator.core.entity.AssetFacetEntity;
import com.consoleconnect.kraken.operator.core.entity.UnifiedAssetEntity;
import com.consoleconnect.kraken.operator.core.enums.*;
import com.consoleconnect.kraken.operator.core.event.IngestionDataResult;
import com.consoleconnect.kraken.operator.core.exception.KrakenException;
import com.consoleconnect.kraken.operator.core.model.*;
import com.consoleconnect.kraken.operator.core.model.facet.ComponentAPIAvailabilityFacets;
import com.consoleconnect.kraken.operator.core.model.facet.ComponentAPIFacets;
import com.consoleconnect.kraken.operator.core.model.facet.ComponentAPISpecFacets;
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
import org.apache.commons.collections4.MapUtils;
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
  public static final String NOT_FOUND_SPEC = "not found spec";
  private static final String API_AVAILABILITY_KEY = "mef.sonata.api-availability";
  public static final String MEF_SONATA = "mef.sonata";
  @Getter private final UnifiedAssetService unifiedAssetService;
  @Getter private final EnvironmentClientRepository environmentClientRepository;
  @Getter private final EnvironmentService environmentService;
  private final UnifiedAssetRepository unifiedAssetRepository;
  private final AssetFacetRepository assetFacetRepository;
  private final ApiAvailabilityChangeHistoryRepository changeHistoryRepository;
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
                        || Objects.equals(WORKFLOW, v.getKey())
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

  public List<StandardComponentInfo> queryForStandardMappingInfo(String productType) {
    List<StandardComponentInfo> result = new ArrayList<>();
    Paging<UnifiedAssetDto> apiAssetsPage =
        unifiedAssetService.search(
            null, "kraken.component.api", true, null, PageRequest.of(0, Integer.MAX_VALUE));
    if (apiAssetsPage == null || CollectionUtils.isEmpty(apiAssetsPage.getData())) {
      return result;
    }
    Paging<UnifiedAssetDto> specAssetsPage =
        unifiedAssetService.search(
            null, "kraken.component.api-spec", true, null, PageRequest.of(0, Integer.MAX_VALUE));
    if (specAssetsPage == null || CollectionUtils.isEmpty(apiAssetsPage.getData())) {
      return result;
    }
    unifiedAssetService.fillSupportedProductType(apiAssetsPage.getData());
    for (UnifiedAsset asset : apiAssetsPage.getData()) {
      ComponentAPIFacets facets = UnifiedAsset.getFacets(asset, ComponentAPIFacets.class);
      if (facets.getSupportedProductTypesAndActions() == null) {
        continue;
      }
      List<ComponentAPIFacets.SupportedProductAndAction> list =
          facets.getSupportedProductTypesAndActions().stream()
              .filter(
                  v ->
                      v.getProductTypes() != null
                          && v.getProductTypes().contains(productType.toUpperCase()))
              .toList();
      if (CollectionUtils.isEmpty(list)) {
        continue;
      }
      UnifiedAssetDto specAsset = findSpec(asset, specAssetsPage);
      result.add(constructStandardComponent(asset, list, specAsset, facets));
    }
    return result;
  }

  private static StandardComponentInfo constructStandardComponent(
      UnifiedAsset asset,
      List<ComponentAPIFacets.SupportedProductAndAction> list,
      UnifiedAssetDto specAsset,
      ComponentAPIFacets facets) {
    StandardComponentInfo info = new StandardComponentInfo();
    info.setComponentKey(asset.getMetadata().getKey());
    info.setName(asset.getMetadata().getName());
    info.setLabels(asset.getMetadata().getLabels());
    info.setSupportedProductTypes(
        CollectionUtils.isEmpty(list) ? null : list.get(0).getProductTypes());
    info.setLogo(specAsset.getMetadata() == null ? null : specAsset.getMetadata().getLogo());
    if (MapUtils.isEmpty(specAsset.getFacets())) {
      return info;
    }
    ComponentAPISpecFacets specFacet =
        UnifiedAsset.getFacets(specAsset, ComponentAPISpecFacets.class);
    info.setBaseSpec(specFacet.getBaseSpec());
    info.setCustomizedSpec(specFacet.getCustomizedSpec());
    info.setApiCount(facets.getMappings().size());
    return info;
  }

  private static UnifiedAssetDto findSpec(
      UnifiedAsset asset, Paging<UnifiedAssetDto> specAssetsPage) {
    AssetLink specLink =
        asset.getLinks().stream()
            .filter(
                assetLink ->
                    Objects.equals(
                        assetLink.getRelationship(),
                        AssetLinkKindEnum.IMPLEMENTATION_STANDARD_API_SPEC.getKind()))
            .findFirst()
            .orElse(null);
    return specAssetsPage.getData().stream()
        .filter(
            assetDto ->
                Objects.equals(assetDto.getMetadata().getKey(), specLink.getTargetAssetKey()))
        .findFirst()
        .orElse(new UnifiedAssetDto());
  }

  public List<String> listProductType() {
    return appProperty.getProductTypes();
  }

  @Transactional
  public ComponentExpandDTO queryComponentExpandInfo(
      String productId, String componentId, String envId, String productType) {
    unifiedAssetService.findOne(productId);
    UnifiedAssetDto unifiedAssetDto = unifiedAssetService.findOne(componentId);
    List<AssetLink> links = unifiedAssetDto.getLinks();
    if (CollectionUtils.isEmpty(links)) {
      throw KrakenException.badRequest(
          "The current componentId has no links, componentId:" + componentId);
    }
    List<String> queryExcludeAssets = appProperty.getQueryExcludeAssetKeys();
    List<String> noRequiredMappingKeys = appProperty.getNoRequiredMappingKeys();
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
            .collect(Collectors.toMap(UnifiedAssetEntity::getKey, t -> toAsset(t, true)));

    List<ComponentExpandDTO.TargetMappingDetail> details = new ArrayList<>();
    assetEntityMap.forEach(
        (k, assetDto) -> {
          ComponentExpandDTO.TargetMappingDetail detail =
              getTargetMappingDetail(k, assetDto, noRequiredMappingKeys);
          if (StringUtils.isNotBlank(productType)
              && !detail.getProductType().equalsIgnoreCase(productType)) {
            return;
          }
          detail.setOrderBy(
              appProperty.getApiTargetMapperOrderBy().getOrDefault(k, "<1000, 1000>"));
          mergeLastDeployment(detail, assetDto, envId);
          ComponentAPITargetFacets currentFacet =
              UnifiedAsset.getFacets(assetDto, ComponentAPITargetFacets.class);
          detail.setSupportedCase(currentFacet.getSupportedCase().getType());
          detail.setRunningMappingType(checkRunningMappingType(currentFacet));
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

  private String checkRunningMappingType(ComponentAPITargetFacets currentFacet) {
    String type = "";
    if (currentFacet.getWorkflow() != null && currentFacet.getWorkflow().isEnabled()) {
      type = SupportedCaseEnum.ONE_TO_MANY.name();
    } else if (CollectionUtils.isNotEmpty(currentFacet.getEndpoints())
        && StringUtils.isNotBlank(currentFacet.getEndpoints().get(0).getPath())) {
      type = SupportedCaseEnum.ONE_TO_ONE.name();
    }
    return type;
  }

  @Transactional(readOnly = true)
  public List<ComponentExpandDTO> listAllApiUseCase() {
    List<UnifiedAssetDto> componentAssetList =
        unifiedAssetRepository
            .findByKindOrderByCreatedAtDesc(AssetKindEnum.COMPONENT_API.getKind())
            .stream()
            .map(t -> toAsset(t, true))
            .toList();
    List<UnifiedAssetDto> mapperAssetList =
        unifiedAssetService.findByKind(AssetKindEnum.COMPONENT_API_TARGET_MAPPER.getKind());
    return convert(
        appProperty.getQueryExcludeAssetKeys(),
        appProperty.getNoRequiredMappingKeys(),
        componentAssetList,
        mapperAssetList);
  }

  public List<ComponentExpandDTO> convert(
      List<String> queryExcludeAssets,
      List<String> noRequiredMappingKeys,
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
          ComponentExpandDTO.TargetMappingDetail detail =
              getTargetMappingDetail(k, assetDto, noRequiredMappingKeys);
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
      String mapperKey, UnifiedAssetDto mapperAsset, List<String> noRequiredMappingKeys) {
    fillMappingStatus(mapperAsset, noRequiredMappingKeys);
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
    if (containsKeywords(noRequiredMappingKeys, mapperAsset.getMetadata().getKey())) {
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
        Arrays.stream(ParentProductTypeEnum.values())
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

  @Transactional(readOnly = true)
  public List<String> findRelatedAssetKeys(String key, ApiUseCaseDto usecase) {
    UnifiedAssetEntity mapperEntity = unifiedAssetService.findOneByIdOrKey(key);
    boolean workflowEnabled = isWorkflowEnabled(mapperEntity);
    return usecase.membersDeployable(workflowEnabled);
  }

  private boolean isWorkflowEnabled(UnifiedAssetEntity mapperEntity) {
    UnifiedAssetDto mapperAsset = toAsset(mapperEntity, true);
    ComponentAPITargetFacets mapperFacets =
        UnifiedAsset.getFacets(mapperAsset, ComponentAPITargetFacets.class);
    return mapperFacets.getWorkflow() != null && mapperFacets.getWorkflow().isEnabled();
  }

  @Transactional
  public IngestionDataResult updateApiAvailability(
      UpdateAipAvailabilityRequest request, String userId) {
    if (request.getMapperKey() == null || request.getEnvName() == null) {
      throw KrakenException.badRequest("mapperKey and envName can not be null");
    }
    Optional<UnifiedAssetEntity> assetOpt =
        unifiedAssetRepository.findOneByKey(API_AVAILABILITY_KEY);
    UnifiedAsset asset = new UnifiedAsset();
    ComponentAPIAvailabilityFacets facets = new ComponentAPIAvailabilityFacets();
    if (assetOpt.isEmpty()) {
      asset.setKind(AssetKindEnum.COMPONENT_API_AVAILABILITY.getKind());
      Metadata metadata = new Metadata();
      metadata.setKey(API_AVAILABILITY_KEY);
      metadata.setVersion(1);
      asset.setMetadata(metadata);
    } else {
      UnifiedAssetEntity assetEntity = assetOpt.get();
      asset = toAsset(assetEntity, true);
      facets = UnifiedAsset.getFacets(asset, ComponentAPIAvailabilityFacets.class);
    }
    SyncMetadata syncMetadata = new SyncMetadata();
    syncMetadata.setSyncedAt(DateTime.nowInUTC().toString());
    syncMetadata.setSyncedBy(userId);
    if (EnvNameEnum.STAGE.name().equalsIgnoreCase(request.getEnvName())) {
      performDisableList(
          facets.getStageDisableApiList(), request.getMapperKey(), request.isDisabled());
    } else if (EnvNameEnum.PRODUCTION.name().equalsIgnoreCase(request.getEnvName())) {
      performDisableList(
          facets.getProdDisableApiList(), request.getMapperKey(), request.isDisabled());
    }
    asset.setFacets(JsonToolkit.convertToMap(facets));
    recordChangeHistory(request, userId);
    return unifiedAssetService.syncAsset(MEF_SONATA, asset, syncMetadata, true);
  }

  private void recordChangeHistory(UpdateAipAvailabilityRequest request, String userId) {
    // record history
    ApiAvailabilityChangeHistoryEntity changeHistory = new ApiAvailabilityChangeHistoryEntity();
    changeHistory.setMapperKey(request.getMapperKey());
    changeHistory.setEnv(request.getEnvName());
    changeHistory.setVersion(request.getVersion());
    changeHistory.setUpdatedBy(userId);
    changeHistory.setAvailable(!request.isDisabled());
    changeHistoryRepository.save(changeHistory);
  }

  public List<ApiAvailabilityChangeHistory> getApiAvailabilityChangeHistory(
      String mapperKey, String env) {
    return changeHistoryRepository
        .findAllByMapperKeyAndEnvOrderByCreatedAtDesc(mapperKey, env)
        .stream()
        .map(ApiAvailabilityMapper.INSTANCE::toChangeHistory)
        .toList();
  }

  private void performDisableList(Set<String> set, String key, boolean disabled) {
    if (disabled) {
      set.add(key);
    } else {
      set.remove(key);
    }
  }
}
