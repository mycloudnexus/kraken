package com.consoleconnect.kraken.operator.controller;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;

import com.consoleconnect.kraken.operator.config.TestApplication;
import com.consoleconnect.kraken.operator.controller.dto.CreateSellerContactRequest;
import com.consoleconnect.kraken.operator.core.enums.ProductCategoryEnum;
import com.consoleconnect.kraken.operator.core.toolkit.JsonToolkit;
import com.consoleconnect.kraken.operator.test.AbstractIntegrationTest;
import com.consoleconnect.kraken.operator.test.MockIntegrationTest;
import java.util.Arrays;
import java.util.List;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;

@Slf4j
@MockIntegrationTest
@ContextConfiguration(classes = {TestApplication.class})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SellerContactControllerTest extends AbstractIntegrationTest {
  private static final String PRODUCT_ID = "product.mef.sonata.api";
  private static final String COMPONENT_KEY_OF_ORDER = "mef.sonata.api.order";
  private static final String COMPONENT_KEY_OF_QUOTE = "mef.sonata.api.quote";
  @Autowired WebTestClient webTestClient;
  @Getter private final WebTestClientHelper testClientHelper;

  @Autowired
  public SellerContactControllerTest(WebTestClient webTestClient) {
    testClientHelper = new WebTestClientHelper(webTestClient);
  }

  public static List<String> buildComponentIds() {
    return List.of(COMPONENT_KEY_OF_ORDER, COMPONENT_KEY_OF_QUOTE);
  }

  @ParameterizedTest
  @MethodSource(value = "buildComponentIds")
  @Order(1)
  void givenLegalSellerContacts_whenCreating_thenReturnOK(String componentId) {
    CreateSellerContactRequest request = new CreateSellerContactRequest();
    request.setProductTypes(
        Arrays.stream(ProductCategoryEnum.values()).map(ProductCategoryEnum::getKind).toList());
    request.setContactName("test-contact");
    request.setContactEmail("test-contact@gmail.com");
    request.setContactPhone("1234567");
    String path =
        String.format("/products/%s/components/%s/seller-contacts", PRODUCT_ID, componentId);
    getTestClientHelper()
        .postAndVerify(
            (uriBuilder -> uriBuilder.path(path).build()),
            JsonToolkit.toJson(request),
            bodyStr -> {
              log.info(bodyStr);
              assertThat(bodyStr, hasJsonPath("$.data.data.key", notNullValue()));
              assertThat(bodyStr, hasJsonPath("$.data.data.labels.componentKey", notNullValue()));
            });
  }
}
