package com.consoleconnect.kraken.operator.controller;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import com.consoleconnect.kraken.operator.config.TestApplication;
import com.consoleconnect.kraken.operator.controller.dto.CreateEnvRequest;
import com.consoleconnect.kraken.operator.test.AbstractIntegrationTest;
import com.consoleconnect.kraken.operator.test.MockIntegrationTest;
import java.util.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;

@MockIntegrationTest
@ContextConfiguration(classes = {TestApplication.class})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class EnvControllerTest extends AbstractIntegrationTest {
  private final WebTestClientHelper webTestClientHelper;

  public static final String PRODUCT_ID = "mef.sonata";
  public static final String BASE_URL = "/products/" + PRODUCT_ID + "/envs";

  @Autowired
  public EnvControllerTest(WebTestClient testClient) {
    this.webTestClientHelper = new WebTestClientHelper(testClient);
  }

  @Order(1)
  @Test
  void givenCorrectRequestPayload_whenCreate_thenReturnOk() {
    CreateEnvRequest body = new CreateEnvRequest();
    body.setName(UUID.randomUUID().toString());
    webTestClientHelper.postAndVerify(
        uriBuilder -> uriBuilder.path(BASE_URL).build(),
        body,
        bodyStr -> {
          Assertions.assertNotNull(bodyStr);
          assertThat(bodyStr, hasJsonPath("$.data", notNullValue()));
          assertThat(bodyStr, hasJsonPath("$.data.name", is(body.getName())));
        });
  }

  @Order(2)
  @Test
  void givenNotExistId_whenFindOne_thenReturn404() {
    String url = BASE_URL + "/" + UUID.randomUUID().toString();
    webTestClientHelper.requestAndVerify(
        HttpMethod.GET,
        uriBuilder -> uriBuilder.path(url).build(),
        HttpStatus.NOT_FOUND.value(),
        null,
        Assertions::assertNotNull);
  }

  @Order(3)
  @Test
  void givenEnvCreated_thenList_shouldReturnOk() {
    webTestClientHelper.getAndVerify(
        uriBuilder -> uriBuilder.path(BASE_URL).build(),
        bodyStr -> {
          assertThat(bodyStr, hasJsonPath("$.data", notNullValue()));
          assertThat(bodyStr, hasJsonPath("$.data.data", hasSize(greaterThanOrEqualTo(1))));
        });
  }

  @Order(3)
  @Test
  void givenNotExistProductId_whenSearch_thenReturnEmpty() {
    this.webTestClientHelper.getAndVerify(
        uriBuilder ->
            uriBuilder.path("/products/" + UUID.randomUUID().toString() + "/envs").build(),
        bodyStr -> {
          assertThat(bodyStr, hasJsonPath("$.data.data", hasSize(0)));
        });
  }
}
