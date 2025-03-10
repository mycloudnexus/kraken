package com.consoleconnect.kraken.operator.core.service;

import static com.consoleconnect.kraken.operator.core.toolkit.AssetsConstants.CUSTOMIZED_PLACE_HOLDER;
import static com.consoleconnect.kraken.operator.core.toolkit.ConstructExpressionUtil.extractMapperParam;

import com.consoleconnect.kraken.operator.core.dto.UnifiedAssetDto;
import com.consoleconnect.kraken.operator.core.entity.UnifiedAssetEntity;
import com.consoleconnect.kraken.operator.core.mapper.FacetsMapper;
import com.consoleconnect.kraken.operator.core.model.CommonMapperRef;
import com.consoleconnect.kraken.operator.core.model.KVPair;
import com.consoleconnect.kraken.operator.core.model.PathRule;
import com.consoleconnect.kraken.operator.core.model.UnifiedAsset;
import com.consoleconnect.kraken.operator.core.model.facet.ComponentAPITargetFacets;
import com.consoleconnect.kraken.operator.core.toolkit.JsonToolkit;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.*;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

public interface FacetsMerger extends AssetReader {

  String MAPPER_REQUEST = "request";
  String MAPPER_RESPONSE = "response";

  default Map<String, Object> mergeFacets(
      UnifiedAssetEntity unifiedAssetEntity, Map<String, Object> facetsUpdated) {
    UnifiedAssetDto assetDto = UnifiedAssetService.toAsset(unifiedAssetEntity, true);
    ComponentAPITargetFacets existFacets =
        UnifiedAsset.getFacets(assetDto, ComponentAPITargetFacets.class);
    ComponentAPITargetFacets newFacets =
        JsonToolkit.fromJson(JsonToolkit.toJson(facetsUpdated), ComponentAPITargetFacets.class);
    return mergeFacets(existFacets, newFacets);
  }

  default Map<String, Object> mergeFacets(
      ComponentAPITargetFacets facetsOld, ComponentAPITargetFacets facetsNew) {
    ComponentAPITargetFacets.Endpoint endpointOld = facetsOld.getEndpoints().get(0);
    ComponentAPITargetFacets.Endpoint endpointNew = facetsNew.getEndpoints().get(0);
    List<PathRule> pathRules =
        (Objects.isNull(endpointNew) || Objects.isNull(endpointNew.getMappers()))
            ? new ArrayList<>()
            : endpointNew.getMappers().getPathRules();
    extendCommonMapper(endpointNew);
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

  default void extendCommonMapper(ComponentAPITargetFacets.Endpoint endpointNew) {
    CommonMapperRef schemaRef =
        (Objects.isNull(endpointNew) || Objects.isNull(endpointNew.getMappers()))
            ? null
            : endpointNew.getMappers().getSchemaRef();
    if (Objects.isNull(schemaRef) || StringUtils.isBlank(schemaRef.getRef())) {
      return;
    }
    String refPath = schemaRef.getRef();
    Optional<UnifiedAsset> commonAssetOpt = readFromPath(refPath);
    commonAssetOpt.ifPresent(
        commonAsset -> {
          ComponentAPITargetFacets commonFacet =
              UnifiedAsset.getFacets(commonAsset, new TypeReference<>() {});
          ComponentAPITargetFacets.Endpoint commonEndpoint = commonFacet.getEndpoints().get(0);
          restoreMapper(
              commonEndpoint.getMappers().getRequest(),
              schemaRef.getParams(),
              endpointNew.getMappers().getRequest());
          restoreMapper(
              commonEndpoint.getMappers().getResponse(),
              schemaRef.getParams(),
              endpointNew.getMappers().getResponse());
        });
  }

  default void restoreMapper(
      List<ComponentAPITargetFacets.Mapper> commonMappers,
      List<KVPair> params,
      List<ComponentAPITargetFacets.Mapper> specificMappers) {
    if (CollectionUtils.isEmpty(commonMappers) || CollectionUtils.isEmpty(params)) {
      return;
    }
    Map<String, String> paramMap =
        params.stream().collect(Collectors.toMap(KVPair::getKey, KVPair::getVal));
    commonMappers.forEach(
        mapper -> {
          List<String> list1 = extractMapperParam(mapper.getName());
          if (CollectionUtils.isNotEmpty(list1)) {
            String val = paramMap.getOrDefault(list1.get(0), "");
            String newVal = mapper.getName().replaceAll(CUSTOMIZED_PLACE_HOLDER, val);
            mapper.setName(newVal);
          }
          List<String> list2 = extractMapperParam(mapper.getConvertValue());
          if (CollectionUtils.isNotEmpty(list2)) {
            String val = paramMap.getOrDefault(list2.get(0), "");
            String newVal = mapper.getConvertValue().replaceAll(CUSTOMIZED_PLACE_HOLDER, val);
            mapper.setConvertValue(newVal);
          }
          if (CollectionUtils.isEmpty(specificMappers)) {
            specificMappers.add(mapper);
          } else {
            specificMappers.stream()
                .filter(item -> item.getName().equals(mapper.getName()))
                .findFirst()
                .ifPresent(old -> old = mapper);
          }
        });
  }

  default Map<String, Map<String, ComponentAPITargetFacets.Mapper>> constructMapperMap(
      ComponentAPITargetFacets.Endpoint endpoint) {
    if (Objects.isNull(endpoint) || Objects.isNull(endpoint.getMappers())) {
      return new LinkedHashMap<>();
    }

    Map<String, Map<String, ComponentAPITargetFacets.Mapper>> mapperMap = new LinkedHashMap<>();
    Map<ComponentAPITargetFacets.Mapper, Boolean> seenMappers = new HashMap<>();
    removeDuplicatedNodes(
        endpoint.getMappers().getRequest(), mapperMap, seenMappers, MAPPER_REQUEST);
    removeDuplicatedNodes(
        endpoint.getMappers().getResponse(), mapperMap, seenMappers, MAPPER_RESPONSE);

    return mapperMap;
  }

  default void mergeMappers(
      Map<String, Map<String, ComponentAPITargetFacets.Mapper>> mapperMapOld,
      Map<String, Map<String, ComponentAPITargetFacets.Mapper>> mapperMapNew) {
    mapperMapOld.forEach(
        (name, value) -> {
          Map.Entry<String, ComponentAPITargetFacets.Mapper> existMapperEntry =
              value.entrySet().iterator().next();
          if (Objects.equals(Boolean.TRUE, existMapperEntry.getValue().getCustomizedField())) {
            String mapperHashCode = String.valueOf(existMapperEntry.getValue().hashCode());
            mapperMapNew.putIfAbsent(
                mapperHashCode,
                new HashMap<>(Map.of(existMapperEntry.getKey(), existMapperEntry.getValue())));
          } else if (mapperMapNew.containsKey(name)) {
            ComponentAPITargetFacets.Mapper mapper =
                mapperMapNew.get(name).get(existMapperEntry.getKey());
            if (Objects.equals(MAPPER_REQUEST, existMapperEntry.getKey())) {
              FacetsMapper.INSTANCE.toRequestMapper(existMapperEntry.getValue(), mapper);
            } else {
              FacetsMapper.INSTANCE.toResponseMapper(existMapperEntry.getValue(), mapper);
            }
          }
        });
  }

  default void removeDuplicatedNodes(
      List<ComponentAPITargetFacets.Mapper> mappers,
      Map<String, Map<String, ComponentAPITargetFacets.Mapper>> mapperMap,
      Map<ComponentAPITargetFacets.Mapper, Boolean> seenMappers,
      String mapperSection) {
    if (CollectionUtils.isNotEmpty(mappers)) {
      for (ComponentAPITargetFacets.Mapper mapper : mappers) {
        if (seenMappers.containsKey(mapper)) {
          continue;
        }
        String key =
            StringUtils.isBlank(mapper.getName())
                ? String.valueOf(mapper.hashCode())
                : mapper.getName();
        mapperMap.computeIfAbsent(key, k -> new LinkedHashMap<>()).put(mapperSection, mapper);
        seenMappers.put(mapper, true);
      }
    }
  }

  default Map<String, List<ComponentAPITargetFacets.Mapper>> toFinalMapper(
      Map<String, Map<String, ComponentAPITargetFacets.Mapper>> newMapperMap) {
    return newMapperMap.values().stream()
        .collect(
            Collectors.groupingBy(
                map -> map.entrySet().iterator().next().getKey(),
                Collectors.mapping(
                    nested -> nested.entrySet().iterator().next().getValue(),
                    Collectors.toList())));
  }
}
