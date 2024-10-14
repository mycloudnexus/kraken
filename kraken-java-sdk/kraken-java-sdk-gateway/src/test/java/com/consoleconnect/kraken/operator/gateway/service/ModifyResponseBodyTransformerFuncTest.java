package com.consoleconnect.kraken.operator.gateway.service;

import static com.consoleconnect.kraken.operator.gateway.runner.AbstractActionRunner.ATTRIBUTE_KEY_PREFIX;
import static com.consoleconnect.kraken.operator.gateway.runner.MappingTransformer.TARGET_VALUE_MAPPER_KEY;
import static com.consoleconnect.kraken.operator.gateway.runner.SpelEngineActionRunner.INPUT_CODE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

import com.consoleconnect.kraken.operator.core.dto.ResponseTargetMapperDto;
import com.consoleconnect.kraken.operator.core.model.AppProperty;
import com.consoleconnect.kraken.operator.core.model.facet.ComponentAPIFacets;
import com.consoleconnect.kraken.operator.gateway.CustomConfig;
import com.consoleconnect.kraken.operator.gateway.entity.HttpRequestEntity;
import com.consoleconnect.kraken.operator.gateway.filter.KrakenFilterConstants;
import com.consoleconnect.kraken.operator.gateway.repo.HttpRequestRepository;
import com.consoleconnect.kraken.operator.gateway.runner.ModifyResponseBodyTransformerFunc;
import com.consoleconnect.kraken.operator.gateway.template.JavaScriptEngine;
import com.consoleconnect.kraken.operator.test.AbstractIntegrationTest;
import com.consoleconnect.kraken.operator.test.MockIntegrationTest;
import java.util.*;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.test.context.ContextConfiguration;

@MockIntegrationTest
@ContextConfiguration(classes = CustomConfig.class)
class ModifyResponseBodyTransformerFuncTest extends AbstractIntegrationTest {

  @SpyBean private JavaScriptEngine javaScriptEngine;
  @SpyBean private AppProperty appProperty;
  @SpyBean private HttpRequestRepository httpRequestRepository;
  @SpyBean private FilterHeaderService filterHeaderService;

  @SneakyThrows
  @Test
  void givenRespOfQuoteUNI_whenTransform_thenNoExceptionThrows() {
    ComponentAPIFacets.Action action = new ComponentAPIFacets.Action();
    ModifyResponseBodyTransformerFunc func =
        new ModifyResponseBodyTransformerFunc(
            action, javaScriptEngine, httpRequestRepository, appProperty, filterHeaderService);
    String responseBody = readFileToString("mef/order/cc_port_uni_quote_resp.json");

    MockServerWebExchange exchange =
        MockServerWebExchange.from(
            MockServerHttpRequest.post("https://httpbin.org/get")
                .contentType(MediaType.APPLICATION_JSON)
                .body(responseBody));

    exchange.getResponse().setStatusCode(HttpStatus.OK);
    exchange
        .getAttributes()
        .put(KrakenFilterConstants.X_TRANSFORMED_ENTITY_ID, "d2dc891d-44bc-4cd2-bfb8-04ebc7b97eeb");
    exchange.getAttributes().put(ATTRIBUTE_KEY_PREFIX + "engine", "spel");
    Map<String, String> valueMappings = new HashMap<>();
    valueMappings.put("y", "calendarMonth");
    valueMappings.put("m", "calendarMonth");

    ResponseTargetMapperDto dto = new ResponseTargetMapperDto();
    dto.setTargetPathMapper(List.of("$.results[0].entity.durationUnit"));
    dto.setTargetValueMapper(valueMappings);

    Map<String, Object> with = new HashMap<>();
    with.put(INPUT_CODE, responseBody);
    with.put(TARGET_VALUE_MAPPER_KEY, dto);
    action.setWith(with);

    HttpRequestEntity httpRequestEntity = new HttpRequestEntity();
    httpRequestEntity.setBizType("123");
    httpRequestEntity.setMethod("post");
    httpRequestEntity.setRequestId("1i32ii4u2u2");
    httpRequestEntity.setUri("https://httpbin.org");
    httpRequestEntity.setPath("/get");
    httpRequestEntity.setId(UUID.fromString("d2dc891d-44bc-4cd2-bfb8-04ebc7b97eeb"));

    doReturn(Optional.of(httpRequestEntity)).when(httpRequestRepository).findById(any());
    Assertions.assertDoesNotThrow(
        () -> {
          func.apply(exchange, responseBody);
        });
  }
}
