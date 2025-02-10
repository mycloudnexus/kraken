package com.consoleconnect.kraken.operator;

import com.consoleconnect.kraken.operator.service.SellerContactsInitializer;
import com.consoleconnect.kraken.operator.test.AbstractIntegrationTest;
import com.consoleconnect.kraken.operator.test.MockIntegrationTest;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

@Slf4j
@ActiveProfiles({"default-seller", "test-no-auth"})
@MockIntegrationTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@AutoConfigureObservability
class SellerContactsInitializerTest extends AbstractIntegrationTest {
  private static final String PRODUCT_ID = "mef.sonata";
  @Autowired private SellerContactsInitializer sellerContactsInitializer;
  @Autowired private WebTestClient webTestClient;

  @Test
  void givenSellerContactsTemplate_whenInitialize_thenReturnOK() {
    sellerContactsInitializer.initialize();
    String path = String.format("/products/%s/components", PRODUCT_ID);
    WebTestClient.RequestBodySpec requestBodySpec =
        webTestClient
            .method(HttpMethod.GET)
            .uri(
                uriBuilder ->
                    uriBuilder
                        .path(path)
                        .queryParam("kind", "kraken.component.seller-contact")
                        .build())
            .header("content-type", "application/json");
    requestBodySpec
        .exchange()
        .expectStatus()
        .isEqualTo(HttpStatus.OK)
        .expectBody()
        .consumeWith(
            response -> {
              String bodyStr = new String(Objects.requireNonNull(response.getResponseBody()));
              log.info(bodyStr);
            });
  }
}
