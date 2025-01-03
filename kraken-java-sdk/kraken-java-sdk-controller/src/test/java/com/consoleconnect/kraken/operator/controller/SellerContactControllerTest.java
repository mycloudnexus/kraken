package com.consoleconnect.kraken.operator.controller;

import static com.consoleconnect.kraken.operator.core.enums.ProductCategoryEnum.ACCESS_ELINE;
import static com.consoleconnect.kraken.operator.core.enums.ProductCategoryEnum.INTERNET_ACCESS;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import com.consoleconnect.kraken.operator.config.TestApplication;
import com.consoleconnect.kraken.operator.controller.dto.CreateSellerContactRequest;
import com.consoleconnect.kraken.operator.core.enums.AssetKindEnum;
import com.consoleconnect.kraken.operator.core.toolkit.JsonToolkit;
import com.consoleconnect.kraken.operator.test.AbstractIntegrationTest;
import com.consoleconnect.kraken.operator.test.MockIntegrationTest;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
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

  @ParameterizedTest
  @MethodSource(value = "buildIllegalCreateReq")
  @Order(1)
  void givenIllegalRequest_whenCreating_thenThrowsException(SellerContactCreateReq req) {
    CreateSellerContactRequest request = new CreateSellerContactRequest();
    if (req.getRequest() != null) {
      request.setProductCategories(req.getRequest().getProductCategories());
      request.setContactName(req.getRequest().getContactName());
      request.setContactEmail(req.getRequest().getContactEmail());
      request.setContactPhone(req.getRequest().getContactPhone());
    }
    String path =
        String.format(
            "/products/%s/components/%s/seller-contacts", req.getProductId(), req.getComponentId());
    getTestClientHelper()
        .postAndVerify(
            (uriBuilder -> uriBuilder.path(path).build()),
            HttpStatus.BAD_REQUEST,
            JsonToolkit.toJson(request),
            bodyStr -> {
              log.info(bodyStr);
              assertThat(bodyStr, hasJsonPath("$.code", not(200)));
            });
  }

  @SneakyThrows
  @ParameterizedTest
  @MethodSource(value = "buildComponentIds")
  @Order(2)
  void givenLegalSellerContacts_whenCreating_thenReturnOK(String componentId) {
    CreateSellerContactRequest request = createSellerContact();
    String key = componentId + "." + ACCESS_ELINE.getKind() + "." + INTERNET_ACCESS.getKind();
    String path =
        String.format("/products/%s/components/%s/seller-contacts", PRODUCT_ID, componentId);
    getTestClientHelper()
        .postAndVerify(
            (uriBuilder -> uriBuilder.path(path).build()),
            JsonToolkit.toJson(request),
            bodyStr -> {
              log.info(bodyStr);
              assertThat(bodyStr, hasJsonPath("$.code", is(200)));
              assertThat(bodyStr, hasJsonPath("$.data.data.key", notNullValue()));
              assertThat(bodyStr, hasJsonPath("$.data.data.key", is(key)));
              assertThat(bodyStr, hasJsonPath("$.data.data.labels.componentKey", notNullValue()));
            });
  }

  @Test
  @Order(3)
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
  @MethodSource(value = "buildComponentIds")
  @Order(4)
  void givenKeyOfSellerContact_whenFindOne_thenReturnOK(String componentId) {
    String key = componentId + "." + ACCESS_ELINE.getKind() + "." + INTERNET_ACCESS.getKind();
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
  @MethodSource(value = "buildComponentIds")
  @Order(5)
  void givenDuplicateSellerContacts_whenCreating_thenReturn400(String componentId) {
    CreateSellerContactRequest request = createSellerContact();
    String path =
        String.format("/products/%s/components/%s/seller-contacts", PRODUCT_ID, componentId);
    getTestClientHelper()
        .postAndVerify(
            (uriBuilder -> uriBuilder.path(path).build()),
            HttpStatus.BAD_REQUEST,
            JsonToolkit.toJson(request),
            bodyStr -> {
              log.info(bodyStr);
              assertThat(bodyStr, hasJsonPath("$.code", not(200)));
            });
  }

  @SneakyThrows
  @ParameterizedTest
  @MethodSource(value = "buildComponentIds")
  @Order(6)
  void givenAnExistedSellerContact_whenDeleting_thenReturnOK(String componentId) {
    String id = componentId + "." + ACCESS_ELINE.getKind() + "." + INTERNET_ACCESS.getKind();
    String path =
        String.format("/products/%s/components/%s/seller-contacts/%s", PRODUCT_ID, componentId, id);
    getTestClientHelper()
        .requestAndVerify(
            HttpMethod.DELETE,
            (uriBuilder -> uriBuilder.path(path).build()),
            HttpStatus.OK.value(),
            null,
            bodyStr -> {
              assertThat(bodyStr, hasJsonPath("$.code", is(200)));
            });
  }

  public static List<String> buildComponentIds() {
    return List.of(COMPONENT_KEY_OF_ORDER, COMPONENT_KEY_OF_QUOTE);
  }

  @SneakyThrows
  public static CreateSellerContactRequest createSellerContact() {
    return JsonToolkit.fromJson(readFileToString(TEST_CONTACT_PATH), new TypeReference<>() {});
  }

  public static List<SellerContactCreateReq> buildIllegalCreateReq() {
    List<SellerContactCreateReq> list = new ArrayList<>();
    // Case-2: Blank contactName
    CreateSellerContactRequest request2 = createSellerContact();
    SellerContactCreateReq req2 = new SellerContactCreateReq();
    req2.setProductId(PRODUCT_ID);
    req2.setComponentId(COMPONENT_KEY_OF_ORDER);
    request2.setContactName("");
    req2.setRequest(request2);
    list.add(req2);

    // Case-3: Blank contactEmail
    CreateSellerContactRequest request3 = createSellerContact();
    SellerContactCreateReq req3 = new SellerContactCreateReq();
    req3.setProductId(PRODUCT_ID);
    req3.setComponentId(COMPONENT_KEY_OF_ORDER);
    request3.setContactEmail("");
    req3.setRequest(request3);
    list.add(req3);

    // Case-4: Blank contactPhone
    CreateSellerContactRequest request4 = createSellerContact();
    SellerContactCreateReq req4 = new SellerContactCreateReq();
    req4.setProductId(PRODUCT_ID);
    req4.setComponentId(COMPONENT_KEY_OF_ORDER);
    request4.setContactPhone("");
    req4.setRequest(request4);
    list.add(req4);

    // Case-5: Empty productTypes
    CreateSellerContactRequest request5 = createSellerContact();
    SellerContactCreateReq req5 = new SellerContactCreateReq();
    req5.setProductId(PRODUCT_ID);
    req5.setComponentId(COMPONENT_KEY_OF_ORDER);
    request5.setProductCategories(List.of());
    req5.setRequest(request5);
    list.add(req5);

    // Case-6: Wrong value of productTypes
    CreateSellerContactRequest request6 = createSellerContact();
    SellerContactCreateReq req6 = new SellerContactCreateReq();
    req6.setProductId(PRODUCT_ID);
    req6.setComponentId(COMPONENT_KEY_OF_ORDER);
    request6.setProductCategories(List.of("access.eline-11111", "internet.access"));
    req6.setRequest(request6);
    list.add(req6);

    return list;
  }

  @Data
  public static class SellerContactCreateReq {
    private String productId;
    private String componentId;
    private CreateSellerContactRequest request;
    private String createdBy;
  }
}
