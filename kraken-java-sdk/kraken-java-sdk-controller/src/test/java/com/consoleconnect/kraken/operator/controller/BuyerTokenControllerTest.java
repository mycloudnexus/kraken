package com.consoleconnect.kraken.operator.controller;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.consoleconnect.kraken.operator.config.TestApplication;
import com.consoleconnect.kraken.operator.controller.dto.BuyerAssetDto;
import com.consoleconnect.kraken.operator.controller.model.Environment;
import com.consoleconnect.kraken.operator.controller.repo.TokenStorageRepository;
import com.consoleconnect.kraken.operator.controller.service.EnvironmentService;
import com.consoleconnect.kraken.operator.core.service.UnifiedAssetService;
import com.consoleconnect.kraken.operator.test.AbstractIntegrationTest;
import com.consoleconnect.kraken.operator.test.MockIntegrationTest;
import java.util.Date;
import java.util.Map;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.vault.core.VaultTemplate;
import org.springframework.vault.support.VaultResponse;

@Slf4j
@MockIntegrationTest
@ContextConfiguration(classes = {TestApplication.class})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ActiveProfiles({"test-rs256", "enable-vault"})
class BuyerTokenControllerTest extends AbstractIntegrationTest implements EnvCreator, BuyerCreator {

  @Getter private final WebTestClientHelper webTestClient;
  @Autowired private UnifiedAssetService unifiedAssetService;
  @Getter @Autowired EnvironmentService environmentService;
  @Autowired private TokenStorageRepository tokenStorageRepository;
  @MockBean private VaultTemplate vaultTemplate;

  @Autowired
  public BuyerTokenControllerTest(WebTestClient webTestClient) {
    this.webTestClient = new WebTestClientHelper(webTestClient);
  }

  @Test
  @Order(1)
  void givenBuyer_whenCreate_thenOK() {
    VaultResponse response = new VaultResponse();
    BuyerAssetDto.BuyerToken buyerToken = new BuyerAssetDto.BuyerToken();
    buyerToken.setAccessToken("mock-token");
    buyerToken.setExpiredAt(new Date());
    response.setData(Map.of("data", buyerToken));
    when(vaultTemplate.write(anyString(), any())).thenReturn(response);
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
        });
  }

  @Test
  @Order(2)
  void givenBuyer_whenRead_thenOK() {
    VaultResponse response = new VaultResponse();
    BuyerAssetDto.BuyerToken buyerToken = new BuyerAssetDto.BuyerToken();
    buyerToken.setAccessToken("mock-token");
    buyerToken.setExpiredAt(new Date());
    response.setData(Map.of("data", buyerToken));
    when(vaultTemplate.read(anyString())).thenReturn(response);
    String readTokenUrl = BUYER_BASE_URL + "/" + "buyer-01" + "/token";
    webTestClient.requestAndVerify(
        HttpMethod.GET,
        uriBuilder -> uriBuilder.path(readTokenUrl).build(),
        HttpStatus.OK.value(),
        null,
        bodyStr -> {
          assertThat(bodyStr, hasJsonPath("$.data", notNullValue()));
          assertThat(bodyStr, hasJsonPath("$.data.accessToken", notNullValue()));
        });
  }
}
