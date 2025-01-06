package com.consoleconnect.kraken.operator.controller;

import static com.consoleconnect.kraken.operator.core.enums.ProductCategoryEnum.ACCESS_ELINE;
import static com.consoleconnect.kraken.operator.core.enums.ProductCategoryEnum.INTERNET_ACCESS;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import com.consoleconnect.kraken.operator.config.TestApplication;
import com.consoleconnect.kraken.operator.controller.dto.CreateSellerContactRequest;
import com.consoleconnect.kraken.operator.controller.dto.UpdateSellerContactRequest;
import com.consoleconnect.kraken.operator.core.enums.AssetKindEnum;
import com.consoleconnect.kraken.operator.core.toolkit.JsonToolkit;
import com.consoleconnect.kraken.operator.test.AbstractIntegrationTest;
import com.consoleconnect.kraken.operator.test.MockIntegrationTest;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.List;
import lombok.Data;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;

@Getter
@Slf4j
@MockIntegrationTest
@ContextConfiguration(classes = {TestApplication.class})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SellerContactControllerTest extends AbstractIntegrationTest {
  private static final String PRODUCT_ID = "product.mef.sonata.api";
  private static final String COMPONENT_KEY_OF_ORDER = "mef.sonata.api.order";
  private static final String COMPONENT_KEY_OF_QUOTE = "mef.sonata.api.quote";
  private static final String TEST_CONTACT_PATH = "data/seller-contact.json";

  private final WebTestClientHelper testClientHelper;

  @Autowired
  public SellerContactControllerTest(WebTestClient webTestClient) {
    testClientHelper = new WebTestClientHelper(webTestClient);
  }

  @SneakyThrows
  @ParameterizedTest
  @MethodSource(value = "buildComponentIds")
  @Order(1)
  void givenLegalSellerContacts_whenCreating_thenReturnOK(String componentId) {
    CreateSellerContactRequest request = createSellerContact();
    String path =
        String.format("/products/%s/components/%s/seller-contacts", PRODUCT_ID, componentId);
    getTestClientHelper()
        .postAndVerify(
            (uriBuilder -> uriBuilder.path(path).build()),
            JsonToolkit.toJson(request),
            bodyStr -> {
              log.info(bodyStr);
              assertThat(bodyStr, hasJsonPath("$.code", is(200)));
              assertThat(bodyStr, hasJsonPath("$.data", hasSize(2)));
              assertThat(bodyStr, hasJsonPath("$.data[0].data.key", notNullValue()));
              assertThat(bodyStr, hasJsonPath("$.data[1].data.key", notNullValue()));
              assertThat(
                  bodyStr, hasJsonPath("$.data[0].data.labels.componentKey", notNullValue()));
              assertThat(
                  bodyStr, hasJsonPath("$.data[1].data.labels.componentKey", notNullValue()));
            });
  }

  @Test
  @Order(2)
  void givenAssetKind_whenSearchSellerContacts_thenReturnOK() {
    String path = String.format("/products/%s/components", PRODUCT_ID);
    getTestClientHelper()
        .getAndVerify(
            (uriBuilder ->
                uriBuilder
                    .path(path)
                    .queryParam("kind", AssetKindEnum.COMPONENT_SELLER_CONTACT.getKind())
                    .build()),
            bodyStr -> {
              log.info(bodyStr);
              assertThat(bodyStr, hasJsonPath("$.code", is(200)));
            });
  }

  @ParameterizedTest
  @MethodSource(value = "buildSellerContactKeys")
  @Order(3)
  void givenKeyOfSellerContact_whenFindOne_thenReturnOK(Pair<String, String> pair) {
    String componentId = pair.getLeft();
    String productCategory = pair.getRight();
    String key = componentId + "." + productCategory;
    String path = String.format("/products/%s/components/%s", PRODUCT_ID, key);
    getTestClientHelper()
        .getAndVerify(
            (uriBuilder -> uriBuilder.path(path).build()),
            bodyStr -> {
              log.info(bodyStr);
              assertThat(bodyStr, hasJsonPath("$.code", is(200)));
            });
  }

  @SneakyThrows
  @ParameterizedTest
  @MethodSource(value = "buildSellerContactKeys")
  @Order(4)
  void givenAnExistedSellerContact_whenUpdating_thenReturnOK(Pair<String, String> pair) {
    String componentId = pair.getLeft();
    String productCategory = pair.getRight();
    String key = componentId + "." + productCategory;
    String path =
        String.format("/products/%s/components/%s/seller-contacts", PRODUCT_ID, componentId);
    UpdateSellerContactRequest request = new UpdateSellerContactRequest();
    request.setContactName("update-test");
    request.setContactPhone("456");
    request.setContactEmail("update-test@gmail.com");
    request.setKey(key);
    getTestClientHelper()
        .requestAndVerify(
            HttpMethod.PATCH,
            (uriBuilder -> uriBuilder.path(path).build()),
            HttpStatus.OK.value(),
            JsonToolkit.toJson(request),
            bodyStr -> {
              log.info(bodyStr);
              assertThat(bodyStr, hasJsonPath("$.code", is(200)));
            });
  }

  public static List<String> buildComponentIds() {
    return List.of(COMPONENT_KEY_OF_ORDER, COMPONENT_KEY_OF_QUOTE);
  }

  public static List<Pair<String, String>> buildSellerContactKeys() {
    return List.of(
        Pair.of(COMPONENT_KEY_OF_ORDER, ACCESS_ELINE.getKind()),
        Pair.of(COMPONENT_KEY_OF_ORDER, INTERNET_ACCESS.getKind()),
        Pair.of(COMPONENT_KEY_OF_QUOTE, ACCESS_ELINE.getKind()),
        Pair.of(COMPONENT_KEY_OF_QUOTE, INTERNET_ACCESS.getKind()));
  }

  @SneakyThrows
  public static CreateSellerContactRequest createSellerContact() {
    return JsonToolkit.fromJson(readFileToString(TEST_CONTACT_PATH), new TypeReference<>() {});
  }

  @Data
  public static class SellerContactCreateReq {
    private String productId;
    private String componentId;
    private UpdateSellerContactRequest request;
    private String createdBy;
  }
}
