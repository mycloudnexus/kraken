package com.consoleconnect.kraken.operator.core.controller;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasNoJsonPath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import com.consoleconnect.kraken.operator.core.CustomConfig;
import com.consoleconnect.kraken.operator.core.enums.AssetKindEnum;
import com.consoleconnect.kraken.operator.core.helper.WebTestClientHelper;
import com.consoleconnect.kraken.operator.test.AbstractIntegrationTest;
import com.consoleconnect.kraken.operator.test.MockIntegrationTest;
import java.util.UUID;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;

@MockIntegrationTest
@ContextConfiguration(classes = CustomConfig.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ProductControllerTest extends AbstractIntegrationTest {

  @Autowired WebTestClient webTestClient;

  private static final String PRODUCT_BASE_PATH = "/products";
  private static final String PRODUCT_ID = "product.mef.sonata.api";

  private final WebTestClientHelper testClientHelper;

  @Autowired
  ProductControllerTest(WebTestClient webTestClient) {
    testClientHelper = new WebTestClientHelper(webTestClient);
  }

  @Order(1)
  @Test
  void testListProducts() {
    testClientHelper.getAndVerify(
        (uriBuilder -> uriBuilder.path(PRODUCT_BASE_PATH).build()),
        bodyStr -> {
          assertThat(
              bodyStr, hasJsonPath("$.data.data[0].kind", is(AssetKindEnum.PRODUCT.getKind())));
          assertThat(bodyStr, hasJsonPath("$.data.data", hasSize(1)));
          assertThat(bodyStr, hasJsonPath("$.data.data[0].metadata", notNullValue()));
          assertThat(bodyStr, hasJsonPath("$.data.data[0].facets", notNullValue()));
          assertThat(
              bodyStr,
              hasJsonPath(
                  "$.data.data[0].facets.componentPaths", hasSize(Matchers.greaterThan(1))));
        });
  }

  @Order(2)
  @Test
  void testListProductWithoutFacets() {
    testClientHelper.getAndVerify(
        (uriBuilder ->
            uriBuilder.path(PRODUCT_BASE_PATH).queryParam("facetIncluded", false).build()),
        bodyStr -> {
          assertThat(
              bodyStr, hasJsonPath("$.data.data[0].kind", is(AssetKindEnum.PRODUCT.getKind())));
          assertThat(bodyStr, hasJsonPath("$.data.data", hasSize(1)));
          assertThat(bodyStr, hasJsonPath("$.data.data[0].metadata", notNullValue()));
          assertThat(bodyStr, hasNoJsonPath("$.data.data[0].facets"));
        });
  }

  @Order(3)
  @Test
  void testRetrieveAProductDetail() {
    String path = String.format("%s/%s", PRODUCT_BASE_PATH, PRODUCT_ID);
    testClientHelper.getAndVerify(
        (uriBuilder -> uriBuilder.path(path).build()),
        bodyStr -> {
          assertThat(bodyStr, hasJsonPath("$.data.kind", is(AssetKindEnum.PRODUCT.getKind())));
          assertThat(bodyStr, hasJsonPath("$.data.metadata", notNullValue()));
          assertThat(bodyStr, hasJsonPath("$.data.facets", notNullValue()));
          assertThat(
              bodyStr,
              hasJsonPath("$.data.facets.componentPaths", hasSize(Matchers.greaterThan(4))));
        });
  }

  @Order(3)
  @Test
  void testRetrieveAProductDetailByWrongId() {
    String path = String.format("%s/%s", PRODUCT_BASE_PATH, UUID.randomUUID().toString());
    testClientHelper.getAndVerify(
        (uriBuilder -> uriBuilder.path(path).build()), HttpStatus.NOT_FOUND, null);
    Assertions.assertTrue(true);
  }
}
