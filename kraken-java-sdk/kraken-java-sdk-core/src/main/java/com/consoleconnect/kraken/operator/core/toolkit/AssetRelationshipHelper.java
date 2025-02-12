package com.consoleconnect.kraken.operator.core.toolkit;

import static com.consoleconnect.kraken.operator.core.enums.AssetLinkKindEnum.*;

import com.consoleconnect.kraken.operator.core.dto.ApiUseCaseDto;
import com.consoleconnect.kraken.operator.core.dto.Tuple2;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public class AssetRelationshipHelper {

  private AssetRelationshipHelper() {}

  private static final Map<String, BiConsumer<String, ApiUseCaseDto>> referenceHandlers =
      Map.of(
          COMPONENT_API.getKind(), AssetRelationshipHelper::handleComponent,
          IMPLEMENTATION_TARGET_MAPPER.getKind(), AssetRelationshipHelper::handleTargetMapper,
          IMPLEMENTATION_WORKFLOW.getKind(), AssetRelationshipHelper::handleWorkflow,
          IMPLEMENTATION_MAPPING_MATRIX.getKind(), AssetRelationshipHelper::handleMappingMatrix,
          IMPLEMENTATION_TARGET.getKind(), AssetRelationshipHelper::handleApiTarget);

  public static ApiUseCaseDto createReferences(List<Tuple2> tuple2s) {
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
}
