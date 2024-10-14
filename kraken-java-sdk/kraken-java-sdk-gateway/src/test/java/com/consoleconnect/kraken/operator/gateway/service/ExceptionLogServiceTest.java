package com.consoleconnect.kraken.operator.gateway.service;

import static org.hamcrest.MatcherAssert.assertThat;

import com.consoleconnect.kraken.operator.core.exception.KrakenExceptionHandler;
import com.consoleconnect.kraken.operator.gateway.CustomConfig;
import com.consoleconnect.kraken.operator.gateway.entity.HttpRequestEntity;
import com.consoleconnect.kraken.operator.gateway.filter.KrakenFilterConstants;
import com.consoleconnect.kraken.operator.gateway.repo.HttpRequestRepository;
import com.consoleconnect.kraken.operator.test.AbstractIntegrationTest;
import com.consoleconnect.kraken.operator.test.MockIntegrationTest;
import java.util.Map;
import java.util.UUID;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.reactive.function.server.MockServerRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.shaded.com.google.common.collect.Maps;

@MockIntegrationTest
@ContextConfiguration(classes = CustomConfig.class)
class ExceptionLogServiceTest extends AbstractIntegrationTest {
  @Autowired ExceptionLogService exceptionLogService;
  @Autowired KrakenExceptionHandler krakenExceptionHandler;
  @Autowired HttpRequestRepository httpRequestRepository;

  @Test
  void testErrorCallback() {
    HttpRequestEntity httpRequestEntity = new HttpRequestEntity();
    httpRequestEntity.setRequestId(UUID.randomUUID().toString());
    httpRequestEntity.setPath("/123");
    httpRequestEntity.setUri("localhost");
    httpRequestEntity.setMethod("GET");
    httpRequestEntity.setBizType("un");
    httpRequestRepository.save(httpRequestEntity);
    HttpRequestEntity httpRequestEntity2 = new HttpRequestEntity();
    httpRequestEntity2.setRequestId(UUID.randomUUID().toString());
    httpRequestEntity2.setPath("/1234");
    httpRequestEntity2.setUri("localhost");
    httpRequestEntity2.setMethod("GET");
    httpRequestEntity2.setBizType("un");
    httpRequestRepository.save(httpRequestEntity2);

    String entityId = httpRequestEntity.getId().toString();
    String transformedEntityId = httpRequestEntity2.getId().toString();
    krakenExceptionHandler
        .getCallbackList()
        .forEach(
            consumer -> {
              MockServerRequest serverRequest =
                  MockServerRequest.builder()
                      .exchange(MockServerWebExchange.from(MockServerHttpRequest.get("/1233")))
                      .build();

              //            MockServerRequest

              serverRequest
                  .exchange()
                  .getAttributes()
                  .put(KrakenFilterConstants.X_ENTITY_ID, entityId);
              serverRequest
                  .exchange()
                  .getAttributes()
                  .put(KrakenFilterConstants.X_TRANSFORMED_ENTITY_ID, transformedEntityId);
              serverRequest
                  .exchange()
                  .getResponse()
                  .setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
              Map<String, String> error = Maps.newHashMap();
              error.put("code", "500");
              consumer.accept(serverRequest, error, HttpStatusCode.valueOf(500));
              assertThat(error, Matchers.notNullValue());
            });
  }
}
