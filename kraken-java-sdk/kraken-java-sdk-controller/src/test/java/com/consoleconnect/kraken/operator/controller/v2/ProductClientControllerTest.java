package com.consoleconnect.kraken.operator.controller.v2;

import static org.hamcrest.MatcherAssert.assertThat;

import com.consoleconnect.kraken.operator.config.TestApplication;
import com.consoleconnect.kraken.operator.test.AbstractIntegrationTest;
import com.consoleconnect.kraken.operator.test.MockIntegrationTest;
import java.time.Duration;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;

@MockIntegrationTest
@ContextConfiguration(classes = {TestApplication.class})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Slf4j
class ProductClientControllerTest extends AbstractIntegrationTest {
  @Autowired WebTestClient webTestClient;

  public static final String BASE_URL = "/v2/products/{productId}/clients";
  private static final String PRODUCT_ID = "product.mef.sonata.api";

  @Order(1)
  @Test
  void testListAllClients() {
    webTestClient
        .mutate()
        .responseTimeout(Duration.ofSeconds(600))
        .build()
        .get()
        .uri(uriBuilder -> uriBuilder.path(BASE_URL).build(PRODUCT_ID))
        .exchange()
        .expectBody()
        .consumeWith(
            response -> {
              String bodyStr = new String(Objects.requireNonNull(response.getResponseBody()));
              log.info("{}", bodyStr);
              assertThat(bodyStr, Matchers.notNullValue());
            });
  }
}
