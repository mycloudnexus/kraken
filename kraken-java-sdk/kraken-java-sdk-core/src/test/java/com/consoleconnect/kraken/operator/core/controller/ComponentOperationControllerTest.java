package com.consoleconnect.kraken.operator.core.controller;

import com.consoleconnect.kraken.operator.core.CustomConfig;
import com.consoleconnect.kraken.operator.core.enums.AssetKindEnum;
import com.consoleconnect.kraken.operator.core.exception.KrakenException;
import com.consoleconnect.kraken.operator.core.helper.WebTestClientHelper;
import com.consoleconnect.kraken.operator.core.ingestion.fs.ClassPathResourceLoader;
import com.consoleconnect.kraken.operator.core.model.FileContentDescriptor;
import com.consoleconnect.kraken.operator.core.model.UnifiedAsset;
import com.consoleconnect.kraken.operator.core.toolkit.JsonToolkit;
import com.consoleconnect.kraken.operator.core.toolkit.YamlToolkit;
import com.consoleconnect.kraken.operator.test.AbstractIntegrationTest;
import com.consoleconnect.kraken.operator.test.MockIntegrationTest;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;

@MockIntegrationTest
@ContextConfiguration(classes = CustomConfig.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ComponentOperationControllerTest extends AbstractIntegrationTest {

  @Autowired WebTestClient webTestClient;

  private static final String PRODUCT_BASE_PATH = "/products";
  private static final String PRODUCT_ID = "product.mef.sonata.api";
  private final WebTestClientHelper testClientHelper;

  @Autowired
  ComponentOperationControllerTest(WebTestClient webTestClient) {
    testClientHelper = new WebTestClientHelper(webTestClient);
  }

  @Order(1)
  @Test
  void testAddComponent_forbidden() {
    String path = String.format("%s/%s/components", PRODUCT_BASE_PATH, PRODUCT_ID);

    // add a not allowed component should return 400
    testClientHelper.requestAndVerify(
        HttpMethod.POST,
        (uriBuilder -> uriBuilder.path(path).build()),
        403,
        "{\"kind\":\"" + AssetKindEnum.PRODUCT.getKind() + "\"}",
        null);
    Assertions.assertTrue(true);
  }

  // add a new component
  @Order(2)
  @Test
  void testAddAndUpdateComponent() {
    String path = String.format("%s/%s/components", PRODUCT_BASE_PATH, PRODUCT_ID);

    // add a new component
    String data =
        new ClassPathResourceLoader()
            .readFile("classpath:deployment-config/components/api-targets/test.add.component.yaml")
            .map(FileContentDescriptor::getContent)
            .orElseThrow(
                () ->
                    KrakenException.notFound(
                        "deployment-config/components/api-targets/test.add.component.yaml"));
    UnifiedAsset component =
        YamlToolkit.parseYaml(data, UnifiedAsset.class)
            .orElseThrow(() -> KrakenException.badRequest("Failed to parse yaml file"));
    testClientHelper.postAndVerify(
        (uriBuilder -> uriBuilder.path(path).build()),
        JsonToolkit.toJson(component),
        bodyStr -> {
          Assertions.assertTrue(true);
        });

    // can't update the data with the same version
    component.getMetadata().setName("updated");
    testClientHelper.requestAndVerify(
        HttpMethod.PATCH,
        (uriBuilder -> uriBuilder.path(path + "/test.add.component").build()),
        500,
        JsonToolkit.toJson(component),
        bodyStr -> {
          Assertions.assertTrue(true);
        });

    // update the data with version increased
    component.getMetadata().setVersion(2);
    testClientHelper.requestAndVerify(
        HttpMethod.PATCH,
        (uriBuilder -> uriBuilder.path(path + "/test.add.component").build()),
        200,
        JsonToolkit.toJson(component),
        bodyStr -> {
          Assertions.assertTrue(true);
        });
  }
}
