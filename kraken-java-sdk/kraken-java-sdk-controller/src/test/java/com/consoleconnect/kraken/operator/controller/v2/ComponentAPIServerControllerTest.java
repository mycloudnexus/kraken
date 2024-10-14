package com.consoleconnect.kraken.operator.controller.v2;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;

import com.consoleconnect.kraken.operator.config.TestApplication;
import com.consoleconnect.kraken.operator.controller.WebTestClientHelper;
import com.consoleconnect.kraken.operator.controller.dto.CreateAPIServerRequest;
import com.consoleconnect.kraken.operator.core.enums.APISpecContentFormatEnum;
import com.consoleconnect.kraken.operator.core.model.facet.ComponentAPISpecFacets;
import com.consoleconnect.kraken.operator.core.toolkit.JsonToolkit;
import com.consoleconnect.kraken.operator.test.AbstractIntegrationTest;
import com.consoleconnect.kraken.operator.test.MockIntegrationTest;
import java.util.List;
import java.util.UUID;
import org.apache.commons.codec.binary.Base64;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;

@MockIntegrationTest
@ContextConfiguration(classes = TestApplication.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ComponentAPIServerControllerTest extends AbstractIntegrationTest {

  @Autowired WebTestClient webTestClient;

  private static final String PRODUCT_BASE_PATH = "/v2/products";
  private static final String PRODUCT_ID = "product.mef.sonata.api";
  private static final String COMPONENT_ID = "mef.sonata.api.order";

  private final WebTestClientHelper testClientHelper;

  @Autowired
  ComponentAPIServerControllerTest(WebTestClient webTestClient) {
    testClientHelper = new WebTestClientHelper(webTestClient);
  }

  @Order(1)
  @Test
  void testCreateAPIServer() {
    String path =
        String.format(
            "%s/%s/components/%s/api-servers", PRODUCT_BASE_PATH, PRODUCT_ID, COMPONENT_ID);

    CreateAPIServerRequest request = new CreateAPIServerRequest();

    request.setName("this is a readable name");
    request.setDescription("this is a short description");
    request.setKey(UUID.randomUUID().toString());

    ComponentAPISpecFacets.APISpec apiSpec = new ComponentAPISpecFacets.APISpec();
    apiSpec.setContent(Base64.encodeBase64String("this is a open api spec".getBytes()));
    apiSpec.setPath("https://test.com");
    apiSpec.setFormat(APISpecContentFormatEnum.OPEN_API);

    request.setBaseSpec(apiSpec);

    request.setSelectedAPIs(List.of("/api/v1/xxx get"));

    testClientHelper.postAndVerify(
        (uriBuilder -> uriBuilder.path(path).build()),
        JsonToolkit.toJson(request),
        bodyStr -> {
          assertThat(bodyStr, hasJsonPath("$.data", notNullValue()));
        });
  }

  @Order(2)
  @Test
  void testSearchAPIServers() {
    String path =
        String.format(
            "%s/%s/components/%s/api-servers", PRODUCT_BASE_PATH, PRODUCT_ID, COMPONENT_ID);
    testClientHelper.getAndVerify(
        (uriBuilder -> uriBuilder.path(path).build()),
        bodyStr -> {
          assertThat(bodyStr, hasJsonPath("$.data.data", hasSize(1)));
        });
  }
}
