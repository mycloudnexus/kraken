package com.consoleconnect.kraken.operator.controller.v2;

import static org.hamcrest.MatcherAssert.assertThat;

import com.consoleconnect.kraken.operator.config.TestApplication;
import com.consoleconnect.kraken.operator.config.TestContextConstants;
import com.consoleconnect.kraken.operator.controller.dto.CreateAPITokenRequest;
import com.consoleconnect.kraken.operator.controller.model.APIToken;
import com.consoleconnect.kraken.operator.controller.service.APITokenService;
import com.consoleconnect.kraken.operator.core.model.AppProperty;
import com.consoleconnect.kraken.operator.test.AbstractIntegrationTest;
import com.consoleconnect.kraken.operator.test.MockIntegrationTest;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;

@ActiveProfiles("test-auth-server-enabled")
@MockIntegrationTest
@ContextConfiguration(classes = {TestApplication.class})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Slf4j
class ProductAPITokenControllerTest extends AbstractIntegrationTest {
  @Autowired WebTestClient webTestClient;
  @Autowired AppProperty appProperty;

  private static final String PRODUCT_ID = TestContextConstants.PRODUCT_ID;
  public static final String ENV_ID = UUID.randomUUID().toString();
  public static final String BASE_URL = "/v2/products/" + PRODUCT_ID + "/api-tokens";

  @Autowired private APITokenService apiTokenService;

  @Order(1)
  @Test
  void testCreate() {
    CreateAPITokenRequest body = new CreateAPITokenRequest();
    body.setName("test");
    body.setEnvId(ENV_ID);
    webTestClient
        .mutate()
        .build()
        .post()
        .uri(uriBuilder -> uriBuilder.path(BASE_URL).build())
        .header("content-type", "application/json")
        .bodyValue(body)
        .exchange()
        .expectStatus()
        .isEqualTo(HttpStatus.OK);
  }

  @Order(2)
  @Test
  void testList() {
    webTestClient
        .mutate()
        .build()
        .get()
        .uri(uriBuilder -> uriBuilder.path(BASE_URL).build())
        .header("content-type", "application/json")
        .exchange()
        .expectStatus()
        .isEqualTo(HttpStatus.OK);
  }

  @Order(3)
  @Test
  void testListByEnv() {
    webTestClient
        .mutate()
        .build()
        .get()
        .uri(uriBuilder -> uriBuilder.path(BASE_URL).queryParam("envId", ENV_ID).build())
        .header("content-type", "application/json")
        .exchange()
        .expectStatus()
        .isEqualTo(HttpStatus.OK)
        .expectBody()
        .consumeWith(
            response -> {
              String bodyStr = new String(Objects.requireNonNull(response.getResponseBody()));
              log.info("{}", bodyStr);
              assertThat(bodyStr, Matchers.notNullValue());
            });
  }

  @Order(3)
  @Test
  void testFindOne_notFound() {
    String url = BASE_URL + "/" + UUID.randomUUID().toString();
    webTestClient
        .mutate()
        .build()
        .get()
        .uri(uriBuilder -> uriBuilder.path(url).build())
        .header("content-type", "application/json")
        .exchange()
        .expectStatus()
        .isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Order(3)
  @Test
  void testFindOne_shouldBeOk() {
    APIToken token = TestApplication.createAccessToken(apiTokenService);
    String url = BASE_URL + "/" + token.getId();
    webTestClient
        .mutate()
        .build()
        .get()
        .uri(uriBuilder -> uriBuilder.path(url).build())
        .header("content-type", "application/json")
        .exchange()
        .expectStatus()
        .isEqualTo(HttpStatus.OK);
  }

  @Order(3)
  @Test
  void testsRevokeToken_shouldBeOk() {
    APIToken token = TestApplication.createAccessToken(apiTokenService);
    String url = BASE_URL + "/" + token.getId();
    webTestClient
        .mutate()
        .build()
        .delete()
        .uri(uriBuilder -> uriBuilder.path(url).build())
        .header("content-type", "application/json")
        .exchange()
        .expectStatus()
        .isEqualTo(HttpStatus.OK);
  }

  @Order(4)
  @Test
  void testsRevokeToken_notFound() {
    String url = BASE_URL + "/" + UUID.randomUUID().toString();
    webTestClient
        .mutate()
        .build()
        .delete()
        .uri(uriBuilder -> uriBuilder.path(url).build())
        .header("content-type", "application/json")
        .exchange()
        .expectStatus()
        .isEqualTo(HttpStatus.NOT_FOUND);
  }
}
