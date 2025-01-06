package com.consoleconnect.kraken.operator.gateway;

import static com.consoleconnect.kraken.operator.core.toolkit.ConstructExpressionUtil.convertToJsonPointer;
import static com.consoleconnect.kraken.operator.gateway.SpELEngineTest.readFileToString;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.contains;

import com.consoleconnect.kraken.operator.core.dto.StateValueMappingDto;
import com.consoleconnect.kraken.operator.core.dto.UnifiedAssetDto;
import com.consoleconnect.kraken.operator.core.model.AppProperty;
import com.consoleconnect.kraken.operator.core.model.UnifiedAsset;
import com.consoleconnect.kraken.operator.core.model.facet.ComponentAPITargetFacets;
import com.consoleconnect.kraken.operator.core.service.UnifiedAssetService;
import com.consoleconnect.kraken.operator.core.toolkit.JsonToolkit;
import com.consoleconnect.kraken.operator.core.toolkit.YamlToolkit;
import com.consoleconnect.kraken.operator.gateway.runner.LoadTargetAPIConfigActionRunner;
import com.consoleconnect.kraken.operator.gateway.runner.MappingTransformer;
import com.consoleconnect.kraken.operator.gateway.service.RenderRequestService;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

@Slf4j
class RequestMapperTest implements MappingTransformer {

  private static UnifiedAssetService unifiedAssetService;
  private static RenderRequestService renderRequestService;
  private static LoadTargetAPIConfigActionRunner runner;

  @BeforeAll
  static void init() {
    unifiedAssetService = Mockito.mock(UnifiedAssetService.class);
    renderRequestService = new RenderRequestService(unifiedAssetService);
    runner =
        new LoadTargetAPIConfigActionRunner(
            new AppProperty(), unifiedAssetService, renderRequestService);
  }

  @Test
  void requestParserTest() throws IOException {
    ComponentAPITargetFacets facets = renderFacets("mockData/api-target.address.validate.yaml");
    runner.encodeUrlParam(facets.getEndpoints().get(0).getPath());
    System.out.println("test address validation");
    Assertions.assertTrue(facets.getEndpoints().get(0).getPath().contains("criteria"));

    System.out.println("test order add");
    ComponentAPITargetFacets orderFacets = renderFacets("mockData/api-target.order.eline.add.yaml");
    Assertions.assertTrue(orderFacets.getEndpoints().get(0).getPath().contains("mefQuery"));

    log.info("test order read");
    UnifiedAssetDto elineAsset =
        YamlToolkit.parseYaml(
                readFileToString("mockData/api-target.order.eline.add.yaml"), UnifiedAssetDto.class)
            .get();
    Mockito.doReturn(elineAsset).when(unifiedAssetService).findOne(contains("order.eline"));
    ComponentAPITargetFacets orderReadFacets =
        renderFacets("mockData/api-target.order.eline.read.yaml");
    Assertions.assertNotNull(orderReadFacets);
    System.out.println("test notify");
    UnifiedAssetDto notifyAsset =
        YamlToolkit.parseYaml(
                readFileToString("mockData/api-target.order.notification.state.change.yaml"),
                UnifiedAssetDto.class)
            .get();
    Mockito.doReturn(notifyAsset).when(unifiedAssetService).findOne(contains("state"));
    ComponentAPITargetFacets readFacets =
        renderFacets("mockData/api-target.order.notification.state.change.yaml");
    Assertions.assertTrue(readFacets.getEndpoints().get(0).getRequestBody().contains("body.id"));
  }

  private ComponentAPITargetFacets renderFacets(String path) throws IOException {
    String s = readFileToString(path);
    Optional<UnifiedAsset> unifiedAsset = YamlToolkit.parseYaml(s, UnifiedAsset.class);
    ComponentAPITargetFacets facets =
        UnifiedAsset.getFacets(unifiedAsset.get(), ComponentAPITargetFacets.class);
    StateValueMappingDto stateValueMappingDto = new StateValueMappingDto();
    renderRequestService.parseRequest(facets.getEndpoints(), stateValueMappingDto);
    return facets;
  }

  @SneakyThrows
  @Test
  void givenPathKey_whenGenerate_returnJson() {
    String s =
        JsonToolkit.generateJson(convertToJsonPointer("@{{user.class[*].name}}"), "John", "{}");
    String s2 =
        JsonToolkit.generateJson(
            convertToJsonPointer("@{{user.class[*].password}}"), "password", s);
    String s3 =
        JsonToolkit.generateJson(
            convertToJsonPointer("@{{user.class[*].secret.key}}"), "secretKey", s2);
    log.info("result: {}", s3);
    assertThat(s3, hasJsonPath("$.user.class[0].secret.key", equalTo("secretKey")));
    Map<String, String> params = new HashMap<>();
    params.put("key1", "value1");
    params.put("key2", "value2");
    String s4 =
        JsonToolkit.generateJson(
            convertToJsonPointer("@{{user.class[*].quoteItemPrice[*]}}"),
            JsonToolkit.toJson(params),
            s3);
    log.info("s4:{}", s4);
  }
}
