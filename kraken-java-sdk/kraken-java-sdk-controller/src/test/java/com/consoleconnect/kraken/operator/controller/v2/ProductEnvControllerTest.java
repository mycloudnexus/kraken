package com.consoleconnect.kraken.operator.controller.v2;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import com.consoleconnect.kraken.operator.auth.security.UserContext;
import com.consoleconnect.kraken.operator.config.TestApplication;
import com.consoleconnect.kraken.operator.config.TestContextConstants;
import com.consoleconnect.kraken.operator.controller.WebTestClientHelper;
import com.consoleconnect.kraken.operator.controller.dto.CreateEnvRequest;
import com.consoleconnect.kraken.operator.controller.repo.EnvironmentRepository;
import com.consoleconnect.kraken.operator.core.model.AppProperty;
import com.consoleconnect.kraken.operator.test.AbstractIntegrationTest;
import com.consoleconnect.kraken.operator.test.MockIntegrationTest;
import java.util.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;

@MockIntegrationTest
@ContextConfiguration(classes = {TestApplication.class})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ProductEnvControllerTest extends AbstractIntegrationTest {
  @Autowired WebTestClient webTestClient;
  @Autowired AppProperty appProperty;
  @Autowired EnvironmentRepository environmentRepository;

  private final WebTestClientHelper webTestClientHelper;

  private static final String PRODUCT_ID = TestContextConstants.PRODUCT_ID;
  public static final String BASE_URL = "/v2/products/" + PRODUCT_ID + "/environments";

  @Autowired
  public ProductEnvControllerTest(WebTestClient testClient) {
    this.webTestClientHelper = new WebTestClientHelper(testClient);
  }

  @Order(1)
  @Test
  void givenEnvName_whenCreate_thenReturnOk() {
    CreateEnvRequest body = new CreateEnvRequest();
    body.setName(UUID.randomUUID().toString());
    webTestClientHelper.postAndVerify(
        uriBuilder -> uriBuilder.path(BASE_URL).build(),
        body,
        bodyStr -> {
          Assertions.assertNotNull(bodyStr);
          assertThat(bodyStr, hasJsonPath("$.data", notNullValue()));
          assertThat(bodyStr, hasJsonPath("$.data.metadata.id", notNullValue()));
          assertThat(bodyStr, hasJsonPath("$.data.metadata.name", is(body.getName())));
          assertThat(bodyStr, hasJsonPath("$.data.createdBy", is(UserContext.ANONYMOUS)));
          assertThat(bodyStr, hasJsonPath("$.data.createdAt", notNullValue()));
        });
  }

  @Order(3)
  @Test
  void givenEnvCreated_whenList_thenReturnOK() {
    webTestClientHelper.getAndVerify(
        uriBuilder -> uriBuilder.path(BASE_URL).build(),
        bodyStr -> {
          assertThat(bodyStr, hasJsonPath("$.data", notNullValue()));
          assertThat(bodyStr, hasJsonPath("$.data.data", hasSize(greaterThanOrEqualTo(1))));
        });
  }
}
