package com.consoleconnect.kraken.operator.controller.api;

import static java.util.List.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.consoleconnect.kraken.operator.config.TestApplication;
import com.consoleconnect.kraken.operator.controller.WebTestClientHelper;
import com.consoleconnect.kraken.operator.controller.dto.statistics.ApiRequestActivityStatistics;
import com.consoleconnect.kraken.operator.controller.dto.statistics.EndpointUsage;
import com.consoleconnect.kraken.operator.controller.dto.statistics.ErrorApiRequestStatistics;
import com.consoleconnect.kraken.operator.controller.dto.statistics.ErrorBreakdown;
import com.consoleconnect.kraken.operator.controller.dto.statistics.MostPopularEndpointStatistics;
import com.consoleconnect.kraken.operator.controller.dto.statistics.RequestStatistics;
import com.consoleconnect.kraken.operator.controller.service.statistics.ApiActivityStatisticsService;
import com.consoleconnect.kraken.operator.core.model.HttpResponse;
import com.consoleconnect.kraken.operator.core.request.ApiStatisticsSearchRequest;
import com.consoleconnect.kraken.operator.test.AbstractIntegrationTest;
import com.consoleconnect.kraken.operator.test.MockIntegrationTest;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.UUID;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;

@ActiveProfiles("test-auth-server-enabled")
@MockIntegrationTest
@ContextConfiguration(classes = TestApplication.class)
class EnvAPIActivityStatisticsControllerTest extends AbstractIntegrationTest {

  public static final ZonedDateTime START_DATE = ZonedDateTime.parse("2023-10-24T00:00:00-03:00");
  public static final ZonedDateTime END_DATE = ZonedDateTime.parse("2023-10-25T00:00:00-03:00");
  public static final String BUYER_ID_1 = "buyerId1";
  @MockBean private ApiActivityStatisticsService service;
  @Autowired private ObjectMapper objectMapper;

  private final WebTestClientHelper testClientHelper;

  @Autowired
  EnvAPIActivityStatisticsControllerTest(WebTestClient webTestClient) {
    testClientHelper = new WebTestClientHelper(webTestClient);
  }

  @Test
  void givenApiActivityLogs_whenGettingApiRequestStatistics_thenReturnsOk() {
    // given
    var envId = UUID.randomUUID();
    var searchRequest =
        ApiStatisticsSearchRequest.builder()
            .env(envId.toString())
            .queryStart(START_DATE)
            .queryEnd(END_DATE)
            .buyerId(BUYER_ID_1)
            .build();

    var apiRequestActivityStatistics =
        new ApiRequestActivityStatistics(
            of(new RequestStatistics(START_DATE.toLocalDate(), 100L, 200L)));
    when(service.loadRequestStatistics(searchRequest)).thenReturn(apiRequestActivityStatistics);
    // when
    var path =
        String.format("/products/%s/envs/%s/statistics/api-activity-requests", "productId", envId);
    testClientHelper.getAndVerify(
        (uriBuilder ->
            uriBuilder
                .path(path)
                .queryParam("requestStartTime", START_DATE)
                .queryParam("requestEndTime", END_DATE)
                .queryParam("buyerId", BUYER_ID_1)
                .build()),
        bodyStr -> {
          // then
          var result =
              content(bodyStr, new TypeReference<HttpResponse<ApiRequestActivityStatistics>>() {});
          assertThat(result.getData()).isEqualTo(apiRequestActivityStatistics);
        });
  }

  @Test
  void givenApiActivityLogs_whenLoadErrorsStatistics_thenReturnsOk() {
    // given
    var envId = UUID.randomUUID();

    var searchRequest =
        ApiStatisticsSearchRequest.builder()
            .env(envId.toString())
            .queryStart(START_DATE)
            .queryEnd(END_DATE)
            .buyerId(BUYER_ID_1)
            .build();

    var errors =
        new ErrorApiRequestStatistics(
            of(new ErrorBreakdown(START_DATE.toLocalDate(), new HashMap<>())));
    when(service.loadErrorsStatistics(searchRequest)).thenReturn(errors);
    // when
    var path = String.format("/products/%s/envs/%s/statistics/error-requests", "productId", envId);
    testClientHelper.getAndVerify(
        (uriBuilder ->
            uriBuilder
                .path(path)
                .queryParam("requestStartTime", START_DATE)
                .queryParam("requestEndTime", END_DATE)
                .queryParam("buyerId", BUYER_ID_1)
                .build()),
        bodyStr -> {
          // then
          var result =
              content(bodyStr, new TypeReference<HttpResponse<ErrorApiRequestStatistics>>() {});
          assertThat(result.getData()).isEqualTo(errors);
        });
  }

  @Test
  void givenApiActivityLogs_whenLoadMostPopularEndpointStatistics_thenReturnsOk() {
    // given
    var envId = UUID.randomUUID();

    var searchRequest =
        ApiStatisticsSearchRequest.builder()
            .env(envId.toString())
            .queryStart(START_DATE)
            .queryEnd(END_DATE)
            .buyerId(BUYER_ID_1)
            .build();

    var popular =
        new MostPopularEndpointStatistics(of(new EndpointUsage("GEt", "/path/1", 100L, 1.1f)));
    when(service.loadMostPopularEndpointStatistics(searchRequest)).thenReturn(popular);
    // when
    var path =
        String.format("/products/%s/envs/%s/statistics/most-popular-endpoint", "productId", envId);
    testClientHelper.getAndVerify(
        (uriBuilder ->
            uriBuilder
                .path(path)
                .queryParam("requestStartTime", START_DATE)
                .queryParam("requestEndTime", END_DATE)
                .queryParam("buyerId", BUYER_ID_1)
                .build()),
        bodyStr -> {
          // then
          var result =
              content(bodyStr, new TypeReference<HttpResponse<MostPopularEndpointStatistics>>() {});
          assertThat(result.getData()).isEqualTo(popular);
        });
  }

  @SneakyThrows
  private <T> T content(String response, TypeReference<T> typeReference) {
    return objectMapper.readValue(response, typeReference);
  }
}
