package com.consoleconnect.kraken.operator.controller.v2;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import com.consoleconnect.kraken.operator.config.TestApplication;
import com.consoleconnect.kraken.operator.config.TestContextConstants;
import com.consoleconnect.kraken.operator.controller.WebTestClientHelper;
import com.consoleconnect.kraken.operator.controller.dto.CreateTagRequest;
import com.consoleconnect.kraken.operator.test.AbstractIntegrationTest;
import com.consoleconnect.kraken.operator.test.MockIntegrationTest;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;

@MockIntegrationTest
@ContextConfiguration(classes = TestApplication.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ComponentTagControllerTest extends AbstractIntegrationTest {

  private static final String PRODUCT_BASE_PATH = "/v2/products";
  private static final String PRODUCT_ID = TestContextConstants.PRODUCT_ID;
  private static final String COMPONENT_ID = "mef.sonata.api.order";

  private final WebTestClientHelper testClientHelper;

  @Autowired
  ComponentTagControllerTest(WebTestClient webTestClient) {
    testClientHelper = new WebTestClientHelper(webTestClient);
  }

  @Order(1)
  @Test
  void givenComponentId_whenCreate_thenReturnOk() {
    String path =
        String.format("%s/%s/components/%s/tags", PRODUCT_BASE_PATH, PRODUCT_ID, COMPONENT_ID);

    CreateTagRequest request = new CreateTagRequest();
    request.setTag("0.0.0");
    request.setName("this is a readable name");
    request.setDescription("this is a short description");
    testClientHelper.postAndVerify(
        (uriBuilder -> uriBuilder.path(path).build()),
        request,
        bodyStr -> {
          assertThat(bodyStr, hasJsonPath("$.data", notNullValue()));
        });
  }

  @Order(2)
  @Test
  void givenTagCreated_whenList_shouldReturnOk() {
    String path =
        String.format("%s/%s/components/%s/tags", PRODUCT_BASE_PATH, PRODUCT_ID, COMPONENT_ID);
    testClientHelper.getAndVerify(
        (uriBuilder -> uriBuilder.path(path).build()),
        bodyStr -> {
          assertThat(bodyStr, hasJsonPath("$.data.data", hasSize(Matchers.greaterThan(0))));
        });
  }
}
