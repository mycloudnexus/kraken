package com.consoleconnect.kraken.operator.controller;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasNoJsonPath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import com.consoleconnect.kraken.operator.config.TestApplication;
import com.consoleconnect.kraken.operator.controller.dto.CreateAPITokenRequest;
import com.consoleconnect.kraken.operator.controller.model.APIToken;
import com.consoleconnect.kraken.operator.controller.service.APITokenService;
import com.consoleconnect.kraken.operator.test.AbstractIntegrationTest;
import com.consoleconnect.kraken.operator.test.MockIntegrationTest;
import java.util.*;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;

@ActiveProfiles("test-auth-server-enabled")
@MockIntegrationTest
@ContextConfiguration(classes = {TestApplication.class})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class EnvAccessTokenControllerTest extends AbstractIntegrationTest {
  @Autowired WebTestClient webTestClient;
  public static final String PRODUCT_ID = "mef.sonata";
  public static final String ENV_ID = UUID.randomUUID().toString();
  public static final String BASE_URL =
      "/products/" + PRODUCT_ID + "/envs/" + ENV_ID + "/api-tokens";

  public static final String LIST_ALL_URL = "/products/" + PRODUCT_ID + "/env-api-tokens";

  @Autowired private APITokenService apiTokenService;

  private final WebTestClientHelper webTestClientHelper;

  @Autowired
  public EnvAccessTokenControllerTest(WebTestClient testClient) {
    this.webTestClientHelper = new WebTestClientHelper(testClient);
  }

  @Order(1)
  @Test
  void givenCorrectRequestPayload_whenCreate_thenReturnOk() {
    CreateAPITokenRequest body = new CreateAPITokenRequest();
    body.setName("token-" + System.currentTimeMillis());
    this.webTestClientHelper.postAndVerify(
        uriBuilder -> uriBuilder.path(BASE_URL).build(),
        body,
        bodyStr -> {
          assertThat(bodyStr, Matchers.notNullValue());
          assertThat(bodyStr, hasJsonPath("$.data", notNullValue()));
          assertThat(bodyStr, hasJsonPath("$.data.id", notNullValue()));
          assertThat(bodyStr, hasJsonPath("$.data.token", notNullValue()));
        });
  }

  @Order(2)
  @Test
  void givenTokenCreated_whenList_thenReturnOk() {
    this.webTestClientHelper.getAndVerify(
        uriBuilder -> uriBuilder.path(LIST_ALL_URL).build(),
        bodyStr -> {
          assertThat(bodyStr, Matchers.notNullValue());
          assertThat(
              bodyStr, hasJsonPath("$.data.data", Matchers.hasSize(Matchers.greaterThan(0))));
        });
  }

  @Order(3)
  @Test
  void givenNotExistTokenId_whenFindOne_thenReturn404() {
    String url = BASE_URL + "/" + UUID.randomUUID().toString();
    this.webTestClientHelper.requestAndVerify(
        HttpMethod.GET,
        uriBuilder -> uriBuilder.path(url).build(),
        HttpStatus.NOT_FOUND.value(),
        null,
        Assertions::assertNotNull);
  }

  @Order(3)
  @Test
  void giveCorrectTokenId_whenFindOne_thenReturnOk() {
    APIToken token = createToken();
    String url = BASE_URL + "/" + token.getId();
    webTestClientHelper.getAndVerify(
        uriBuilder -> uriBuilder.path(url).build(),
        bodyStr -> {
          assertThat(bodyStr, Matchers.notNullValue());
          assertThat(bodyStr, hasJsonPath("$.data", notNullValue()));
          assertThat(bodyStr, hasJsonPath("$.data.id", notNullValue()));
          assertThat(bodyStr, hasJsonPath("$.data.revoked", is(false)));
          assertThat(bodyStr, hasNoJsonPath("$.data.token"));
        });
  }

  @Order(3)
  @Test
  void givenCorrectTokenId_whenRevokeIt_thenReturnOK() {
    APIToken token = createToken();
    String url = BASE_URL + "/" + token.getId();
    webTestClientHelper.requestAndVerify(
        HttpMethod.DELETE,
        uriBuilder -> uriBuilder.path(url).build(),
        HttpStatus.OK.value(),
        null,
        bodyStr -> {
          assertThat(bodyStr, Matchers.notNullValue());
          assertThat(bodyStr, hasJsonPath("$.data", notNullValue()));
          assertThat(bodyStr, hasJsonPath("$.data.id", notNullValue()));
          assertThat(bodyStr, hasJsonPath("$.data.revoked", is(true)));
          assertThat(bodyStr, hasJsonPath("$.data.revokedAt", notNullValue()));
          assertThat(bodyStr, hasNoJsonPath("$.data.token"));
        });
  }

  @Order(4)
  @Test
  void givenNotExistTokenId_whenRevokeIt_thenReturn404() {
    String url = BASE_URL + "/" + UUID.randomUUID().toString();
    webTestClientHelper.requestAndVerify(
        HttpMethod.DELETE,
        uriBuilder -> uriBuilder.path(url).build(),
        HttpStatus.NOT_FOUND.value(),
        null,
        Assertions::assertNotNull);
  }

  private APIToken createToken() {
    CreateAPITokenRequest body = new CreateAPITokenRequest();
    body.setName("test");
    body.setEnvId(ENV_ID);

    return apiTokenService.createToken(PRODUCT_ID, body, System.currentTimeMillis() + "");
  }
}
