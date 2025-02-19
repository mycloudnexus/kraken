package com.consoleconnect.kraken.operator.controller;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import com.consoleconnect.kraken.operator.config.TestApplication;
import com.consoleconnect.kraken.operator.controller.dto.CreateAPITokenRequest;
import com.consoleconnect.kraken.operator.controller.model.APIToken;
import com.consoleconnect.kraken.operator.controller.service.APITokenService;
import com.consoleconnect.kraken.operator.controller.service.ComponentAPIServerService;
import com.consoleconnect.kraken.operator.core.enums.AssetKindEnum;
import com.consoleconnect.kraken.operator.test.AbstractIntegrationTest;
import com.consoleconnect.kraken.operator.test.MockIntegrationTest;
import java.time.Duration;
import java.util.*;
import java.util.function.Consumer;
import lombok.extern.slf4j.Slf4j;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

@ActiveProfiles("test-hs512")
@Slf4j
@MockIntegrationTest
@ContextConfiguration(classes = {TestApplication.class})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AuditCollectorControllerTest extends AbstractIntegrationTest {
  @Autowired WebTestClient webTestClient;
  @Autowired APITokenService apiTokenService;
  @Autowired ComponentAPIServerService componentAPIServerService;

  public static final String PRODUCT_ID = "mef.sonata";
  public static final String SYN_API_SERVER_URL = "/v2/callback/audits/api-servers";
  public static final String SYNC_FROM_SERVER = "/v2/callback/audits/sync-server-asset";

  public static String accessToken;

  private APIToken createToken(String envId) {
    CreateAPITokenRequest body = new CreateAPITokenRequest();
    body.setName("Token-" + System.currentTimeMillis());
    body.setEnvId(envId);
    return apiTokenService.createToken(PRODUCT_ID, body, null);
  }

  @BeforeEach
  public void setup() {
    if (accessToken == null) {
      accessToken = "Bearer " + createToken(TestApplication.envId).getToken();
    }
  }

  @Test
  @Order(3)
  void testSynApiServers() {
    webTestClient
        .mutate()
        .responseTimeout(Duration.ofSeconds(600))
        .build()
        .get()
        .uri(uriBuilder -> uriBuilder.path(SYN_API_SERVER_URL).build())
        .header("Authorization", accessToken)
        .exchange()
        .expectBody()
        .consumeWith(
            response -> {
              String bodyStr = new String(Objects.requireNonNull(response.getResponseBody()));
              log.info("query api server info {}", bodyStr);
              assertThat(bodyStr, Matchers.notNullValue());
            });
    assertThat(componentAPIServerService, Matchers.notNullValue());
    Mono.delay(Duration.ofSeconds(2))
        .subscribe(t -> componentAPIServerService.buildAPIServerCache());
  }

  @Test
  @Order(4)
  void testInvalidTokens() {
    Consumer<String> consumer =
        url -> {
          webTestClient
              .mutate()
              .responseTimeout(Duration.ofSeconds(600))
              .build()
              .get()
              .uri(uriBuilder -> uriBuilder.path(url).build())
              .header("Authorization", "")
              .exchange()
              .expectStatus()
              .isUnauthorized();
        };
    consumer.accept(SYN_API_SERVER_URL);
  }

  @Test
  @Order(5)
  void testSyncBuyer() {
    webTestClient
        .mutate()
        .responseTimeout(Duration.ofSeconds(600))
        .build()
        .get()
        .uri(
            uriBuilder ->
                uriBuilder
                    .path(SYNC_FROM_SERVER)
                    .queryParam("kind", AssetKindEnum.PRODUCT_BUYER.getKind())
                    .queryParam("updatedAt", "")
                    .build())
        .header("Authorization", accessToken)
        .exchange()
        .expectBody()
        .consumeWith(
            response -> {
              String bodyStr = new String(Objects.requireNonNull(response.getResponseBody()));
              log.info("searched buyer:{}", bodyStr);
              assertThat(bodyStr, Matchers.notNullValue());
              assertThat(bodyStr, hasJsonPath("$.code", is(200)));
            });
  }

  @Test
  @Order(6)
  void testSyncApiServerUrl() {
    webTestClient
        .mutate()
        .responseTimeout(Duration.ofSeconds(600))
        .build()
        .get()
        .uri(
            uriBuilder ->
                uriBuilder
                    .path(SYNC_FROM_SERVER)
                    .queryParam("kind", AssetKindEnum.COMPONENT_API_SERVER.getKind())
                    .build())
        .header("Authorization", accessToken)
        .exchange()
        .expectBody()
        .consumeWith(
            response -> {
              String bodyStr = new String(Objects.requireNonNull(response.getResponseBody()));
              assertThat(bodyStr, Matchers.notNullValue());
            });
  }

  @Test
  @Order(7)
  void testSyncSellerContacts() {
    webTestClient
        .mutate()
        .responseTimeout(Duration.ofSeconds(600))
        .build()
        .get()
        .uri(
            uriBuilder ->
                uriBuilder
                    .path(SYNC_FROM_SERVER)
                    .queryParam("kind", AssetKindEnum.COMPONENT_SELLER_CONTACT.getKind())
                    .build())
        .header("Authorization", accessToken)
        .exchange()
        .expectBody()
        .consumeWith(
            response -> {
              String bodyStr = new String(Objects.requireNonNull(response.getResponseBody()));
              assertThat(bodyStr, Matchers.notNullValue());
            });
  }

  @Test
  @Order(7)
  void test4xxHttpGet() {
    webTestClient
        .mutate()
        .responseTimeout(Duration.ofSeconds(600))
        .build()
        .get()
        .uri(
            uriBuilder ->
                uriBuilder
                    .path(SYNC_FROM_SERVER)
                    .queryParam("kind1", AssetKindEnum.COMPONENT_API_SERVER.getKind())
                    .build())
        .header("Authorization", accessToken)
        .exchange()
        .expectBody()
        .consumeWith(
            response -> {
              String bodyStr = new String(Objects.requireNonNull(response.getResponseBody()));
              log.info("bodyStr:{}", bodyStr);
              assertThat(bodyStr, Matchers.notNullValue());
              assertThat(bodyStr, hasJsonPath("$.code", not(200)));
            });
  }
}
