package com.consoleconnect.kraken.operator.core.service;

import static com.consoleconnect.kraken.operator.core.enums.AssetKindEnum.COMPONENT_API_TARGET_MAPPER;
import static com.consoleconnect.kraken.operator.core.enums.AssetKindEnum.COMPONENT_API_WORK_FLOW;

import com.consoleconnect.kraken.operator.core.entity.UnifiedAssetEntity;
import com.consoleconnect.kraken.operator.core.mapper.FacetsMapper;
import com.consoleconnect.kraken.operator.core.model.CommonMapperRef;
import com.consoleconnect.kraken.operator.core.model.HttpTask;
import com.consoleconnect.kraken.operator.core.model.PathRule;
import com.consoleconnect.kraken.operator.core.model.facet.ComponentAPITargetFacets;
import com.consoleconnect.kraken.operator.core.model.facet.ComponentWorkflowFacets;
import com.consoleconnect.kraken.operator.core.toolkit.JsonToolkit;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.*;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.logging.log4j.util.Strings;

public interface FacetsMerger extends CommonMapperExtender {

  String MAPPER_REQUEST = "request";
  String MAPPER_RESPONSE = "response";

  @Slf4j
  final class LogHolder {}

  default Map<String, Object> mergeEndpoint(
      ComponentAPITargetFacets.Endpoint endpointOld,
      ComponentAPITargetFacets.Endpoint endpointNew,
      boolean extendCommon) {
    List<PathRule> pathRules =
        (Objects.isNull(endpointNew) || Objects.isNull(endpointNew.getMappers()))
            ? new ArrayList<>()
            : endpointNew.getMappers().getPathRules();
    CommonMapperRef schemaRef =
        (Objects.isNull(endpointNew) || Objects.isNull(endpointNew.getMappers()))
            ? null
            : endpointNew.getMappers().getSchemaRef();
    if (extendCommon) {
      LogHolder.log.info("Start to extend common mapper");
      extendCommonMapper(endpointNew);
    }
    FacetsMapper.INSTANCE.toEndpoint(endpointOld, endpointNew);

    Map<String, Map<String, ComponentAPITargetFacets.Mapper>> mapperOldMap =
        constructMapperMap(endpointOld);
    Map<String, Map<String, ComponentAPITargetFacets.Mapper>> mapperNewMap =
        constructMapperMap(endpointNew);
    mergeMappers(mapperOldMap, mapperNewMap);

    Map<String, List<ComponentAPITargetFacets.Mapper>> finalMap = toFinalMapper(mapperNewMap);
    ComponentAPITargetFacets.Mappers mappers = new ComponentAPITargetFacets.Mappers();
    mappers.setSchemaRef(schemaRef);
    mappers.setResponse(finalMap.getOrDefault(MAPPER_RESPONSE, Collections.emptyList()));
    mappers.setRequest(finalMap.getOrDefault(MAPPER_REQUEST, Collections.emptyList()));
    mappers.setPathRules(pathRules);
    if (Objects.nonNull(endpointNew)) {
      endpointNew.setMappers(mappers);
    }
    return JsonToolkit.fromJson(JsonToolkit.toJson(endpointNew), new TypeReference<>() {});
  }

  default void mergeMappers(
      Map<String, Map<String, ComponentAPITargetFacets.Mapper>> mapperMapOld,
      Map<String, Map<String, ComponentAPITargetFacets.Mapper>> mapperMapNew) {
    mapperMapOld.forEach(
        (name, value) -> {
          Map.Entry<String, ComponentAPITargetFacets.Mapper> existMapperEntry =
              value.entrySet().iterator().next();
          Optional<ComponentAPITargetFacets.Mapper> copyTo =
              findMapper(mapperMapNew, name, existMapperEntry.getKey());
          if (copyTo.isPresent()) {
            mergeMapper(existMapperEntry, copyTo.get());
          } else {
            deepCopyMapper(name, existMapperEntry, mapperMapNew);
          }
        });
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

  default Map<String, Map<String, ComponentAPITargetFacets.Mapper>> constructMapperMap(
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

  default void removeDuplicatedNodes(
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

  private static void mergeMapper(
      Map.Entry<String, ComponentAPITargetFacets.Mapper> copyFrom,
      ComponentAPITargetFacets.Mapper copyTo) {
    String mapperSection = copyFrom.getKey();
    if (isConfigured(copyFrom)) {
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
      copyFrom.getValue().setRequiredMapping(Boolean.FALSE);
      mapperMapNew.put(name, new HashMap<>(Map.of(mapperSection, copyFrom.getValue())));
    } else if (isSystemAndConfigured(copyFrom)) {
      copyFrom.getValue().setCustomizedField(Boolean.TRUE);
      copyFrom.getValue().setRequiredMapping(Boolean.FALSE);
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
    return isConfigured(mapper);
  }

  private static boolean isSystemAndConfigured(
      Map.Entry<String, ComponentAPITargetFacets.Mapper> mapper) {
    if (!isSystemMapping(mapper)) {
      return false;
    }
    return isConfigured(mapper);
  }

  private static boolean isConfigured(Map.Entry<String, ComponentAPITargetFacets.Mapper> mapper) {
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

  default boolean enableMerge(UnifiedAssetEntity assetEntity) {
    return Objects.equals(assetEntity.getKind(), COMPONENT_API_TARGET_MAPPER.getKind())
        || Objects.equals(assetEntity.getKind(), COMPONENT_API_WORK_FLOW.getKind());
  }

  default Map<String, Object> mergeFacets(
      ComponentWorkflowFacets facetsOld, ComponentWorkflowFacets facetsNew) {
    mergeFacets(facetsOld.getValidationStage(), facetsNew.getValidationStage());
    mergeFacets(facetsOld.getPreparationStage(), facetsNew.getPreparationStage());
    mergeFacets(facetsOld.getExecutionStage(), facetsNew.getExecutionStage());
    return JsonToolkit.fromJson(JsonToolkit.toJson(facetsNew), new TypeReference<>() {});
  }

  default void mergeFacets(List<HttpTask> stageTasksOld, List<HttpTask> stageTasksNew) {
    if (CollectionUtils.isEmpty(stageTasksOld) || CollectionUtils.isEmpty(stageTasksNew)) {
      return;
    }

    HttpTask.ConditionCheck oldConditionCheck = stageTasksOld.get(0).getConditionCheck();
    HttpTask.ConditionCheck newConditionCheck = stageTasksNew.get(0).getConditionCheck();
    if (oldConditionCheck != null
        && newConditionCheck != null
        && newConditionCheck.getBuildInTask() != null) {
      oldConditionCheck.setBuildInTask(newConditionCheck.getBuildInTask());
    }
    stageTasksNew.get(0).setConditionCheck(oldConditionCheck);

    ComponentAPITargetFacets.Endpoint endpointOld = stageTasksOld.get(0).getEndpoint();
    ComponentAPITargetFacets.Endpoint endpointNew = stageTasksNew.get(0).getEndpoint();
    mergeEndpoint(endpointOld, endpointNew, false);
  }

  default Map<String, Object> mergeFacets(
      ComponentAPITargetFacets facetsOld,
      ComponentAPITargetFacets facetsNew,
      boolean extendCommon) {
    ComponentAPITargetFacets.Endpoint endpointOld = facetsOld.getEndpoints().get(0);
    ComponentAPITargetFacets.Endpoint endpointNew = facetsNew.getEndpoints().get(0);
    mergeEndpoint(endpointOld, endpointNew, extendCommon);
    return JsonToolkit.fromJson(JsonToolkit.toJson(facetsNew), new TypeReference<>() {});
  }

  default Map<String, Object> extendCommonNodesInFacets(
      boolean extendCommon, Map<String, Object> facetsUpdated) {
    if (!extendCommon || MapUtils.isEmpty(facetsUpdated)) {
      return facetsUpdated;
    }
    ComponentAPITargetFacets facetsNew =
        JsonToolkit.fromJson(JsonToolkit.toJson(facetsUpdated), ComponentAPITargetFacets.class);
    ComponentAPITargetFacets.Endpoint endpointNew = facetsNew.getEndpoints().get(0);
    extendCommonMapper(endpointNew);
    return JsonToolkit.fromJson(JsonToolkit.toJson(facetsNew), new TypeReference<>() {});
  }
}
