package com.consoleconnect.kraken.operator.gateway.runner;

import static com.consoleconnect.kraken.operator.gateway.runner.SpelEngineActionRunner.INPUT_CODE;

import com.consoleconnect.kraken.operator.core.entity.ApiActivityLogBodyEntity;
import com.consoleconnect.kraken.operator.core.entity.ApiActivityLogEntity;
import com.consoleconnect.kraken.operator.core.exception.KrakenException;
import com.consoleconnect.kraken.operator.core.model.facet.ComponentAPIFacets;
import com.consoleconnect.kraken.operator.core.repo.ApiActivityLogRepository;
import com.consoleconnect.kraken.operator.core.service.ApiActivityLogService;
import com.consoleconnect.kraken.operator.core.toolkit.JsonToolkit;
import com.consoleconnect.kraken.operator.core.toolkit.YamlToolkit;
import com.consoleconnect.kraken.operator.gateway.CustomConfig;
import com.consoleconnect.kraken.operator.gateway.filter.KrakenFilterConstants;
import com.consoleconnect.kraken.operator.gateway.template.SpELEngine;
import com.consoleconnect.kraken.operator.test.AbstractIntegrationTest;
import com.consoleconnect.kraken.operator.test.MockIntegrationTest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.server.ServerWebExchange;

@MockIntegrationTest
@ContextConfiguration(classes = CustomConfig.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class JavaScriptEngineActionRunnerTest extends AbstractIntegrationTest {

  @Autowired private JavaScriptEngineActionRunner runner;
  @Autowired private ApiActivityLogService apiActivityLogService;
  @Autowired private ApiActivityLogRepository apiActivityLogRepository;

  @Test
  @SneakyThrows
  void givenRequestMissAction_whenExecuteJavascriptAction_thenThrowException() {
    ComponentAPIFacets.Action action =
        YamlToolkit.parseYaml(
                readFileToString("/mockData/api.order.mock.yaml"), ComponentAPIFacets.Action.class)
            .get();
    String code = (String) action.getWith().get("code");
    Map request =
        JsonToolkit.fromJson(
            readFileToString("/mockData/create_order_miss_action_request.json"), Map.class);
    ServerWebExchange exchange = Mockito.mock(ServerWebExchange.class);
    Assertions.assertThrowsExactly(
        KrakenException.class,
        () ->
            runner.runIt(
                exchange,
                action,
                Map.of(
                    "body",
                    request,
                    INPUT_CODE,
                    code,
                    "productOrderItem",
                    SpELEngine.evaluate("${productOrderItem[0]}", request, Object.class)),
                Map.of("order", new HashMap<>())));
  }

  @ParameterizedTest
  @MethodSource(value = "buildRoutingErrorResult")
  void givenRoutingError_whenHandle_thenThrowsException(String resultJson) {
    Assertions.assertThrowsExactly(
        KrakenException.class, () -> runner.handleRoutingResult(resultJson));
  }

  @SneakyThrows
  public static List<String> buildRoutingErrorResult() {
    String s1 = readFileToString("/mockData/routing-error-400.json");
    String s2 = readFileToString("/mockData/routing-error-422.json");
    return List.of(s1, s2);
  }

  @SneakyThrows
  @Test
  void givenRequestWithProductType_whenRunning_thenProductTypeSavedOK() {
    String resultInJson = readFileToString("/mockData/routing-ok.json");
    ApiActivityLogBodyEntity apiLogBodyEntity = new ApiActivityLogBodyEntity();
    apiLogBodyEntity.setRequest(resultInJson);

    ApiActivityLogEntity entity = new ApiActivityLogEntity();
    entity.setRequestId(UUID.randomUUID().toString());
    entity.setPath("/mefApi/sonata/quoteManagement/v8/quote");
    entity.setMethod("POST");
    entity.setUri("localhost");
    entity.setApiLogBodyEntity(apiLogBodyEntity);
    entity.setCallSeq(0);
    entity.setRequest(resultInJson);
    entity = apiActivityLogService.save(entity);

    ServerWebExchange exchange = Mockito.mock(ServerWebExchange.class);
    Mockito.doReturn(entity.getId().toString())
        .when(exchange)
        .getAttribute(KrakenFilterConstants.X_LOG_ENTITY_ID);
    runner.recordProductType(exchange, resultInJson);
    entity = apiActivityLogRepository.findById(entity.getId()).orElseThrow();
    String productType = entity.getProductType();
    Assertions.assertEquals("UNI", productType);
  }
}
