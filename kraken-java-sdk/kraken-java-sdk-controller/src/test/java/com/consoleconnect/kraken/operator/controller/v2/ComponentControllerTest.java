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
import java.util.function.BiConsumer;
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
                .queryParam("facetIncluded", true)
                .queryParam("parentProductType", ParentProductTypeEnum.ACCESS_ELINE.getKind())
                .build()),
        bodyStr -> {
          assertThat(bodyStr, hasJsonPath("$.data.data", hasSize(3)));
          assertThat(
              bodyStr,
              hasJsonPath("$.data.data[0].kind", is(AssetKindEnum.COMPONENT_API.getKind())));
          assertThat(bodyStr, hasJsonPath("$.data.data[0].metadata", notNullValue()));
          assertThat(bodyStr, hasJsonPath("$.data.data[0].facets", notNullValue()));
          assertThat(bodyStr, hasJsonPath("$.data.data[0].facets.mappings", notNullValue()));
        });
  }

  @Order(3)
  @Test
  void given_componentsAndParentProductType_whenSearchingByPage_thenReturnOK() {
    String basePath = String.format("%s/%s/components", PRODUCT_BASE_PATH, PRODUCT_ID);

    BiConsumer<String, Integer> assertPageSize =
        (bodyStr, expectedSize) -> {
          assertThat(bodyStr, hasJsonPath("$.data.data", hasSize(expectedSize)));
          if (expectedSize > 0) {
            assertThat(
                bodyStr,
                hasJsonPath("$.data.data[0].kind", is(AssetKindEnum.COMPONENT_API.getKind())));
            assertThat(bodyStr, hasJsonPath("$.data.data[0].metadata", notNullValue()));
            assertThat(bodyStr, hasJsonPath("$.data.data[0].facets", notNullValue()));
            assertThat(bodyStr, hasJsonPath("$.data.data[0].facets.mappings", notNullValue()));
          }
        };

    testClientHelper.getAndVerify(
        uriBuilder ->
            uriBuilder
                .path(basePath)
                .queryParam("kind", AssetKindEnum.COMPONENT_API.getKind())
                .queryParam("facetIncluded", true)
                .queryParam("parentProductType", ParentProductTypeEnum.ACCESS_ELINE.getKind())
                .queryParam("page", 0)
                .queryParam("size", 4)
                .build(),
        bodyStr -> assertPageSize.accept(bodyStr, 3));

    for (int page = 0; page <= 3; page++) {
      int expectedSize = (page < 3) ? 1 : 0; // Last page should return 0
      int finalPage = page;
      testClientHelper.getAndVerify(
          (uriBuilder ->
              uriBuilder
                  .path(basePath)
                  .queryParam("kind", AssetKindEnum.COMPONENT_API.getKind())
                  .queryParam("facetIncluded", true)
                  .queryParam("parentProductType", ParentProductTypeEnum.ACCESS_ELINE.getKind())
                  .queryParam("page", finalPage)
                  .queryParam("size", 1)
                  .build()),
          bodyStr -> assertPageSize.accept(bodyStr, expectedSize));
    }
  }
}
