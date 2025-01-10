package com.consoleconnect.kraken.operator.core.service;

import static com.consoleconnect.kraken.operator.core.enums.AssetLinkKindEnum.*;

import com.consoleconnect.kraken.operator.core.dto.ApiUseCaseDto;
import com.consoleconnect.kraken.operator.core.dto.Tuple2;
import com.consoleconnect.kraken.operator.core.enums.AssetKindEnum;
import com.consoleconnect.kraken.operator.core.model.AssetLink;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;

public interface ApiUseCaseSelector {

  UnifiedAssetService getUnifiedAssetService();

  default Map<String, List<Tuple2>> findApiUseCase() {
    // groupKey,<componentKey,assetLinkKind>
    Map<String, List<Tuple2>> result = new HashMap<>();
    getUnifiedAssetService().findByKind(AssetKindEnum.COMPONENT_API.getKind()).stream()
        .filter(component -> CollectionUtils.isNotEmpty(component.getLinks()))
        .forEach(
            component -> {
              Map<String, List<Tuple2>> groupMap =
                  component.getLinks().stream()
                      .filter(assetLink -> assetLink.getGroup() != null)
                      .collect(
                          Collectors.groupingBy(
                              AssetLink::getGroup,
                              Collectors.mapping(
                                  t -> Tuple2.of(t.getTargetAssetKey(), t.getRelationship()),
                                  Collectors.toList())));
              groupMap.forEach(
                  (key, members) -> {
                    members.add(
                        Tuple2.of(component.getMetadata().getKey(), COMPONENT_API.getKind()));
                    result.put(key, members);
                  });
            });
    return result;
  }

  default Optional<ApiUseCaseDto> findRelatedApiUse(
      String key, Map<String, List<Tuple2>> apiUseCaseMap) {
    return apiUseCaseMap.values().stream()
        .map(this::createReferences)
        .filter(t -> t.membersExcludeApiKey().contains(key))
        .findFirst();
  }

  private ApiUseCaseDto createReferences(List<Tuple2> tuple2s) {
    final ApiUseCaseDto apiUseCaseDto = new ApiUseCaseDto();
    tuple2s.stream()
        .forEach(
            link -> {
              if (referenceHandlers.containsKey(link.value())) {
                referenceHandlers.get(link.value()).accept(link.field(), apiUseCaseDto);
              }
            });
    return apiUseCaseDto;
  }

  private static void handleComponent(String key, ApiUseCaseDto apiUseCaseDto) {
    apiUseCaseDto.setComponentApiKey(key);
  }

  private static void handleTargetMapper(String key, ApiUseCaseDto apiUseCaseDto) {
    apiUseCaseDto.setMapperKey(key);
  }

  private static void handleWorkflow(String key, ApiUseCaseDto apiUseCaseDto) {
    apiUseCaseDto.setWorkflowKey(key);
  }

  private static void handleMappingMatrix(String key, ApiUseCaseDto apiUseCaseDto) {
    apiUseCaseDto.setMappingMatrixKey(key);
  }

  private static void handleApiTarget(String key, ApiUseCaseDto apiUseCaseDto) {
    apiUseCaseDto.setTargetKey(key);
  }

  static final Map<String, BiConsumer<String, ApiUseCaseDto>> referenceHandlers =
      new HashMap<>() {
        {
          put(COMPONENT_API.getKind(), ApiUseCaseSelector::handleComponent);
          put(IMPLEMENTATION_TARGET_MAPPER.getKind(), ApiUseCaseSelector::handleTargetMapper);
          put(IMPLEMENTATION_WORKFLOW.getKind(), ApiUseCaseSelector::handleWorkflow);
          put(IMPLEMENTATION_MAPPING_MATRIX.getKind(), ApiUseCaseSelector::handleMappingMatrix);
          put(IMPLEMENTATION_TARGET.getKind(), ApiUseCaseSelector::handleApiTarget);
        }
      };
}
