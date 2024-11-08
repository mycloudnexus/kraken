package com.consoleconnect.kraken.operator.controller;

import com.consoleconnect.kraken.operator.config.TestApplication;
import com.consoleconnect.kraken.operator.config.TestContextConstants;
import com.consoleconnect.kraken.operator.test.AbstractIntegrationTest;
import com.consoleconnect.kraken.operator.test.MockIntegrationTest;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;

@Slf4j
@MockIntegrationTest
@ContextConfiguration(classes = {TestApplication.class})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class KrakenApiUseCaseBuilderControllerTest extends AbstractIntegrationTest {
  @Autowired WebTestClient webTestClient;
  @Getter private final WebTestClientHelper testClientHelper;

  @Autowired
  public KrakenApiUseCaseBuilderControllerTest(WebTestClient webTestClient) {
    testClientHelper = new WebTestClientHelper(webTestClient);
  }

  @Test
  @Order(1)
  void givenProjectInitialized_whenBuildUseCase_thenReturnData() {
    testClientHelper.getAndVerify(
        uriBuilder ->
            uriBuilder
                .path("/products/{productId}/version-specification")
                .build(TestContextConstants.PRODUCT_ID),
        body -> {
          log.info(body);
          MatcherAssert.assertThat(body, Matchers.notNullValue());
        });
  }
}
