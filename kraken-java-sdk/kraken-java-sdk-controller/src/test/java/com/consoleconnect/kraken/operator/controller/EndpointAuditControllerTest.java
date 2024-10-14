package com.consoleconnect.kraken.operator.controller;

import static org.hamcrest.MatcherAssert.assertThat;

import com.consoleconnect.kraken.operator.config.TestApplication;
import com.consoleconnect.kraken.operator.controller.audit.AuditLogFilter;
import com.consoleconnect.kraken.operator.controller.audit.EndpointAuditEntity;
import com.consoleconnect.kraken.operator.test.AbstractIntegrationTest;
import com.consoleconnect.kraken.operator.test.MockIntegrationTest;
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

  @Autowired
  EndpointAuditControllerTest(WebTestClient webTestClient) {
    this.testClientHelper = new WebTestClientHelper(webTestClient);
  }

  @Order(1)
  @Test
  void givenEmptyCondition_whenRequestIsCorrect_thenSuccess() {
    testClientHelper.getAndVerify(
        uriBuilder -> uriBuilder.path("/audit/logs").build(),
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
  void givenResourceId_whenRequestIsWrong_thenSuccess() {
    testClientHelper.getAndVerify(
        uriBuilder -> uriBuilder.path("/audit/logs/resources/12345").build(),
        bodyStr -> {
          log.info(bodyStr);
          assertThat(bodyStr, Matchers.notNullValue());
        });
  }
}
