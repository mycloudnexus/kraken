package com.consoleconnect.kraken.operator.controller.v2;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import com.consoleconnect.kraken.operator.config.TestApplication;
import com.consoleconnect.kraken.operator.controller.WebTestClientHelper;
import com.consoleconnect.kraken.operator.core.enums.AssetKindEnum;
import com.consoleconnect.kraken.operator.core.enums.ParentProductTypeEnum;
import com.consoleconnect.kraken.operator.test.AbstractIntegrationTest;
import com.consoleconnect.kraken.operator.test.MockIntegrationTest;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;

@MockIntegrationTest
@ContextConfiguration(classes = TestApplication.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ComponentControllerTest extends AbstractIntegrationTest {

  @Autowired WebTestClient webTestClient;

  private static final String PRODUCT_BASE_PATH = "/v2/products";
  private static final String PRODUCT_ID = "product.mef.sonata.api";
  private static final String COMPONENT_ID = "mef.sonata.api.order";

  private final WebTestClientHelper testClientHelper;

  @Autowired
  ComponentControllerTest(WebTestClient webTestClient) {
    testClientHelper = new WebTestClientHelper(webTestClient);
  }

  @Order(1)
  @Test
  void testSearchComponents() {
    String path = String.format("%s/%s/components", PRODUCT_BASE_PATH, PRODUCT_ID);
    testClientHelper.getAndVerify(
        (uriBuilder -> uriBuilder.path(path).build()),
        bodyStr -> {
          assertThat(bodyStr, hasJsonPath("$.data.data", hasSize(4)));
        });
  }

  @Order(1)
  @Test
  void testFindComponentById() {
    String path = String.format("%s/%s/components/%s", PRODUCT_BASE_PATH, PRODUCT_ID, COMPONENT_ID);
    testClientHelper.getAndVerify(
        (uriBuilder -> uriBuilder.path(path).build()),
        bodyStr -> {
          assertThat(bodyStr, hasJsonPath("$.data", notNullValue()));
        });
  }

  @Order(2)
  @Test
  void given_componentsAndParentProductType_whenSearching_thenReturnOK() {
    String path = String.format("%s/%s/components", PRODUCT_BASE_PATH, PRODUCT_ID);
    testClientHelper.getAndVerify(
        (uriBuilder ->
            uriBuilder
                .path(path)
                .queryParam("kind", AssetKindEnum.COMPONENT_API.getKind())
                .queryParam("parentProductType", ParentProductTypeEnum.ACCESS_ELINE.getKind())
                .build()),
        bodyStr -> {
          assertThat(bodyStr, hasJsonPath("$.data.data", hasSize(1)));
          assertThat(
              bodyStr,
              hasJsonPath("$.data.data[0].kind", is(AssetKindEnum.COMPONENT_API.getKind())));
          assertThat(bodyStr, hasJsonPath("$.data.data[0].metadata", notNullValue()));
          assertThat(bodyStr, hasJsonPath("$.data.data[0].facets", notNullValue()));
          assertThat(bodyStr, hasJsonPath("$.data.data[0].facets.mappings", notNullValue()));
          assertThat(bodyStr, hasJsonPath("$.data.data[0].facets.apiSpec.key", notNullValue()));
        });
  }
}
