package com.consoleconnect.kraken.operator.core.service;

import static com.consoleconnect.kraken.operator.core.enums.AssetKindEnum.COMPONENT_API_TARGET_MAPPER;
import static com.consoleconnect.kraken.operator.core.toolkit.LabelConstants.FUNCTION_JSON_EXTRACT_PATH_TEXT;

import com.consoleconnect.kraken.operator.core.dto.AssetLinkDto;
import com.consoleconnect.kraken.operator.core.dto.Tuple2;
import com.consoleconnect.kraken.operator.core.dto.UnifiedAssetDto;
import com.consoleconnect.kraken.operator.core.entity.AssetFacetEntity;
import com.consoleconnect.kraken.operator.core.entity.AssetLinkEntity;
import com.consoleconnect.kraken.operator.core.entity.UnifiedAssetEntity;
import com.consoleconnect.kraken.operator.core.enums.AssetKindEnum;
import com.consoleconnect.kraken.operator.core.event.IngestionDataResult;
import com.consoleconnect.kraken.operator.core.exception.KrakenException;
import com.consoleconnect.kraken.operator.core.mapper.AssetMapper;
import com.consoleconnect.kraken.operator.core.mapper.FacetsMapper;
import com.consoleconnect.kraken.operator.core.model.*;
import com.consoleconnect.kraken.operator.core.model.facet.ComponentAPITargetFacets;
import com.consoleconnect.kraken.operator.core.repo.AssetFacetRepository;
import com.consoleconnect.kraken.operator.core.repo.AssetLinkRepository;
import com.consoleconnect.kraken.operator.core.repo.UnifiedAssetRepository;
import com.consoleconnect.kraken.operator.core.toolkit.*;
import com.fasterxml.jackson.core.type.TypeReference;
import jakarta.persistence.criteria.*;
import java.util.*;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@AllArgsConstructor
public class UnifiedAssetService implements UUIDWrapper {

  private static final String DEFAULT_ORDER_SEQ = "1000";
  private static final String MAPPER_REQUEST = "request";
  private static final String MAPPER_RESPONSE = "response";
  private static final String PARENT_PRODUCT_TYPE_KEY = "parentProductType";

  private static final Set<String> API_KINDS =
      Set.of(AssetKindEnum.COMPONENT_API_SPEC.getKind(), AssetKindEnum.COMPONENT_API.getKind());

  private final UnifiedAssetRepository assetRepository;
  private final AssetFacetRepository assetFacetRepository;
  private final AssetLinkRepository assetLinkRepository;
  private final AppProperty appProperty;
  private final MergeService mergeService;

  public static PageRequest getSearchPageRequest() {
    return getSearchPageRequest(PagingHelper.DEFAULT_PAGE, PagingHelper.DEFAULT_SIZE, null, null);
  }

  public static PageRequest getSearchPageRequest(int page, int size) {
    return getSearchPageRequest(page, size, null, null);
  }

  public static PageRequest getSearchPageRequest(
      int page, int size, Sort.Direction direction, String orderBy) {
    final String key = "key";
    final String name = "name";
    final String createAt = "createdAt";
    final String updatedAt = "updatedAt";

    final Set<String> fieldList = Set.of(key, name, createAt, updatedAt);
    if (orderBy == null || !fieldList.contains(orderBy)) {
      orderBy = createAt;
    }

    if (direction == null) {
      direction = Sort.Direction.DESC;
    }

    return PageRequest.of(page, size, direction, orderBy);
  }

  @Transactional(readOnly = true)
  public Paging<UnifiedAssetDto> search(
      String parentId,
      String kind,
      boolean facetIncluded,
      String q,
      String parentProductType,
      PageRequest pageRequest) {
    return performSearch(parentId, kind, facetIncluded, q, parentProductType, pageRequest);
  }

  @Transactional(readOnly = true)
  public Paging<UnifiedAssetDto> search(
      String parentId, String kind, boolean facetIncluded, String q, PageRequest pageRequest) {
    return performSearch(parentId, kind, facetIncluded, q, null, pageRequest);
  }

  private Paging<UnifiedAssetDto> performSearch(
      String parentId,
      String kind,
      boolean facetIncluded,
      String q,
      String parentProductType,
      PageRequest pageRequest) {
    log.info(
        "search asset, parentId: {}, kind: {}, q: {}, parentProductType:{}, pageRequest: {}",
        parentId,
        kind,
        q,
        parentProductType,
        pageRequest);
    if (parentId != null) {
      parentId = findOneByIdOrKey(parentId).getId().toString();
    }
    List<UnifiedAssetEntity> data =
        assetRepository.searchWithoutPagination(parentId, kind, StringUtils.lowerCase(q));
    if (appProperty.getQueryExcludeAssetKinds().contains(kind)) {
      data = filterExcludedAssets(data);
    }
    if (StringUtils.isNotBlank(kind) && API_KINDS.contains(kind)) {
      data = sortData(data, kind);
      if (facetIncluded && StringUtils.isNotBlank(parentProductType)) {
        List<UnifiedAssetDto> assetDtoList = filterByParentProductType(data, parentProductType);
        Page<UnifiedAssetDto> pagedData = PagingHelper.paginateList(assetDtoList, pageRequest);
        return PagingHelper.toPaging(pagedData, x -> x);
      }
    }
    Page<UnifiedAssetEntity> pagedData = PagingHelper.paginateList(data, pageRequest);
    return PagingHelper.toPaging(pagedData, entity -> toAsset(entity, facetIncluded));
  }

  private List<UnifiedAssetEntity> filterExcludedAssets(List<UnifiedAssetEntity> data) {
    return data.stream()
        .filter(entity -> !appProperty.getQueryExcludeAssetKeys().contains(entity.getKey()))
        .toList();
  }

  private List<UnifiedAssetEntity> sortData(List<UnifiedAssetEntity> data, String kind) {
    Map<String, Map<String, String>> orderByMap =
        Map.of(
            AssetKindEnum.COMPONENT_API_SPEC.getKind(), appProperty.getApiSpecOrderBy(),
            AssetKindEnum.COMPONENT_API.getKind(), appProperty.getApiOrderBy());
    Map<String, String> orderBy = orderByMap.getOrDefault(kind, Collections.emptyMap());
    return data.stream()
        .sorted(Comparator.comparing(t -> orderBy.getOrDefault(t.getKey(), DEFAULT_ORDER_SEQ)))
        .toList();
  }

  private List<UnifiedAssetDto> filterByParentProductType(
      List<UnifiedAssetEntity> data, String parentProductType) {
    return data.stream()
        .map(entity -> toAsset(entity, true))
        .filter(item -> parentProductType.equals(getParentProductType(item)))
        .toList();
  }

  private String getParentProductType(UnifiedAssetDto item) {
    return item.getMetadata().getLabels().getOrDefault(PARENT_PRODUCT_TYPE_KEY, null);
  }

  @Transactional(readOnly = true)
  public UnifiedAssetDto findOne(String id) {
    log.info("find asset, id: {}", id);
    UnifiedAssetEntity entity = findOneByIdOrKey(id);
    return toAsset(entity, true);
  }

  public UnifiedAssetEntity findOneByIdOrKey(String idOrKey) {
    Optional<UUID> uuidOptional = getUUID(idOrKey);
    if (uuidOptional.isPresent()) {
      return assetRepository
          .findById(uuidOptional.get())
          .orElseThrow(() -> KrakenException.notFound("Asset not found,id=" + idOrKey));
    } else {
      return assetRepository
          .findOneByKey(idOrKey)
          .orElseThrow(() -> KrakenException.notFound("Asset not found,key=" + idOrKey));
    }
  }

  @Transactional(readOnly = true)
  public Paging<AssetLinkDto> findAssetLinks(
      String assetId, String relationship, int page, int size) {
    UnifiedAssetEntity assetEntity = this.findOneByIdOrKey(assetId);
    var linkedList =
        assetEntity.getLinks().stream()
            .filter(x -> relationship == null || relationship.equalsIgnoreCase(x.getRelationship()))
            .toList();
    List<UnifiedAssetEntity> linkedToAssetList =
        this.assetRepository.findAllByKeyIn(
            linkedList.stream().map(AssetLinkEntity::getTargetAssetKey).distinct().toList());
    var otherLinkedList = this.getLinkedList(linkedList, linkedToAssetList, null);
    return PagingHelper.toPage(otherLinkedList, page, size);
  }

  @Transactional(readOnly = true)
  public List<UnifiedAssetEntity> findAllByIdIn(List<String> ids) {
    return assetRepository.findAllByIdIn(ids.stream().map(UUID::fromString).toList());
  }

  public List<AssetLinkDto> getLinkedList(
      List<AssetLinkEntity> linkedList,
      List<UnifiedAssetEntity> linkedToAssetList,
      String selfKey) {

    return linkedList.stream()
        .map(
            x -> {
              AssetLinkDto assetLinkDto = AssetMapper.INSTANCE.toAssetLinkDto(x);
              assetLinkDto.setTargetAsset(
                  linkedToAssetList.stream()
                      .filter(
                          y -> {
                            if (StringUtils.isEmpty(selfKey)) {
                              return y.getKey().equals(x.getTargetAssetKey());
                            }
                            return selfKey.equals(x.getTargetAssetKey())
                                && x.getAsset().getId().equals(y.getId());
                          })
                      .findFirst()
                      .map(y -> toAsset(y, true))
                      .orElse(null));
              return assetLinkDto;
            })
        .toList();
  }

  @Transactional
  public void deleteOne(String id) {
    log.info("deleting asset,  id: {}", id);
    UnifiedAssetEntity entity = findOneByIdOrKey(id);
    assetRepository
        .findAllByParentId(entity.getId().toString())
        .forEach(
            asset -> {
              log.info("delete child asset, assetId: {}", asset.getId());
              assetRepository.delete(asset);
            });
    assetRepository.delete(entity);
    log.info("delete asset, assetId: {} done", id);
  }

  public static UnifiedAssetDto toAsset(UnifiedAssetEntity entity, boolean facetIncluded) {
    UnifiedAssetDto asset = AssetMapper.INSTANCE.toAsset(entity);
    Metadata metadata = AssetMapper.INSTANCE.toMetadata(entity);
    asset.setMetadata(metadata);
    if (facetIncluded) {
      asset.setFacets(new HashMap<>());
      entity
          .getFacets()
          .forEach(facet -> asset.getFacets().put(facet.getKey(), facet.getPayload()));
    }
    return asset;
  }

  @Transactional
  public IngestionDataResult syncAsset(
      String parentKey, UnifiedAsset data, SyncMetadata syncMetadata, boolean enforceSync) {
    log.info(
        "sync asset, kind: {}, key: {}, parentKey:{},fullPath:{}",
        data.getKind(),
        data.getMetadata().getKey(),
        parentKey,
        syncMetadata.getFullPath());
    Optional<UnifiedAssetEntity> entityOptional =
        assetRepository.findOneByKey(data.getMetadata().getKey());

    if (!enforceSync && entityOptional.isPresent()) {
      UnifiedAssetEntity entity = entityOptional.get();
      if (entity.getVersion() == null || entity.getVersion() < data.getMetadata().getVersion()) {
        log.info(
            "existing asset version is lower than the incoming asset, assetKey: {}, version: {}, incomingVersion: {}",
            entity.getKey(),
            entity.getVersion(),
            data.getMetadata().getVersion());
      } else {
        log.info(
            "Incoming asset version is not higher than existing one, assetKey: {}, existing version: {}, incomingVersion: {}",
            entity.getKey(),
            entity.getVersion(),
            data.getMetadata().getVersion());
        return IngestionDataResult.of(
            HttpStatus.CONFLICT.value(), "Asset version is lower", entity);
      }
    }
    if (enforceSync && entityOptional.isPresent()) {
      UnifiedAssetEntity entity = entityOptional.get();
      data.getMetadata().setVersion(entity.getVersion() + 1);
    }

    final String parentId = getParentId(parentKey);
    UnifiedAssetEntity assetEntity =
        entityOptional
            .map(entity -> updateAssetEntity(parentId, entity, data, syncMetadata))
            .orElseGet(() -> createAssetEntity(parentId, data, syncMetadata));

    log.info("syncing asset facets, assetId: {}", assetEntity.getKey());

    if (Objects.equals(assetEntity.getKind(), COMPONENT_API_TARGET_MAPPER.getKind())
        && entityOptional.isPresent()) {
      data.setFacets(mergeService.mergeFacets(entityOptional.get(), data.getFacets()));
    }
    if (data.getFacets() != null) {
      syncFacets(assetEntity, data.getFacets());
    }

    log.info("syncing asset links, assetId: {}", assetEntity.getKey());
    if (data.getLinks() != null) {
      syncAssetLinks(assetEntity, data.getLinks());
    }

    assetEntity = assetRepository.save(assetEntity);

    log.info(
        "deleting not exist children, assetKey: {}, assetId: {}",
        assetEntity.getKey(),
        assetEntity.getId());
    removeNotExistingChildren(assetEntity.getId().toString());

    log.info("asset synced, assetKey:{}, assetId: {}", assetEntity.getKey(), assetEntity.getId());
    return IngestionDataResult.of(HttpStatus.OK.value(), "Asset synced", assetEntity);
  }

  private String getParentId(String parentKey) {
    return parentKey == null ? null : findOneByIdOrKey(parentKey).getId().toString();
  }

  public static Map<String, Object> mergeFacets(
      UnifiedAssetEntity unifiedAssetEntity, Map<String, Object> facetsUpdated) {
    UnifiedAssetDto assetDto = UnifiedAssetService.toAsset(unifiedAssetEntity, true);
    ComponentAPITargetFacets existFacets =
        UnifiedAsset.getFacets(assetDto, ComponentAPITargetFacets.class);
    ComponentAPITargetFacets newFacets =
        JsonToolkit.fromJson(JsonToolkit.toJson(facetsUpdated), ComponentAPITargetFacets.class);
    return mergeFacets(existFacets, newFacets);
  }

  public static Map<String, Object> mergeFacets(
      ComponentAPITargetFacets facetsOld, ComponentAPITargetFacets facetsNew) {
    ComponentAPITargetFacets.Endpoint endpointOld = facetsOld.getEndpoints().get(0);
    ComponentAPITargetFacets.Endpoint endpointNew = facetsNew.getEndpoints().get(0);
    List<PathRule> pathRules =
        (Objects.isNull(endpointNew) || Objects.isNull(endpointNew.getMappers()))
            ? new ArrayList<>()
            : endpointNew.getMappers().getPathRules();
    FacetsMapper.INSTANCE.toEndpoint(endpointOld, endpointNew);

    Map<String, Map<String, ComponentAPITargetFacets.Mapper>> mapperOldMap =
        constructMapperMap(endpointOld);
    Map<String, Map<String, ComponentAPITargetFacets.Mapper>> mapperNewMap =
        constructMapperMap(endpointNew);
    mergeMappers(mapperOldMap, mapperNewMap);

    Map<String, List<ComponentAPITargetFacets.Mapper>> finalMap = toFinalMapper(mapperNewMap);
    ComponentAPITargetFacets.Mappers mappers = new ComponentAPITargetFacets.Mappers();
    mappers.setResponse(finalMap.getOrDefault(MAPPER_RESPONSE, Collections.emptyList()));
    mappers.setRequest(finalMap.getOrDefault(MAPPER_REQUEST, Collections.emptyList()));
    mappers.setPathRules(pathRules);
    if (Objects.nonNull(endpointNew)) {
      endpointNew.setMappers(mappers);
    }
    return JsonToolkit.fromJson(JsonToolkit.toJson(facetsNew), new TypeReference<>() {});
  }

  public static void mergeMappers(
      Map<String, Map<String, ComponentAPITargetFacets.Mapper>> mapperMapOld,
      Map<String, Map<String, ComponentAPITargetFacets.Mapper>> mapperMapNew) {
    mapperMapOld.forEach(
        (name, value) -> {
          Map.Entry<String, ComponentAPITargetFacets.Mapper> existMapperEntry =
              value.entrySet().iterator().next();
          Optional<ComponentAPITargetFacets.Mapper> copyTo =
              findMapper(mapperMapNew, name, existMapperEntry.getKey());
          if (copyTo.isPresent()) {
            mergeMapper(name, existMapperEntry, mapperMapNew, copyTo.get());
          } else {
            deepCopyMapper(name, existMapperEntry, mapperMapNew);
          }
        });
  }

  public static Map<String, List<ComponentAPITargetFacets.Mapper>> toFinalMapper(
      Map<String, Map<String, ComponentAPITargetFacets.Mapper>> newMapperMap) {
    return newMapperMap.values().stream()
        .collect(
            Collectors.groupingBy(
                map -> map.entrySet().iterator().next().getKey(),
                Collectors.mapping(
                    nested -> nested.entrySet().iterator().next().getValue(),
                    Collectors.toList())));
  }

  public static Map<String, Map<String, ComponentAPITargetFacets.Mapper>> constructMapperMap(
      ComponentAPITargetFacets.Endpoint endpoint) {
    if (Objects.isNull(endpoint) || Objects.isNull(endpoint.getMappers())) {
      return new LinkedHashMap<>();
    }

    Map<String, Map<String, ComponentAPITargetFacets.Mapper>> mapperMap = new LinkedHashMap<>();
    Set<ComponentAPITargetFacets.Mapper> seenMappers = new HashSet<>();
    removeDuplicatedNodes(
        endpoint.getMappers().getRequest(), mapperMap, seenMappers, MAPPER_REQUEST);
    removeDuplicatedNodes(
        endpoint.getMappers().getResponse(), mapperMap, seenMappers, MAPPER_RESPONSE);

    return mapperMap;
  }

  public static void removeDuplicatedNodes(
      List<ComponentAPITargetFacets.Mapper> mappers,
      Map<String, Map<String, ComponentAPITargetFacets.Mapper>> mapperMap,
      Set<ComponentAPITargetFacets.Mapper> seenMappers,
      String mapperSection) {
    if (CollectionUtils.isNotEmpty(mappers)) {
      for (ComponentAPITargetFacets.Mapper mapper : mappers) {
        if (seenMappers.contains(mapper)) {
          continue;
        }
        String key = mapper.getKey(mapperSection);
        mapperMap.computeIfAbsent(key, k -> new LinkedHashMap<>()).put(mapperSection, mapper);
        seenMappers.add(mapper);
      }
    }
  }

  public void removeNotExistingChildren(String assetId) {
    // delete not existing children
    assetRepository
        .findAllByParentId(assetId)
        .forEach(
            child -> {
              if (child.getSyncMetadata() == null) {
                log.info("Deleting child asset {}", child.getKey());
                assetRepository.delete(child);
              } else {
                log.info("Child asset {} is still valid", child.getKey());
              }
            });
  }

  private static void mergeMapper(
      String name,
      Map.Entry<String, ComponentAPITargetFacets.Mapper> copyFrom,
      Map<String, Map<String, ComponentAPITargetFacets.Mapper>> mapperMapNew,
      ComponentAPITargetFacets.Mapper copyTo) {
    String mapperSection = copyFrom.getKey();
    if (isCustomizedAndConfigured(copyFrom)) {
      copyFrom.getValue().setCustomizedField(copyTo.getCustomizedField());
      copyFrom.getValue().setName(copyTo.getName());
      mapperMapNew.put(name, new HashMap<>(Map.of(mapperSection, copyFrom.getValue())));
    } else if (isSystemAndConfigured(copyFrom)) {
      shallowCopyMapper(copyFrom.getValue(), copyTo, mapperSection);
    }
  }

  private static void shallowCopyMapper(
      ComponentAPITargetFacets.Mapper copyFrom,
      ComponentAPITargetFacets.Mapper copyTo,
      String mapperSection) {
    if (Objects.equals(MAPPER_REQUEST, mapperSection)) {
      FacetsMapper.INSTANCE.toRequestMapper(copyFrom, copyTo);
    } else {
      FacetsMapper.INSTANCE.toResponseMapper(copyFrom, copyTo);
    }
  }

  private static void deepCopyMapper(
      String name,
      Map.Entry<String, ComponentAPITargetFacets.Mapper> copyFrom,
      Map<String, Map<String, ComponentAPITargetFacets.Mapper>> mapperMapNew) {
    String mapperSection = copyFrom.getKey();
    if (isCustomizedAndConfigured(copyFrom)) {
      mapperMapNew.put(name, new HashMap<>(Map.of(mapperSection, copyFrom.getValue())));
    } else if (isSystemAndConfigured(copyFrom)) {
      copyFrom.getValue().setCustomizedField(Boolean.TRUE);
      mapperMapNew.put(name, new HashMap<>(Map.of(mapperSection, copyFrom.getValue())));
    }
  }

  private static Optional<ComponentAPITargetFacets.Mapper> findMapper(
      Map<String, Map<String, ComponentAPITargetFacets.Mapper>> mapperMap,
      String name,
      String mapperSection) {
    return Optional.ofNullable(mapperMap.getOrDefault(name, null))
        .map(x -> x.getOrDefault(mapperSection, null));
  }

  private static boolean isCustomizedAndConfigured(
      Map.Entry<String, ComponentAPITargetFacets.Mapper> mapper) {
    if (!isCustomizedMapping(mapper)) {
      return false;
    }
    if (Objects.equals(MAPPER_REQUEST, mapper.getKey())) {
      return Strings.isNotBlank(mapper.getValue().getTarget());
    } else {
      return Strings.isNotBlank(mapper.getValue().getSource());
    }
  }

  private static boolean isSystemAndConfigured(
      Map.Entry<String, ComponentAPITargetFacets.Mapper> mapper) {
    if (!isSystemMapping(mapper)) {
      return false;
    }
    if (Objects.equals(MAPPER_REQUEST, mapper.getKey())) {
      return Strings.isNotBlank(mapper.getValue().getTarget());
    } else {
      return Strings.isNotBlank(mapper.getValue().getSource());
    }
  }

  private static boolean isSystemMapping(
      Map.Entry<String, ComponentAPITargetFacets.Mapper> mapper) {
    return !isCustomizedMapping(mapper);
  }

  private static boolean isCustomizedMapping(
      Map.Entry<String, ComponentAPITargetFacets.Mapper> mapper) {
    return Objects.equals(Boolean.TRUE, mapper.getValue().getCustomizedField());
  }

  private UnifiedAssetEntity createAssetEntity(
      String parentId, UnifiedAsset data, SyncMetadata syncMetadata) {
    UnifiedAssetEntity entity = new UnifiedAssetEntity();
    entity.setKind(data.getKind());
    entity.setApiVersion(data.getApiVersion());
    entity.setKey(data.getMetadata().getKey());
    entity.setStatus(data.getMetadata().getStatus());
    entity.setParentId(parentId);
    entity.setMapperKey(data.getMetadata().getMapperKey());
    copyAssetEntity(entity, data, syncMetadata);
    return assetRepository.save(entity);
  }

  private UnifiedAssetEntity updateAssetEntity(
      String parentId, UnifiedAssetEntity entity, UnifiedAsset data, SyncMetadata syncMetadata) {
    entity.setParentId(parentId);
    copyAssetEntity(entity, data, syncMetadata);
    return assetRepository.save(entity);
  }

  private void copyAssetEntity(
      UnifiedAssetEntity entity, UnifiedAsset data, SyncMetadata syncMetadata) {
    entity.setName(data.getMetadata().getName());
    entity.setDescription(data.getMetadata().getDescription());
    entity.setLogo(data.getMetadata().getLogo());
    entity.setLabels(data.getMetadata().getLabels());
    entity.setTags(data.getMetadata().getTags());
    entity.setVersion(data.getMetadata().getVersion());
    entity.setMapperKey(data.getMetadata().getMapperKey());
    entity.setStatus(data.getMetadata().getStatus());
    entity.setSyncMetadata(syncMetadata);

    if (entity.getId() == null) { // create
      entity.setCreatedBy(syncMetadata.getSyncedBy());
    } else {
      entity.setUpdatedBy(syncMetadata.getSyncedBy());
    }
  }

  private void syncFacets(UnifiedAssetEntity assetEntity, Map<String, Object> facets) {
    Set<AssetFacetEntity> existingFacets = assetEntity.getFacets();
    Set<AssetFacetEntity> newFacets = new HashSet<>();
    assetEntity.setFacets(newFacets);
    Set<UUID> toDeletedIds =
        existingFacets.stream().map(AssetFacetEntity::getId).collect(Collectors.toSet());
    log.info("delete asset facets ids:{}", JsonToolkit.toJson(toDeletedIds));
    this.assetFacetRepository.deleteAll(existingFacets);
    this.assetFacetRepository.flush();
    facets.forEach(
        (key, value) -> {
          AssetFacetEntity facetEntity = new AssetFacetEntity();
          facetEntity.setAsset(assetEntity);
          facetEntity.setKey(key);
          facetEntity.setPayload(value);
          newFacets.add(facetEntity);
        });
    this.assetFacetRepository.saveAll(newFacets);
  }

  private void syncAssetLinks(UnifiedAssetEntity assetEntity, List<AssetLink> links) {
    log.info("sync asset links, assetId: {},links:{}", assetEntity.getKey(), links.size());
    Set<AssetLinkEntity> existingLinks = assetEntity.getLinks();
    Set<AssetLinkEntity> newLinks = new HashSet<>();
    assetEntity.setLinks(newLinks);
    this.assetLinkRepository.deleteAll(existingLinks);

    for (AssetLink link : links) {
      if (link.getTargetAssetKey() == null || link.getRelationship() == null) {
        log.warn("Invalid link: {}", link);
        continue;
      }
      AssetLinkEntity linkEntity = new AssetLinkEntity();
      linkEntity.setAsset(assetEntity);
      linkEntity.setAsset(assetEntity);
      linkEntity.setTargetAssetKey(link.getTargetAssetKey());
      linkEntity.setRelationship(link.getRelationship());
      linkEntity.setGroup(link.getGroup());
      newLinks.add(linkEntity);
    }
    this.assetLinkRepository.saveAll(newLinks);
  }

  public Optional<UnifiedAssetEntity> findLatest(String parentId, AssetKindEnum kind) {
    return assetRepository.findTopOneByParentIdAndKindOrderByCreatedAtDesc(
        parentId, kind.getKind());
  }

  private static Specification<UnifiedAssetEntity> generateSpecification(
      List<Tuple2> eqConditions,
      List<Tuple2> labelConditions,
      List<String> tags,
      List<Tuple2> inConditions) {
    return (root, query, criteriaBuilder) -> {
      List<Predicate> predicateList = new ArrayList<>();
      if (CollectionUtils.isNotEmpty(eqConditions)) {
        eqConditions.stream()
            .filter(t -> t.value() != null)
            .forEach(
                tuple2 -> {
                  Predicate predicate =
                      criteriaBuilder.equal(root.get(tuple2.field()), realValue(tuple2));
                  predicateList.add(predicate);
                });
      }
      if (CollectionUtils.isNotEmpty(labelConditions)) {
        labelConditions.forEach(
            tuple3 -> {
              if (tuple3.value().equals(LabelConstants.CONDITION_NULL)) {
                Predicate predicate =
                    criteriaBuilder.isNull(
                        criteriaBuilder.function(
                            FUNCTION_JSON_EXTRACT_PATH_TEXT,
                            String.class,
                            root.get(AssetsConstants.FIELD_LABELS),
                            criteriaBuilder.literal(tuple3.field())));
                predicateList.add(predicate);
              } else {
                Predicate predicate =
                    criteriaBuilder.equal(
                        criteriaBuilder.function(
                            FUNCTION_JSON_EXTRACT_PATH_TEXT,
                            String.class,
                            root.get(AssetsConstants.FIELD_LABELS),
                            criteriaBuilder.literal(tuple3.field())),
                        tuple3.value());
                predicateList.add(predicate);
              }
            });
      }
      if (CollectionUtils.isNotEmpty(inConditions)) {
        Map<String, List<Tuple2>> map =
            inConditions.stream().collect(Collectors.groupingBy(Tuple2::field));
        map.forEach(
            (field, list) -> {
              Path<Object> path = root.get(field);
              CriteriaBuilder.In<Object> in = criteriaBuilder.in(path);
              list.stream()
                  .filter(t -> t.value() != null)
                  .forEach(tuple2 -> in.value(realValue(tuple2)));
              Predicate predicate = criteriaBuilder.and(in);
              predicateList.add(predicate);
            });
      }

      if (CollectionUtils.isNotEmpty(tags)) {
        Expression<Object> cast =
            criteriaBuilder.function("jsonb", Object.class, root.get(AssetsConstants.FIELD_TAGS));
        Expression<Boolean> expression =
            criteriaBuilder.function(
                "jsonb_contains",
                Boolean.class,
                cast,
                criteriaBuilder.literal(JsonToolkit.toJson(tags)));

        predicateList.add(criteriaBuilder.isTrue(expression));
      }

      Predicate[] predicateListArray = predicateList.toArray(new Predicate[0]);
      return query.where(predicateListArray).getRestriction();
    };
  }

  static Object realValue(Tuple2 tuple2) {
    if (tuple2.field().equals(AssetsConstants.FIELD_ID)) {
      return UUID.fromString(tuple2.value());
    }
    return tuple2.value();
  }

  @Transactional(readOnly = true)
  public Paging<UnifiedAssetDto> findBySpecification(
      List<Tuple2> eqConditions,
      List<Tuple2> labelConditions,
      List<String> tags,
      Pageable pageable,
      List<Tuple2> inConditions) {
    if (pageable == null) {
      pageable = PageRequest.of(0, 20);
    }
    Specification<UnifiedAssetEntity> specification =
        generateSpecification(eqConditions, labelConditions, tags, inConditions);
    return PagingHelper.toPaging(
        assetRepository.findAll(specification, pageable), t -> toAsset(t, true));
  }

  @Transactional(readOnly = true)
  public List<UnifiedAssetDto> findByAllKeysIn(List<String> keys, boolean includeFacets) {
    List<UnifiedAssetEntity> allByKeyIn = assetRepository.findAllByKeyIn(keys);
    return allByKeyIn.stream().map(t -> UnifiedAssetService.toAsset(t, includeFacets)).toList();
  }

  @Transactional(rollbackFor = Exception.class)
  public void addLabel(String assetId, String label, String value) {
    assetRepository
        .findById(UUID.fromString(assetId))
        .ifPresent(
            asset -> {
              Map<String, String> newLabels = new HashMap<>(asset.getLabels());
              newLabels.put(label, value);
              asset.setLabels(newLabels);
              assetRepository.save(asset);
            });
  }

  @Transactional(readOnly = true)
  public List<UnifiedAssetDto> findAll() {
    List<UnifiedAssetEntity> entities = assetRepository.findAll();
    return entities.stream().map(t -> UnifiedAssetService.toAsset(t, true)).toList();
  }

  @Transactional(readOnly = true)
  public List<UnifiedAssetDto> findByKind(String kind) {
    List<UnifiedAssetEntity> entities = assetRepository.findByKindOrderByCreatedAtDesc(kind);
    return entities.stream().map(t -> UnifiedAssetService.toAsset(t, true)).toList();
  }

  public List<AssetLinkEntity> findAssetLink(String targetKey, String relationShip) {
    return assetLinkRepository.findAllByTargetAssetKeyAndRelationship(targetKey, relationShip);
  }

  public boolean existed(String idOrKey) {
    return getUUID(idOrKey)
        .map(assetRepository::existsById)
        .orElseGet(() -> assetRepository.existsByKey(idOrKey));
  }
}
