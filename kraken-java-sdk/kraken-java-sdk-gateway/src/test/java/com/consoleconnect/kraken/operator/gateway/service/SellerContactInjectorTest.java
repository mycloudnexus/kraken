package com.consoleconnect.kraken.operator.gateway.service;

import static com.consoleconnect.kraken.operator.core.enums.AssetKindEnum.COMPONENT_SELLER_CONTACT;
import static com.consoleconnect.kraken.operator.core.toolkit.Constants.ORDER_KEY_WORD;
import static com.consoleconnect.kraken.operator.core.toolkit.Constants.QUOTE_KEY_WORD;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import com.consoleconnect.kraken.operator.core.enums.AssetStatusEnum;
import com.consoleconnect.kraken.operator.core.enums.ParentProductTypeEnum;
import com.consoleconnect.kraken.operator.core.event.IngestionDataResult;
import com.consoleconnect.kraken.operator.core.model.SyncMetadata;
import com.consoleconnect.kraken.operator.core.model.UnifiedAsset;
import com.consoleconnect.kraken.operator.core.model.facet.SellerContactFacets;
import com.consoleconnect.kraken.operator.core.service.UnifiedAssetService;
import com.consoleconnect.kraken.operator.core.toolkit.DateTime;
import com.consoleconnect.kraken.operator.core.toolkit.JsonToolkit;
import com.consoleconnect.kraken.operator.gateway.CustomConfig;
import com.consoleconnect.kraken.operator.gateway.runner.SellerContactInjector;
import com.consoleconnect.kraken.operator.test.AbstractIntegrationTest;
import com.consoleconnect.kraken.operator.test.MockIntegrationTest;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@Slf4j
@MockIntegrationTest
@ContextConfiguration(classes = CustomConfig.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SellerContactInjectorTest extends AbstractIntegrationTest implements SellerContactInjector {
  @Autowired UnifiedAssetService unifiedAssetService;

  @Override
  public UnifiedAssetService getUnifiedAssetService() {
    return this.unifiedAssetService;
  }

  @Test
  @Order(1)
  void givenNotExistedTargetKey_whenInjection_thenReturnDirectly() {
    Map<String, Object> inputs = buildInputs();
    inject(inputs, "mef.sonata.api-target.order.eline.add1");
    String bodyStr = JsonToolkit.toJson(inputs);
    log.info(bodyStr);
    assertThat(bodyStr, hasJsonPath("$.env.seller", notNullValue()));
    assertThat(bodyStr, hasJsonPath("$.env.seller.number", notNullValue()));
    assertThat(bodyStr, hasJsonPath("$.env.seller.role", notNullValue()));
    assertThat(bodyStr, hasJsonPath("$.env.seller.emailAddress", notNullValue()));
    assertThat(bodyStr, hasJsonPath("$.env.seller.name", equalTo("kraken")));
  }

  @Test
  @Order(2)
  void givenNotExistSellerContactKey_whenInjection_thenReturnOK() {
    Map<String, Object> inputs = buildInputs();
    inject(inputs, "mef.sonata.api-target.order.eline.add");
    String bodyStr = JsonToolkit.toJson(inputs);
    log.info(bodyStr);
    assertThat(bodyStr, hasJsonPath("$.env.seller", notNullValue()));
    assertThat(bodyStr, hasJsonPath("$.env.seller.name", notNullValue()));
    assertThat(bodyStr, hasJsonPath("$.env.seller.number", notNullValue()));
    assertThat(bodyStr, hasJsonPath("$.env.seller.role", notNullValue()));
    assertThat(bodyStr, hasJsonPath("$.env.seller.emailAddress", notNullValue()));
  }

  @Test
  @Order(3)
  void givenExistedSellerContact_whenInjection_thenReturnOK() {
    String componentKey = "mef.sonata.api.order";
    String productId = "product.mef.sonata.api";
    String sellerContactKey =
        generateSellerContactKey(componentKey, ParentProductTypeEnum.ACCESS_ELINE.getKind());
    UnifiedAsset sellerContactAsset =
        createSellerContact(
            componentKey, sellerContactKey, ParentProductTypeEnum.ACCESS_ELINE.getKind());
    SyncMetadata syncMetadata = new SyncMetadata("", "", DateTime.nowInUTCString(), "test-user");
    IngestionDataResult ingestionDataResult =
        unifiedAssetService.syncAsset(productId, sellerContactAsset, syncMetadata, true);
    Assertions.assertNotNull(ingestionDataResult);
    Map<String, Object> inputs = buildInputs();

    inject(inputs, "mef.sonata.api-target.order.eline.add");
    String bodyStr = JsonToolkit.toJson(inputs);
    log.info(bodyStr);
    assertThat(bodyStr, hasJsonPath("$.env.seller", notNullValue()));
    assertThat(bodyStr, hasJsonPath("$.env.seller.number", notNullValue()));
    assertThat(bodyStr, hasJsonPath("$.env.seller.role", notNullValue()));
    assertThat(bodyStr, hasJsonPath("$.env.seller.emailAddress", notNullValue()));
    assertThat(bodyStr, hasJsonPath("$.env.seller.name", equalTo("test-new-seller-contact")));
  }

  private UnifiedAsset createSellerContact(
      String componentKey, String sellerContactKey, String productCategory) {
    UnifiedAsset unifiedAsset =
        UnifiedAsset.of(
            COMPONENT_SELLER_CONTACT.getKind(), sellerContactKey, "mef.sonata.seller.contact");
    unifiedAsset.getMetadata().setStatus(AssetStatusEnum.ACTIVATED.getKind());
    unifiedAsset.getMetadata().getLabels().put("componentKey", componentKey);
    unifiedAsset.getMetadata().getLabels().put(productCategory, String.valueOf(Boolean.TRUE));

    SellerContactFacets facets = getSellerContactFacets(componentKey);
    unifiedAsset.setFacets(
        JsonToolkit.fromJson(
            JsonToolkit.toJson(facets), new TypeReference<Map<String, Object>>() {}));
    return unifiedAsset;
  }

  private static @NotNull SellerContactFacets getSellerContactFacets(String componentKey) {
    SellerContactFacets facets = new SellerContactFacets();
    SellerContactFacets.SellerInfo sellerInfo = new SellerContactFacets.SellerInfo();
    sellerInfo.setName("test-new-seller-contact");
    sellerInfo.setNumber("789");
    if (componentKey.contains(ORDER_KEY_WORD)) {
      sellerInfo.setRole("sellerContact");
    } else if (componentKey.contains(QUOTE_KEY_WORD)) {
      sellerInfo.setRole("sellerContactInformation");
    } else {
      sellerInfo.setRole("");
    }
    sellerInfo.setEmailAddress("test-new-seller-contact@gmail.com");
    facets.setSellerInfo(sellerInfo);
    return facets;
  }

  public Map<String, Object> buildInputs() {
    Map<String, Object> seller = new HashMap<>();
    seller.put("name", "kraken");
    seller.put("number", "N/A");
    seller.put("role", "sellerContact");
    seller.put("emailAddress", "support@kraken.com");
    Map<String, Object> env = new HashMap<>();
    env.put("seller", seller);
    Map<String, Object> inputs = new HashMap<>();
    inputs.put("env", env);
    return inputs;
  }
}
