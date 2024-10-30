package com.consoleconnect.kraken.operator;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import com.consoleconnect.kraken.operator.config.AppMgmtConfig;
import com.consoleconnect.kraken.operator.config.AppMgmtProperty;
import com.consoleconnect.kraken.operator.controller.dto.CreateEnvRequest;
import com.consoleconnect.kraken.operator.test.AbstractIntegrationTest;
import com.consoleconnect.kraken.operator.test.MockIntegrationTest;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

@ActiveProfiles({"default-env", "test-no-auth"})
@MockIntegrationTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@AutoConfigureObservability
class EnvironmentTest extends AbstractIntegrationTest {

  @Autowired WebTestClient webTestClient;

  @Autowired private AppMgmtProperty appMgmtProperty;

  @Test
  void testPropertyConfig() {
    AppMgmtConfig config = new AppMgmtConfig();
    AppMgmtProperty property = config.appMgmtProperty();
    Assertions.assertNotNull(property);
    AppMgmtProperty.Product env = new AppMgmtProperty.Product();
    env.setKey(UUID.randomUUID().toString());
    CreateEnvRequest createEnvRequest = new CreateEnvRequest();
    createEnvRequest.setName("dev");
    env.setEnvironments(List.of(createEnvRequest));
    property.setProducts(List.of(env));
    Assertions.assertNotNull(property.getProducts());
  }

  @Test
  void listDefaultEnvironments() {
    Assertions.assertNotNull(appMgmtProperty.getProducts());
    Assertions.assertEquals(1, appMgmtProperty.getProducts().size());
    Assertions.assertEquals("mef.sonata", appMgmtProperty.getProducts().get(0).getKey());
    Assertions.assertEquals(2, appMgmtProperty.getProducts().get(0).getEnvironments().size());

    String endpoints = "/products/mef.sonata/envs";
    WebTestClient.RequestBodySpec requestBodySpec =
        webTestClient
            .method(HttpMethod.GET)
            .uri(uriBuilder -> uriBuilder.path(endpoints).build())
            .header("content-type", "application/json");
    requestBodySpec
        .exchange()
        .expectStatus()
        .isEqualTo(HttpStatus.OK)
        .expectBody()
        .consumeWith(
            response -> {
              String bodyStr = new String(Objects.requireNonNull(response.getResponseBody()));
              assertThat(bodyStr, hasJsonPath("$.data.data", hasSize(2)));
              assertThat(bodyStr, hasJsonPath("$.data.data[0].name", notNullValue()));
              assertThat(bodyStr, hasJsonPath("$.data.data[0].name", is("production")));
              assertThat(bodyStr, hasJsonPath("$.data.data[1].name", notNullValue()));
              assertThat(bodyStr, hasJsonPath("$.data.data[1].name", is("stage")));
            });
  }
}
