package com.consoleconnect.kraken.operator.controller;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import com.consoleconnect.kraken.operator.config.TestApplication;
import com.consoleconnect.kraken.operator.controller.dto.BuyerAssetDto;
import com.consoleconnect.kraken.operator.controller.dto.CreateBuyerRequest;
import com.consoleconnect.kraken.operator.controller.entity.TokenStorageEntity;
import com.consoleconnect.kraken.operator.controller.model.Environment;
import com.consoleconnect.kraken.operator.controller.repo.TokenStorageRepository;
import com.consoleconnect.kraken.operator.controller.service.EnvironmentService;
import com.consoleconnect.kraken.operator.controller.service.TokenStorageService;
import com.consoleconnect.kraken.operator.core.dto.Tuple2;
import com.consoleconnect.kraken.operator.core.dto.UnifiedAssetDto;
import com.consoleconnect.kraken.operator.core.enums.AssetKindEnum;
import com.consoleconnect.kraken.operator.core.enums.AssetStatusEnum;
import com.consoleconnect.kraken.operator.core.service.UnifiedAssetService;
import com.consoleconnect.kraken.operator.core.toolkit.AssetsConstants;
import com.consoleconnect.kraken.operator.core.toolkit.LabelConstants;
import com.consoleconnect.kraken.operator.test.AbstractIntegrationTest;
import com.consoleconnect.kraken.operator.test.MockIntegrationTest;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;

@Slf4j
@MockIntegrationTest
@ContextConfiguration(classes = {TestApplication.class})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ActiveProfiles({"test-rs256", "enable-postgres"})
class BuyerControllerTest extends AbstractIntegrationTest implements EnvCreator, BuyerCreator {

  @Getter private final WebTestClientHelper webTestClient;
  @Autowired private UnifiedAssetService unifiedAssetService;
  @Getter @Autowired EnvironmentService environmentService;
  @Autowired private TokenStorageRepository tokenStorageRepository;
  @Autowired private TokenStorageService tokenStorageService;

  @Autowired
  public BuyerControllerTest(WebTestClient webTestClient) {
    this.webTestClient = new WebTestClientHelper(webTestClient);
  }

  @Test
  @Order(2)
  void givenBuyer_whenCreate_thenOK() {
    Environment envStage = createStage(PRODUCT_ID);
    BuyerAssetDto buyerCreated = createBuyer(BUYER_ID, envStage.getId(), COMPANY_NAME);
    String refreshAccessTokenUrl = BUYER_BASE_URL + "/" + buyerCreated.getId() + "/access-tokens";
    webTestClient.requestAndVerify(
        HttpMethod.POST,
        uriBuilder ->
            uriBuilder
                .path(refreshAccessTokenUrl)
                .queryParam("tokenExpiredInSeconds", "86400")
                .build(),
        HttpStatus.OK.value(),
        null,
        bodyStr -> {
          assertThat(bodyStr, hasJsonPath("$.data", notNullValue()));
          assertThat(bodyStr, hasJsonPath("$.data.buyerToken", notNullValue()));
          assertThat(bodyStr, hasJsonPath("$.data.buyerToken.accessToken", notNullValue()));

          TokenStorageEntity entity =
              tokenStorageRepository.findFirstByAssetId(buyerCreated.getId());
          Assertions.assertNotNull(entity.getToken());
          BuyerAssetDto.BuyerToken buyerToken =
              tokenStorageService.readSecret(buyerCreated.getId());
          Assertions.assertNotNull(buyerToken.getAccessToken());
        });
  }

  @Test
  @Order(2)
  void givenNoneExistBuyer_whenRead_thenReturnError() {
    String readTokenUrl = BUYER_BASE_URL + "/" + BUYER_ID + "/token";
    webTestClient.requestAndVerify(
        HttpMethod.GET,
        uriBuilder -> uriBuilder.path(readTokenUrl).build(),
        HttpStatus.NOT_FOUND.value(),
        null,
        bodyStr -> {
          assertThat(bodyStr, hasJsonPath("$.code", is("notFound")));
        });
  }

  @Test
  @Order(3)
  void givenBuyer_whenSearch_thenOK() {
    webTestClient.requestAndVerify(
        HttpMethod.GET,
        uriBuilder ->
            uriBuilder
                .path(BUYER_BASE_URL)
                .queryParam("status", AssetStatusEnum.ACTIVATED.getKind())
                .queryParam("buyerId", "testing-company")
                .build(),
        HttpStatus.OK.value(),
        null,
        bodyStr -> {
          assertThat(bodyStr, Matchers.notNullValue());
          assertThat(bodyStr, hasJsonPath("$.data.data", hasSize(1)));
        });
  }

  @Test
  @Order(3)
  void givenBuyer_whenSearchInactive_thenReturnEmptyData() {
    webTestClient.requestAndVerify(
        HttpMethod.GET,
        uriBuilder ->
            uriBuilder
                .path(BUYER_BASE_URL)
                .queryParam("status", AssetStatusEnum.DEACTIVATED.getKind())
                .build(),
        HttpStatus.OK.value(),
        null,
        bodyStr -> {
          log.info(bodyStr);
          assertThat(bodyStr, Matchers.notNullValue());
          assertThat(bodyStr, hasJsonPath("$.data.data", hasSize(0)));
        });
  }

  @Test
  @Order(4)
  void givenDuplicatedBuyer_whenCreate_thenNot200() {
    CreateBuyerRequest requestEntity = new CreateBuyerRequest();
    requestEntity.setBuyerId(BUYER_ID);
    Environment envStage = createStage(PRODUCT_ID);
    requestEntity.setEnvId(envStage.getId());
    requestEntity.setCompanyName("console connect");

    webTestClient.requestAndVerify(
        HttpMethod.POST,
        uriBuilder -> uriBuilder.path(BUYER_BASE_URL).build(),
        HttpStatus.BAD_REQUEST.value(),
        requestEntity,
        bodyStr -> {
          log.info(bodyStr);
          assertThat(bodyStr, hasJsonPath("$.code", not(200)));
        });
  }

  @Test
  @Order(5)
  void givenBlankBuyer_whenCreate_thenNot200() {
    CreateBuyerRequest requestEntity = new CreateBuyerRequest();
    requestEntity.setBuyerId("");
    requestEntity.setEnvId("stage");
    requestEntity.setCompanyName("console connect");
    webTestClient.requestAndVerify(
        HttpMethod.POST,
        uriBuilder -> uriBuilder.path(BUYER_BASE_URL).build(),
        HttpStatus.BAD_REQUEST.value(),
        requestEntity,
        bodyStr -> {
          assertThat(bodyStr, hasJsonPath("$.code", not(200)));
        });
  }

  @Test
  @Order(6)
  void givenBlankEnv_whenCreateBuyer_thenNot200() {
    CreateBuyerRequest requestEntity = new CreateBuyerRequest();
    requestEntity.setBuyerId(BUYER_ID);
    requestEntity.setEnvId("");
    requestEntity.setCompanyName("console connect");
    webTestClient.requestAndVerify(
        HttpMethod.POST,
        uriBuilder -> uriBuilder.path(BUYER_BASE_URL).build(),
        HttpStatus.BAD_REQUEST.value(),
        requestEntity,
        bodyStr -> {
          assertThat(bodyStr, hasJsonPath("$.code", not(200)));
        });
  }

  @Test
  @Order(7)
  void givenCreateBuyer_whenDeactivate_thenOK() {
    UnifiedAssetDto buyerAsset =
        unifiedAssetService
            .findBySpecification(
                Tuple2.ofList(AssetsConstants.FIELD_KIND, AssetKindEnum.PRODUCT_BUYER.getKind()),
                Tuple2.ofList(LabelConstants.LABEL_BUYER_ID, BUYER_ID),
                null,
                null,
                null)
            .getData()
            .get(0);
    webTestClient.requestAndVerify(
        HttpMethod.POST,
        uriBuilder ->
            uriBuilder
                .path("/products/{productId}/buyers/{id}/deactivate")
                .build(PRODUCT_ID, buyerAsset.getId()),
        HttpStatus.OK.value(),
        null,
        bodyStr -> {
          assertThat(bodyStr, hasJsonPath("$.code", is(200)));
        });
  }

  @Test
  @Order(8)
  void givenBuyerDeactivate_whenActivate_thenOK() {
    UnifiedAssetDto buyerAsset =
        unifiedAssetService
            .findBySpecification(
                Tuple2.ofList(AssetsConstants.FIELD_KIND, AssetKindEnum.PRODUCT_BUYER.getKind()),
                Tuple2.ofList(LabelConstants.LABEL_BUYER_ID, BUYER_ID),
                null,
                null,
                null)
            .getData()
            .get(0);
    webTestClient.requestAndVerify(
        HttpMethod.POST,
        uriBuilder ->
            uriBuilder
                .path("/products/{productId}/buyers/{id}/activate")
                .build(PRODUCT_ID, buyerAsset.getId()),
        HttpStatus.OK.value(),
        null,
        bodyStr -> {
          assertThat(bodyStr, hasJsonPath("$.code", is(200)));
        });
  }
}
