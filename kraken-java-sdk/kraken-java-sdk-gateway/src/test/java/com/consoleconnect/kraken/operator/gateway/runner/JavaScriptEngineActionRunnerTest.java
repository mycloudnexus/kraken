package com.consoleconnect.kraken.operator.gateway.runner;

import static com.consoleconnect.kraken.operator.gateway.runner.SpelEngineActionRunner.INPUT_CODE;

import com.consoleconnect.kraken.operator.core.exception.KrakenException;
import com.consoleconnect.kraken.operator.core.model.facet.ComponentAPIFacets;
import com.consoleconnect.kraken.operator.core.toolkit.JsonToolkit;
import com.consoleconnect.kraken.operator.core.toolkit.YamlToolkit;
import com.consoleconnect.kraken.operator.gateway.CustomConfig;
import com.consoleconnect.kraken.operator.test.AbstractIntegrationTest;
import com.consoleconnect.kraken.operator.test.MockIntegrationTest;
import java.util.HashMap;
import java.util.Map;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.server.ServerWebExchange;

@MockIntegrationTest
@ContextConfiguration(classes = CustomConfig.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class JavaScriptEngineActionRunnerTest extends AbstractIntegrationTest {

  @Autowired private JavaScriptEngineActionRunner runner;

  @Test
  @SneakyThrows
  void givenRequestMissAction_whenExecuteJavascriptAction_thenThrowException() {
    ComponentAPIFacets.Action action =
        YamlToolkit.parseYaml(
                readFileToString("/mockData/api.order.mock.yaml"), ComponentAPIFacets.Action.class)
            .get();
    String code = (String) action.getWith().get("code");
    Object request =
        JsonToolkit.fromJson(
            readFileToString("/mockData/create_order_miss_action_request.json"), Object.class);
    ServerWebExchange exchange = Mockito.mock(ServerWebExchange.class);
    Assertions.assertThrowsExactly(
        KrakenException.class,
        () ->
            runner.runIt(
                exchange,
                action,
                Map.of("mefRequestBody", request, INPUT_CODE, code),
                Map.of("order", new HashMap<>())));
  }
}
