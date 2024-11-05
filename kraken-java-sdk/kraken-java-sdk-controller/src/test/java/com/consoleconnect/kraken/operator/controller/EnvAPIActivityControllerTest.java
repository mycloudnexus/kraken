package com.consoleconnect.kraken.operator.controller;

import static com.jayway.jsonassert.impl.matcher.IsCollectionWithSize.hasSize;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import com.consoleconnect.kraken.operator.config.TestApplication;
import com.consoleconnect.kraken.operator.controller.dto.BuyerAssetDto;
import com.consoleconnect.kraken.operator.controller.model.Environment;
import com.consoleconnect.kraken.operator.controller.service.EnvironmentService;
import com.consoleconnect.kraken.operator.core.entity.ApiActivityLogEntity;
import com.consoleconnect.kraken.operator.core.model.UnifiedAsset;
import com.consoleconnect.kraken.operator.core.model.facet.BuyerOnboardFacets;
import com.consoleconnect.kraken.operator.core.repo.ApiActivityLogRepository;
import com.consoleconnect.kraken.operator.core.toolkit.IpUtils;
import com.consoleconnect.kraken.operator.core.toolkit.JsonToolkit;
import com.consoleconnect.kraken.operator.test.AbstractIntegrationTest;
import com.consoleconnect.kraken.operator.test.MockIntegrationTest;
import java.time.ZonedDateTime;
import java.util.*;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;

@Slf4j
@Getter
@MockIntegrationTest
@ContextConfiguration(classes = {TestApplication.class})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ActiveProfiles("test-rs256")
class EnvAPIActivityControllerTest extends AbstractIntegrationTest
    implements ApiActivityLogCreator, EnvCreator, BuyerCreator {
  WebTestClientHelper webTestClient;
  @Autowired ApiActivityLogRepository apiActivityLogRepository;
  @Autowired EnvironmentService environmentService;

  private String requestId;

  @Autowired
  public EnvAPIActivityControllerTest(WebTestClient webTestClient) {
    this.webTestClient = new WebTestClientHelper(webTestClient);
  }

  @Test
  @Order(1)
  void givenFakedActivityId_whenSearchActivityDetail_thenReturnEmpty() {
    Environment envStage = createStage(PRODUCT_ID);
    String activityBaseUrl =
        String.format("/products/%s/envs/%s/api-activities", PRODUCT_ID, envStage.getId());
    webTestClient.requestAndVerify(
        HttpMethod.GET,
        uriBuilder -> uriBuilder.path(activityBaseUrl + "/{activityId}").build("11"),
        HttpStatus.OK.value(),
        null,
        bodyStr -> {
          log.info(bodyStr);
          assertThat(bodyStr, hasJsonPath("$.message", notNullValue()));
          IpUtils.getIP(MockServerHttpRequest.get("/123").build());
        });
  }

  @Test
  @Order(2)
  void givenExistedActivityId_whenSearchDetail_thenReturnOK() {
    Environment envStage = createStage(PRODUCT_ID);
    String activityBaseUrl =
        String.format("/products/%s/envs/%s/api-activities", PRODUCT_ID, envStage.getId());
    BuyerAssetDto buyerAssetDto =
        createBuyer(BUYER_ID + "-" + System.currentTimeMillis(), envStage.getId(), COMPANY_NAME);
    BuyerOnboardFacets buyerFacets =
        UnifiedAsset.getFacets(buyerAssetDto, BuyerOnboardFacets.class);
    ApiActivityLogEntity apiActivityLogEntity =
        createApiActivityLog(buyerFacets.getBuyerInfo().getBuyerId(), envStage.getId());
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
  @Order(3)
  void givenTimeRange_whenSearchActivities_thenReturnOK() {
    Environment envStage = createStage(PRODUCT_ID);
    log.info("envId:{}", envStage.getId());
    String activityBaseUrl =
        String.format("/products/%s/envs/%s/api-activities", PRODUCT_ID, envStage.getId());
    webTestClient.requestAndVerify(
        HttpMethod.GET,
        uriBuilder ->
            uriBuilder
                .path(activityBaseUrl)
                .queryParam("envId", envStage.getId())
                .queryParam("path", "/123")
                .queryParam("method", "GET")
                .queryParam(
                    "requestStartTime", ZonedDateTime.now().minusDays(1).toInstant().toEpochMilli())
                .queryParam(
                    "requestEndTime", ZonedDateTime.now().plusDays(10).toInstant().toEpochMilli())
                .build(),
        HttpStatus.OK.value(),
        null,
        bodyStr -> {
          log.info(bodyStr);
          assertThat(bodyStr, hasJsonPath("$.data", notNullValue()));
          assertThat(bodyStr, hasJsonPath("$.data.data", hasSize(1)));
          assertThat(bodyStr, hasJsonPath("$.data.data[0].buyerName", equalTo("console connect")));
        });
  }

  @Test
  @Order(4)
  void testGetIP() {
    MockServerHttpRequest mockServerHttpRequest = MockServerHttpRequest.get("/123").build();
    ServerHttpRequest serverHttpRequest =
        mockServerHttpRequest
            .mutate()
            .header("x-forwarded-for", "192.168.1.11,192.16.1.10")
            .build();
    String ip = IpUtils.getIP(serverHttpRequest);
    assertThat(ip, notNullValue());
  }
}
