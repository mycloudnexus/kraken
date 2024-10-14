package com.consoleconnect.kraken.operator.core.controller;

import com.consoleconnect.kraken.operator.core.CustomConfig;
import com.consoleconnect.kraken.operator.core.enums.APISpecEnum;
import com.consoleconnect.kraken.operator.core.helper.WebTestClientHelper;
import com.consoleconnect.kraken.operator.test.AbstractIntegrationTest;
import com.consoleconnect.kraken.operator.test.MockIntegrationTest;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;

@MockIntegrationTest
@ContextConfiguration(classes = CustomConfig.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ComponentAPISpecControllerTest extends AbstractIntegrationTest {

  @Autowired WebTestClient webTestClient;

  private static final String PRODUCT_BASE_PATH = "/products";
  private static final String PRODUCT_ID = "product.mef.sonata.api";

  private final WebTestClientHelper testClientHelper;

  @Autowired
  ComponentAPISpecControllerTest(WebTestClient webTestClient) {
    testClientHelper = new WebTestClientHelper(webTestClient);
  }

  @Order(1)
  @Test
  void testDownloadBaseOpenAPIDocs() {
    String path =
        String.format(
            "%s/%s/components/%s/api-docs",
            PRODUCT_BASE_PATH, PRODUCT_ID, "mef.sonata.api-spec.order");
    testClientHelper.getAndVerify(
        (uriBuilder -> uriBuilder.path(path).queryParam("specType", APISpecEnum.BASE).build()),
        Assertions::assertNotNull);
  }

  @Order(2)
  @Test
  void testDownloadCustomizedOpenAPIDocs() {
    String path =
        String.format(
            "%s/%s/components/%s/api-docs",
            PRODUCT_BASE_PATH, PRODUCT_ID, "mef.sonata.api-spec.order");
    testClientHelper.getAndVerify(
        (uriBuilder ->
            uriBuilder.path(path).queryParam("specType", APISpecEnum.CUSTOMIZED).build()),
        Assertions::assertNotNull);
  }
}
