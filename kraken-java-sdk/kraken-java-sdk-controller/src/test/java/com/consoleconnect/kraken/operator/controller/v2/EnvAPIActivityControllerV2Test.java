package com.consoleconnect.kraken.operator.controller.v2;

import static com.jayway.jsonassert.impl.matcher.IsCollectionWithSize.hasSize;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import com.consoleconnect.kraken.operator.config.TestApplication;
import com.consoleconnect.kraken.operator.controller.ApiActivityLogCreator;
import com.consoleconnect.kraken.operator.controller.BuyerCreator;
import com.consoleconnect.kraken.operator.controller.EnvCreator;
import com.consoleconnect.kraken.operator.controller.WebTestClientHelper;
import com.consoleconnect.kraken.operator.controller.dto.BuyerAssetDto;
import com.consoleconnect.kraken.operator.controller.model.Environment;
import com.consoleconnect.kraken.operator.controller.service.EnvironmentService;
import com.consoleconnect.kraken.operator.core.entity.ApiActivityLogEntity;
import com.consoleconnect.kraken.operator.core.model.UnifiedAsset;
import com.consoleconnect.kraken.operator.core.model.facet.BuyerOnboardFacets;
import com.consoleconnect.kraken.operator.core.repo.ApiActivityLogRepository;
import com.consoleconnect.kraken.operator.core.toolkit.JsonToolkit;
import com.consoleconnect.kraken.operator.test.AbstractIntegrationTest;
import com.consoleconnect.kraken.operator.test.MockIntegrationTest;
import java.time.ZonedDateTime;
import java.util.List;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;

@Slf4j
@Getter
@MockIntegrationTest
@ContextConfiguration(classes = {TestApplication.class})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ActiveProfiles("test-rs256")
class EnvAPIActivityControllerV2Test extends AbstractIntegrationTest
    implements ApiActivityLogCreator, EnvCreator, BuyerCreator {
  private static final String ACTIVITY_BASE_URL = "/v2/products/%s/envs/%s/api-activities";
  WebTestClientHelper webTestClient;
  @Autowired ApiActivityLogRepository apiActivityLogRepository;
  @Autowired EnvironmentService environmentService;

  private String requestId;

  @Autowired
  public EnvAPIActivityControllerV2Test(WebTestClient webTestClient) {
    this.webTestClient = new WebTestClientHelper(webTestClient);
  }

  @Test
  @Order(1)
  void givenExistedActivityId_whenSearchDetail_thenReturnOK() {
    Environment envStage = createStage(PRODUCT_ID);
    String activityBaseUrl = String.format(ACTIVITY_BASE_URL, PRODUCT_ID, envStage.getId());
    BuyerAssetDto buyerAssetDto =
        createBuyer(BUYER_ID + "-" + System.currentTimeMillis(), envStage.getId(), COMPANY_NAME);
    BuyerOnboardFacets buyerFacets =
        UnifiedAsset.getFacets(buyerAssetDto, BuyerOnboardFacets.class);
    ApiActivityLogEntity apiActivityLogEntity =
        createApiActivityLog(buyerFacets.getBuyerInfo().getBuyerId(), envStage.getId(), "UNI");
    log.info("activity log created:{}", JsonToolkit.toJson(apiActivityLogEntity));
    requestId = apiActivityLogEntity.getRequestId();
    webTestClient.requestAndVerify(
        HttpMethod.GET,
        uriBuilder ->
            uriBuilder
                .path(activityBaseUrl + "/{activityId}")
                .build(apiActivityLogEntity.getRequestId()),
        HttpStatus.OK.value(),
        null,
        bodyStr -> {
          log.info(bodyStr);
          assertThat(bodyStr, hasJsonPath("$.data", notNullValue()));
          assertThat(bodyStr, hasJsonPath("$.data.main.buyer", notNullValue()));
        });
  }

  @Test
  @Order(2)
  void givenTimeRange_whenSearchActivities_thenReturnOK() {
    Environment envStage = createStage(PRODUCT_ID);
    log.info("envId:{}", envStage.getId());
    for (int i = 0; i < 3; i++) {
      createApiActivityLog("buyer-" + i, envStage.getId(), "UNI", "/x-" + i, "localhost", "POST");
    }
    List<String> methods = List.of("GET", "POST");

    String activityBaseUrl = String.format(ACTIVITY_BASE_URL, PRODUCT_ID, envStage.getId());
    webTestClient.requestAndVerify(
        HttpMethod.GET,
        uriBuilder ->
            uriBuilder
                .path(activityBaseUrl)
                .queryParam("envId", envStage.getId())
                .queryParam("methods", methods.toArray())
                .queryParam(
                    "requestStartTime", ZonedDateTime.now().minusDays(1).toInstant().toEpochMilli())
                .queryParam(
                    "requestEndTime", ZonedDateTime.now().plusDays(10).toInstant().toEpochMilli())
                .queryParam("productType", "UNI")
                .build(),
        HttpStatus.OK.value(),
        null,
        bodyStr -> {
          log.info(bodyStr);
          assertThat(bodyStr, hasJsonPath("$.data", notNullValue()));
          assertThat(bodyStr, hasJsonPath("$.data.data", hasSize(4)));
          assertThat(bodyStr, hasJsonPath("$.data.data[0].buyer", notNullValue()));
          assertThat(bodyStr, hasJsonPath("$.data.data[0].productType", equalTo("UNI")));
        });
  }
}
