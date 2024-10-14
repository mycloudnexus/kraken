package com.consoleconnect.kraken.operator.core.controller;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasNoJsonPath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import com.consoleconnect.kraken.operator.core.CustomConfig;
import com.consoleconnect.kraken.operator.core.enums.APISpecEnum;
import com.consoleconnect.kraken.operator.core.enums.AssetKindEnum;
import com.consoleconnect.kraken.operator.core.helper.WebTestClientHelper;
import com.consoleconnect.kraken.operator.test.AbstractIntegrationTest;
import com.consoleconnect.kraken.operator.test.MockIntegrationTest;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;

@MockIntegrationTest
@ContextConfiguration(classes = CustomConfig.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AssetControllerTest extends AbstractIntegrationTest {

  @Autowired WebTestClient webTestClient;

  private static final String PRODUCT_ID = "product.mef.sonata.api";

  private static final String BASE64_PATTERN =
      "^(([\\w+/]{4}){19}\r\n)*(([\\w+/]{4})*([\\w+/]{4}|[\\w+/]{3}=|[\\w+/]{2}==))$";

  private final WebTestClientHelper testClientHelper;

  @Autowired
  AssetControllerTest(WebTestClient webTestClient) {
    testClientHelper = new WebTestClientHelper(webTestClient);
  }

  @Order(1)
  @Test
  void testSearchAssets() {
    String path = "/assets";
    testClientHelper.getAndVerify(
        (uriBuilder -> uriBuilder.path(path).build()),
        bodyStr -> {
          assertThat(bodyStr, hasJsonPath("$.data.data", hasSize(Matchers.greaterThan(2))));
          assertThat(bodyStr, hasJsonPath("$.data.data[0].kind", notNullValue()));
          assertThat(bodyStr, hasJsonPath("$.data.data[0].metadata", notNullValue()));
          assertThat(bodyStr, hasJsonPath("$.data.data[0].facets", notNullValue()));
        });
  }

  @Order(2)
  @Test
  void testSearchAssetsWithoutFacets() {
    String path = "/assets";
    testClientHelper.getAndVerify(
        (uriBuilder -> uriBuilder.path(path).queryParam("facetIncluded", false).build()),
        bodyStr -> {
          assertThat(bodyStr, hasJsonPath("$.data.data", hasSize(Matchers.greaterThan(2))));
          assertThat(bodyStr, hasJsonPath("$.data.data[0].kind", notNullValue()));
          assertThat(bodyStr, hasJsonPath("$.data.data[0].metadata", notNullValue()));
          assertThat(bodyStr, hasNoJsonPath("$.data.data[0].facets"));
        });
  }

  @Order(3)
  @Test
  void testSearchAPISpecs() {
    String path = "/assets";
    testClientHelper.getAndVerify(
        (uriBuilder ->
            uriBuilder
                .path(path)
                .queryParam("kind", AssetKindEnum.COMPONENT_API_SPEC.getKind())
                .build()),
        bodyStr -> {
          assertThat(bodyStr, hasJsonPath("$.data.data", hasSize(1)));
          assertThat(
              bodyStr,
              hasJsonPath("$.data.data[0].kind", is(AssetKindEnum.COMPONENT_API_SPEC.getKind())));
          assertThat(bodyStr, hasJsonPath("$.data.data[0].metadata", notNullValue()));
          assertThat(bodyStr, hasJsonPath("$.data.data[0].facets", notNullValue()));
          assertThat(bodyStr, hasJsonPath("$.data.data[0].facets.baseSpec.path", notNullValue()));
          assertThat(
              bodyStr, hasJsonPath("$.data.data[0].facets.baseSpec.content", notNullValue()));
          assertThat(
              bodyStr, hasJsonPath("$.data.data[0].facets.customizedSpec.path", notNullValue()));
          assertThat(
              bodyStr, hasJsonPath("$.data.data[0].facets.customizedSpec.content", notNullValue()));
          // the content MUST be based64 encoded
          assertThat(
              bodyStr,
              hasJsonPath(
                  "$.data.data[0].facets.baseSpec.content", matchesPattern(BASE64_PATTERN)));
          assertThat(
              bodyStr,
              hasJsonPath(
                  "$.data.data[0].facets.customizedSpec.content", matchesPattern(BASE64_PATTERN)));
        });
  }

  @Order(4)
  @Test
  void testSearchAPIs() {
    String path = "/assets";
    testClientHelper.getAndVerify(
        (uriBuilder ->
            uriBuilder
                .path(path)
                .queryParam("kind", AssetKindEnum.COMPONENT_API.getKind())
                .queryParam("parentId", PRODUCT_ID)
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

  @Order(8)
  @Test
  void testListProductComponentTransformer() {
    String path = "/assets";
    testClientHelper.getAndVerify(
        (uriBuilder ->
            uriBuilder
                .path(path)
                .queryParam("kind", AssetKindEnum.COMPONENT_TRANSFORMER.getKind())
                .queryParam("parentId", PRODUCT_ID)
                .build()),
        bodyStr -> {
          assertThat(bodyStr, hasJsonPath("$.data.data", hasSize(2)));
          assertThat(
              bodyStr,
              hasJsonPath(
                  "$.data.data[0].kind", is(AssetKindEnum.COMPONENT_TRANSFORMER.getKind())));
          assertThat(bodyStr, hasJsonPath("$.data.data[0].metadata", notNullValue()));
          assertThat(bodyStr, hasJsonPath("$.data.data[0].facets", notNullValue()));
          assertThat(bodyStr, hasJsonPath("$.data.data[0].facets.script", notNullValue()));
          assertThat(bodyStr, hasJsonPath("$.data.data[0].facets.script.path", notNullValue()));
          assertThat(bodyStr, hasJsonPath("$.data.data[0].facets.script.code", notNullValue()));
          // the code MUST be based64 encoded
          assertThat(
              bodyStr,
              hasJsonPath("$.data.data[0].facets.script.code", matchesPattern(BASE64_PATTERN)));
        });
  }

  @Order(5)
  @Test
  void testRetrieveAssetById() {

    String path = String.format("/assets/%s", "mef.sonata.api-spec.order");
    testClientHelper.getAndVerify(
        (uriBuilder -> uriBuilder.path(path).build()),
        bodyStr -> {
          assertThat(bodyStr, hasJsonPath("$.data", notNullValue()));
          assertThat(
              bodyStr, hasJsonPath("$.data.kind", is(AssetKindEnum.COMPONENT_API_SPEC.getKind())));
          assertThat(bodyStr, hasJsonPath("$.data.metadata", notNullValue()));
          assertThat(bodyStr, hasJsonPath("$.data.facets", notNullValue()));
          assertThat(bodyStr, hasJsonPath("$.data.facets.baseSpec.path", notNullValue()));
          assertThat(bodyStr, hasJsonPath("$.data.facets.baseSpec.content", notNullValue()));
          assertThat(bodyStr, hasJsonPath("$.data.facets.customizedSpec.path", notNullValue()));
          assertThat(bodyStr, hasJsonPath("$.data.facets.customizedSpec.content", notNullValue()));
          // the content MUST be based64 encoded
          assertThat(
              bodyStr,
              hasJsonPath("$.data.facets.baseSpec.content", matchesPattern(BASE64_PATTERN)));
          assertThat(
              bodyStr,
              hasJsonPath("$.data.facets.customizedSpec.content", matchesPattern(BASE64_PATTERN)));
        });
  }

  @Order(5)
  @Test
  void testRetrieveAssetLinkById() {
    String path = String.format("/assets/%s/links", "mef.sonata.api-spec.order");
    testClientHelper.getAndVerify(
        (uriBuilder -> uriBuilder.path(path).build()),
        bodyStr -> {
          assertThat(bodyStr, hasJsonPath("$.data", notNullValue()));
        });
  }

  @Order(6)
  @Test
  void testRetrieveAssetChildrenById() {
    String path = String.format("/assets/%s/children", PRODUCT_ID);
    testClientHelper.getAndVerify(
        (uriBuilder -> uriBuilder.path(path).build()),
        bodyStr -> {
          assertThat(bodyStr, hasJsonPath("$.data", notNullValue()));
        });
  }

  @Order(7)
  @Test
  void testDownloadBaseOpenAPIDocs() {
    String path = String.format("/assets/%s/api-docs", "mef.sonata.api-spec.order");
    testClientHelper.getAndVerify(
        (uriBuilder -> uriBuilder.path(path).queryParam("specType", APISpecEnum.BASE).build()),
        Assertions::assertNotNull);
  }

  @Order(8)
  @Test
  void testDownloadCustomizedOpenAPIDocs() {
    String path = String.format("/assets/%s/api-docs", "mef.sonata.api-spec.order");
    testClientHelper.getAndVerify(
        (uriBuilder ->
            uriBuilder.path(path).queryParam("specType", APISpecEnum.CUSTOMIZED).build()),
        Assertions::assertNotNull);
  }
}
