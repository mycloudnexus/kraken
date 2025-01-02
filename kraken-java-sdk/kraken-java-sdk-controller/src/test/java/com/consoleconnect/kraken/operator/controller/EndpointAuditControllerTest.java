package com.consoleconnect.kraken.operator.controller;

import static org.hamcrest.MatcherAssert.assertThat;

import com.consoleconnect.kraken.operator.config.TestApplication;
import com.consoleconnect.kraken.operator.controller.audit.AuditLogFilter;
import com.consoleconnect.kraken.operator.controller.audit.EndpointAuditEntity;
import com.consoleconnect.kraken.operator.controller.audit.EndpointAuditRepository;
import com.consoleconnect.kraken.operator.test.AbstractIntegrationTest;
import com.consoleconnect.kraken.operator.test.MockIntegrationTest;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;

@Slf4j
@MockIntegrationTest
@ContextConfiguration(classes = {TestApplication.class})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class EndpointAuditControllerTest extends AbstractIntegrationTest {
  private final WebTestClientHelper testClientHelper;
  @Autowired EndpointAuditRepository endpointAuditRepository;
  private static final UUID audit_uuid = UUID.randomUUID();

  @Autowired
  EndpointAuditControllerTest(WebTestClient webTestClient) {
    this.testClientHelper = new WebTestClientHelper(webTestClient);
  }

  @Order(1)
  @Test
  void givenAuditLog_whenSave_thenSuccess() {
    EndpointAuditEntity entity = new EndpointAuditEntity();
    entity.setId(audit_uuid);
    entity.setName("Admin");
    entity.setPath("/products/mef.sonata/components");
    Map<String, String> pathVariables = new HashMap<>();
    pathVariables.put("productId", "mef.sonata");
    entity.setPathVariables(pathVariables);
    entity.setRemoteAddress("127.0.0.1");
    entity.setResource("target api server");
    entity.setResourceId(UUID.randomUUID().toString());
    Map<String, String> request = new HashMap<>();
    request.put("description", "test request");
    entity.setRequest(request);

    Map<String, String> response = new HashMap<>();
    response.put("description", "test response");
    entity.setResponse(response);
    entity.setStatusCode(200);
    entity.setUserId(UUID.randomUUID().toString());
    entity.setMethod("GET");
    entity.setAction("CREATE");
    entity.setUserId("Admin");
    entity.setEmail("admin");
    entity.setDescription("create test audit log");
    entity.setName("test");
    entity.setResource("/");
    EndpointAuditEntity result = endpointAuditRepository.save(entity);
    Assertions.assertNotNull(result);
  }

  @Order(2)
  @Test
  void givenEmptyCondition_whenRequestIsCorrect_thenSuccess() {
    testClientHelper.getAndVerify(
        uriBuilder -> uriBuilder.path("/audit/logs").build(),
        bodyStr -> {
          log.info(bodyStr);
          assertThat(bodyStr, Matchers.notNullValue());
        });
    String detail = String.format("/audit/logs/%s", audit_uuid);
    testClientHelper.getAndVerify(
        uriBuilder -> uriBuilder.path(detail).build(),
        bodyStr -> {
          log.info(bodyStr);
          assertThat(bodyStr, Matchers.notNullValue());
        });
  }

  @Test
  void givenMethod_thenReturnAction() {
    Assertions.assertSame(
        AuditLogFilter.method2Action("GET"), EndpointAuditEntity.Action.READ.name());
    Assertions.assertSame(
        AuditLogFilter.method2Action("POST"), EndpointAuditEntity.Action.CREATE.name());
    Assertions.assertSame(
        AuditLogFilter.method2Action("PATCH"), EndpointAuditEntity.Action.UPDATE.name());
    Assertions.assertSame(
        AuditLogFilter.method2Action("DELETE"), EndpointAuditEntity.Action.DELETE.name());
    Assertions.assertSame(
        AuditLogFilter.method2Action("UNKNOWN"), EndpointAuditEntity.Action.UNKNOWN.name());
  }

  @Test
  @Order(3)
  void givenResourceId_whenRequestIsWrong_thenSuccess() {
    testClientHelper.getAndVerify(
        uriBuilder -> uriBuilder.path("/audit/logs/resources/12345").build(),
        bodyStr -> {
          log.info(bodyStr);
          assertThat(bodyStr, Matchers.notNullValue());
        });
  }

  @Order(4)
  @Test
  void givenLiteSearch_whenListAuditLog_thenSuccess() {
    testClientHelper.getAndVerify(
            uriBuilder -> uriBuilder.path("/audit/logs")
                    .queryParam("liteSearch", true)
                    .build(),
            bodyStr -> {
              log.info(bodyStr);
              assertThat(bodyStr, Matchers.notNullValue());
            });
    String detail = String.format("/audit/logs/%s", audit_uuid);
    testClientHelper.getAndVerify(
            uriBuilder -> uriBuilder.path(detail).build(),
            bodyStr -> {
              log.info(bodyStr);
              assertThat(bodyStr, Matchers.notNullValue());
            });
  }

}
