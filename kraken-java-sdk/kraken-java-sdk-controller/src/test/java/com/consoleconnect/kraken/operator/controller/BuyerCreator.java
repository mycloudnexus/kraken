package com.consoleconnect.kraken.operator.controller;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;

import com.consoleconnect.kraken.operator.controller.dto.BuyerAssetDto;
import com.consoleconnect.kraken.operator.controller.dto.CreateBuyerRequest;
import com.consoleconnect.kraken.operator.core.model.HttpResponse;
import com.consoleconnect.kraken.operator.core.toolkit.JsonToolkit;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

public interface BuyerCreator {
  String PRODUCT_ID = "product.mef.sonata.api";
  String BUYER_BASE_URL = String.format("/products/%s/buyers", PRODUCT_ID);
  String BUYER_ID = "consolecore-poping-company";
  String COMPANY_NAME = "console connect";

  WebTestClientHelper getWebTestClient();

  default BuyerAssetDto createBuyer(String buyerId, String envId, String companyName) {
    CreateBuyerRequest requestEntity = new CreateBuyerRequest();
    requestEntity.setBuyerId(buyerId);
    requestEntity.setEnvId(envId);
    requestEntity.setCompanyName(companyName);

    String resp =
        getWebTestClient()
            .requestAndVerify(
                HttpMethod.POST,
                uriBuilder -> uriBuilder.path(BUYER_BASE_URL).build(),
                HttpStatus.OK.value(),
                requestEntity,
                bodyStr -> {
                  assertThat(bodyStr, hasJsonPath("$.data", notNullValue()));
                  assertThat(bodyStr, hasJsonPath("$.data.buyerToken", notNullValue()));
                  assertThat(bodyStr, hasJsonPath("$.data.buyerToken.accessToken", notNullValue()));
                });
    HttpResponse<BuyerAssetDto> buyerCreatedResp =
        JsonToolkit.fromJson(resp, new TypeReference<HttpResponse<BuyerAssetDto>>() {});
    return buyerCreatedResp.getData();
  }
}
