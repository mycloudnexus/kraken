package com.consoleconnect.kraken.operator.gateway.runner;

import com.consoleconnect.kraken.operator.core.enums.DBActionTypeEnum;
import com.consoleconnect.kraken.operator.core.exception.KrakenException;
import com.consoleconnect.kraken.operator.gateway.CustomConfig;
import com.consoleconnect.kraken.operator.gateway.entity.HttpRequestEntity;
import com.consoleconnect.kraken.operator.gateway.filter.KrakenFilterConstants;
import com.consoleconnect.kraken.operator.gateway.repo.HttpRequestRepository;
import com.consoleconnect.kraken.operator.test.AbstractIntegrationTest;
import com.consoleconnect.kraken.operator.test.MockIntegrationTest;
import java.util.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.server.ServerWebExchange;

@MockIntegrationTest
@ContextConfiguration(classes = CustomConfig.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DBCrudActionRunnerTest extends AbstractIntegrationTest {

  @Autowired private DBCrudActionRunner dbCrudActionRunner;
  @Autowired private HttpRequestRepository httpRequestRepository;

  @Test
  void givenNotExistedId_whenQueryRequestEntity_thenThrow404() {
    ServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/1234"));
    DBCrudActionRunner.Config config = new DBCrudActionRunner.Config();
    config.setId("mock-" + UUID.randomUUID());
    config.setAction(DBActionTypeEnum.READ);
    Assertions.assertThrows(
        KrakenException.class, () -> dbCrudActionRunner.onPersist(exchange, config));
  }

  @Test
  void givenExistedId_whenQueryRequestEntity_thenResponseOK() {
    ServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/1234"));
    HttpRequestEntity entity = DBCrudActionRunner.generateHttpRequestEntity(exchange);
    entity.setUri("https://httpbin.org/");
    entity.setPath("/1234");
    entity.setMethod("GET");
    entity.setRequestId(UUID.randomUUID().toString());
    entity.setBizType("db persist");
    httpRequestRepository.save(entity);

    DBCrudActionRunner.Config config = new DBCrudActionRunner.Config();
    config.setId(entity.getId().toString());
    config.setAction(DBActionTypeEnum.READ);
    Assertions.assertDoesNotThrow(() -> dbCrudActionRunner.onPersist(exchange, config));
  }

  @Test
  void givenProductInstanceId_whenUpdateRequestEntity_thenReturnOK() {
    ServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/1234"));
    HttpRequestEntity entity = DBCrudActionRunner.generateHttpRequestEntity(exchange);
    String payload = "[{\"id\":\"67ad55a4d3044d46e53dc5ab\"}]";
    exchange.getAttributes().put(KrakenFilterConstants.X_KRAKEN_RENDERED_RESPONSE_BODY, payload);
    entity.setUri("https://httpbin.org/");
    entity.setPath("/1234");
    entity.setMethod("GET");
    entity.setRequestId(UUID.randomUUID().toString());
    entity.setBizType("db persist");
    httpRequestRepository.save(entity);

    DBCrudActionRunner.Config config = new DBCrudActionRunner.Config();
    Map<String, String> env = new HashMap<>();
    env.put("productInstanceId", "${renderedResponseBody[0].id?:''}");
    config.setEnv(env);

    List<String> properties = new ArrayList<>();
    properties.add("productInstanceId");
    config.setProperties(properties);
    config.setId(entity.getId().toString());
    config.setAction(DBActionTypeEnum.UPDATE);
    exchange.getAttributes().put(KrakenFilterConstants.X_ENTITY_ID, entity.getId().toString());
    Assertions.assertDoesNotThrow(() -> dbCrudActionRunner.onPersist(exchange, config));
  }
}
