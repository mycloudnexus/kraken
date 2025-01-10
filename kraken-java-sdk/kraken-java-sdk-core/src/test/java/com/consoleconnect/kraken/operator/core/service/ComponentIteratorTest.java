package com.consoleconnect.kraken.operator.core.service;

import com.consoleconnect.kraken.operator.core.CustomConfig;
import com.consoleconnect.kraken.operator.core.dto.ApiUseCaseDto;
import com.consoleconnect.kraken.operator.core.dto.Tuple2;
import com.consoleconnect.kraken.operator.core.enums.AssetLinkKindEnum;
import com.consoleconnect.kraken.operator.core.toolkit.JsonToolkit;
import com.consoleconnect.kraken.operator.test.AbstractIntegrationTest;
import com.consoleconnect.kraken.operator.test.MockIntegrationTest;
import java.util.*;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@Slf4j
@MockIntegrationTest
@ContextConfiguration(classes = CustomConfig.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ComponentIteratorTest extends AbstractIntegrationTest implements ApiUseCaseSelector {

  @Autowired UnifiedAssetService unifiedAssetService;

  private static final List<String> targetKeys =
      List.of(
          "mef.sonata.api-target.order.eline.add",
          "mef.sonata.api-target.order.uni.read",
          "mef.sonata.api-target.order.eline.delete",
          "mef.sonata.api-target.order.eline.read",
          "mef.sonata.api-target.order.uni.delete",
          "mef.sonata.api-target.order.eline.read.delete",
          "mef.sonata.api-target.order.uni.read.delete",
          "mef.sonata.api-target.order.uni.add");

  @Override
  public UnifiedAssetService getUnifiedAssetService() {
    return unifiedAssetService;
  }

  @Test
  @Order(1)
  void givenNothing_whenFindApiUseCase_thenReturnOK() {
    Map<String, List<Tuple2>> map = findApiUseCase();
    log.info(JsonToolkit.toJson(map));
    Assertions.assertNotNull(map);

    targetKeys.forEach(
        target -> {
          Assertions.assertTrue(map.containsKey(target));
          Set<String> values =
              map.get(target).stream().map(Tuple2::value).collect(Collectors.toSet());
          Assertions.assertTrue(values.contains(AssetLinkKindEnum.IMPLEMENTATION_TARGET.getKind()));
          Assertions.assertTrue(
              values.contains(AssetLinkKindEnum.IMPLEMENTATION_TARGET_MAPPER.getKind()));
          Assertions.assertTrue(
              values.contains(AssetLinkKindEnum.IMPLEMENTATION_MAPPING_MATRIX.getKind()));
          if (target.equals("mef.sonata.api-target.order.eline.delete")) {
            Assertions.assertTrue(
                values.contains(AssetLinkKindEnum.IMPLEMENTATION_WORKFLOW.getKind()));
          }
        });
  }

  @Test
  @Order(2)
  void givenTargetKey_whenFindRelatedApiUse_thenReturnOK() {
    Map<String, List<Tuple2>> map = findApiUseCase();
    Assertions.assertNotNull(map);
    Optional<ApiUseCaseDto> result =
        findRelatedApiUse("mef.sonata.api-target.order.eline.add", map);
    Assertions.assertTrue(result.isPresent());
    ApiUseCaseDto apiUseCaseDto = result.get();
    Assertions.assertNotNull(apiUseCaseDto);
    log.info(JsonToolkit.toJson(apiUseCaseDto));
    Assertions.assertEquals("mef.sonata.api.order", apiUseCaseDto.getComponentApiKey());
    Assertions.assertEquals(
        "mef.sonata.api-target-mapper.order.eline.add", apiUseCaseDto.getMapperKey());
    Assertions.assertEquals("mef.sonata.api-target.order.eline.add", apiUseCaseDto.getTargetKey());
    Assertions.assertEquals(
        "mef.sonata.api.matrix.order.eline.add", apiUseCaseDto.getMappingMatrixKey());
  }
}
