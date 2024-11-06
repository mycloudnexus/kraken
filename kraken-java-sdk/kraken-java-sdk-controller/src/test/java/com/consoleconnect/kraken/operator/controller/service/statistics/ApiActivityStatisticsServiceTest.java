package com.consoleconnect.kraken.operator.controller.service.statistics;

import static org.assertj.core.api.Assertions.assertThat;

import com.consoleconnect.kraken.operator.config.TestApplication;
import com.consoleconnect.kraken.operator.controller.handler.ClientAPIAuditLogEventHandler;
import com.consoleconnect.kraken.operator.core.client.ClientEvent;
import com.consoleconnect.kraken.operator.core.client.ClientEventTypeEnum;
import com.consoleconnect.kraken.operator.core.dto.ApiActivityLog;
import com.consoleconnect.kraken.operator.core.request.ApiStatisticsSearchRequest;
import com.consoleconnect.kraken.operator.core.toolkit.JsonToolkit;
import com.consoleconnect.kraken.operator.test.AbstractIntegrationTest;
import com.consoleconnect.kraken.operator.test.MockIntegrationTest;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

@ActiveProfiles("test-auth-server-enabled")
@MockIntegrationTest
@ContextConfiguration(classes = TestApplication.class)
class ApiActivityStatisticsServiceTest extends AbstractIntegrationTest {

  public static final String BUYER_ID_1 = "buyerId1";
  public static final String BUYER_ID_2 = "buyerId2";
  public static final String NOW_WITH_TIMEZONE = "2023-10-24T05:00:00+02:00";

  @Autowired private ApiActivityStatisticsService sut;
  @Autowired private ClientAPIAuditLogEventHandler clientAPIAuditLogEventHandler;

  @Test
  void givenNoLogsForEnv_whenLoadRequestStatistics_thenEmptyResult() {
    // given
    var envId = UUID.randomUUID();
    var now = ZonedDateTime.parse(NOW_WITH_TIMEZONE);
    var searchRequest =
        ApiStatisticsSearchRequest.builder()
            .env(envId.toString())
            .queryStart(now.minusDays(2))
            .queryEnd(now)
            .build();
    // when
    var result = sut.loadRequestStatistics(searchRequest);
    // then
    assertThat(result.getRequestStatistics()).isEmpty();
  }

  @Test
  void givenLogsForEnv_whenLoadRequestStatisticsOutOfTheTimeRage_thenEmptyResult() {
    // given
    var envId = UUID.randomUUID();
    var now = ZonedDateTime.parse(NOW_WITH_TIMEZONE);
    addApiLogActivity(envId.toString(), createPayloads(toUTC(now)));
    var searchRequest =
        ApiStatisticsSearchRequest.builder()
            .env(envId.toString())
            .queryStart(now.minusDays(4))
            .queryEnd(now.minusDays(2))
            .build();
    // when
    var result = sut.loadRequestStatistics(searchRequest);
    // then
    assertThat(result.getRequestStatistics()).isEmpty();
  }

  private ZonedDateTime toUTC(ZonedDateTime now) {
    return now.withZoneSameInstant(ZoneId.of("UTC"));
  }

  @Test
  void givenApiActivityLogs_whenLoadRequestStatistics_thenReturnsApiRequestsStatisticsSortedAsc() {
    // given
    var envId = UUID.randomUUID();
    var now = ZonedDateTime.parse(NOW_WITH_TIMEZONE);
    addApiLogActivity(envId.toString(), createPayloads(toUTC(now)));
    var searchRequest =
        ApiStatisticsSearchRequest.builder()
            .env(envId.toString())
            .queryStart(now.minusDays(2))
            .queryEnd(now)
            .build();
    // when
    var result = sut.loadRequestStatistics(searchRequest);
    // then
    assertThat(result.getRequestStatistics()).hasSize(2);
    var statsFor1Day = result.getRequestStatistics().get(0);
    var statsFor2Day = result.getRequestStatistics().get(1);
    assertThat(statsFor1Day.getDate()).isBefore(statsFor2Day.getDate());
    assertThat(statsFor1Day.getSuccess()).isEqualTo(8);
    assertThat(statsFor1Day.getError()).isEqualTo(8);
    assertThat(statsFor2Day.getSuccess()).isEqualTo(12);
    assertThat(statsFor2Day.getError()).isEqualTo(12);
  }

  @Test
  void givenApiActivityLogs_whenLoadRequestStatisticsByBuyerId_thenReturnsApiRequestsStatistics() {
    // given
    var envId = UUID.randomUUID();
    var now = ZonedDateTime.parse(NOW_WITH_TIMEZONE);
    addApiLogActivity(envId.toString(), createPayloads(toUTC(now)));
    var searchRequest =
        ApiStatisticsSearchRequest.builder()
            .env(envId.toString())
            .queryStart(now.minusDays(2))
            .queryEnd(now)
            .buyerId(BUYER_ID_1)
            .build();
    // when
    var result = sut.loadRequestStatistics(searchRequest);
    // then
    assertThat(result.getRequestStatistics()).hasSize(2);
    var statsFor1Day = result.getRequestStatistics().get(0);
    assertThat(statsFor1Day.getSuccess()).isEqualTo(8);
    assertThat(statsFor1Day.getError()).isEqualTo(4);
    var statsFor2Day = result.getRequestStatistics().get(1);
    assertThat(statsFor2Day.getSuccess()).isEqualTo(12);
    assertThat(statsFor2Day.getError()).isEqualTo(6);
  }

  @Test
  void givenNoErrorLogsForEnv_whenLoadErrorsStatistics_thenEmptyResult() {
    // given
    var envId = UUID.randomUUID();
    var now = ZonedDateTime.parse(NOW_WITH_TIMEZONE);
    var searchRequest =
        ApiStatisticsSearchRequest.builder()
            .env(envId.toString())
            .queryStart(now.minusDays(2))
            .queryEnd(now)
            .build();
    // when
    var result = sut.loadErrorsStatistics(searchRequest);
    // then
    assertThat(result.getErrorBreakdowns()).isEmpty();
  }

  @Test
  void givenErrorLogsForEnv_whenLoadErrorsStatisticsOutOfTheTimeRage_thenEmptyResult() {
    // given
    var envId = UUID.randomUUID();
    var now = ZonedDateTime.parse(NOW_WITH_TIMEZONE);
    addApiLogActivity(envId.toString(), createPayloads(toUTC(now)));
    var searchRequest =
        ApiStatisticsSearchRequest.builder()
            .env(envId.toString())
            .queryStart(now.minusDays(4))
            .queryEnd(now.minusDays(2))
            .build();
    // when
    var result = sut.loadErrorsStatistics(searchRequest);
    // then
    assertThat(result.getErrorBreakdowns()).isEmpty();
  }

  @Test
  void givenErrorApiActivityLogs_whenLoadErrorsStatistics_thenReturnsErrorStatisticsSortedAsc() {
    // given
    var envId = UUID.randomUUID();
    var now = ZonedDateTime.parse(NOW_WITH_TIMEZONE);
    addApiLogActivity(envId.toString(), createPayloads(toUTC(now)));
    var searchRequest =
        ApiStatisticsSearchRequest.builder()
            .env(envId.toString())
            .queryStart(now.minusDays(2))
            .queryEnd(now)
            .build();
    // when
    var result = sut.loadErrorsStatistics(searchRequest);
    // then
    assertThat(result.getErrorBreakdowns()).hasSize(2);
    var errorBreakdown0 = result.getErrorBreakdowns().get(0);
    var errorBreakdown1 = result.getErrorBreakdowns().get(1);
    assertThat(errorBreakdown0.getDate()).isBefore(errorBreakdown1.getDate());
    assertThat(errorBreakdown0.getErrors()).containsEntry(401, 4L).containsEntry(500, 4L);
    assertThat(errorBreakdown1.getErrors()).containsEntry(401, 6L).containsEntry(500, 6L);
  }

  @Test
  void givenErrorApiActivityLogs_whenLoadErrorsStatisticsByBuyerId_thenReturnsErrorStatistics() {
    // given
    var envId = UUID.randomUUID();
    var now = ZonedDateTime.parse(NOW_WITH_TIMEZONE);
    addApiLogActivity(envId.toString(), createPayloads(toUTC(now)));
    var searchRequest =
        ApiStatisticsSearchRequest.builder()
            .env(envId.toString())
            .queryStart(now.minusDays(2))
            .queryEnd(now)
            .buyerId(BUYER_ID_1)
            .build();
    // when
    var result = sut.loadErrorsStatistics(searchRequest);
    // then
    assertThat(result.getErrorBreakdowns()).hasSize(2);
    assertThat(result.getErrorBreakdowns().get(0).getErrors()).containsEntry(401, 4L);
    assertThat(result.getErrorBreakdowns().get(1).getErrors()).containsEntry(401, 6L);
  }

  @Test
  void givenNoLogsForEnv_whenLoadMostPopularEndpointStatistics_thenEmptyResult() {
    // given
    var envId = UUID.randomUUID();
    var now = ZonedDateTime.parse(NOW_WITH_TIMEZONE);
    var searchRequest =
        ApiStatisticsSearchRequest.builder()
            .env(envId.toString())
            .queryStart(now.minusDays(2))
            .queryEnd(now)
            .build();
    // when
    var result = sut.loadMostPopularEndpointStatistics(searchRequest);
    // then
    assertThat(result.getEndpointUsages()).isEmpty();
  }

  @Test
  void givenNoLogsForEnv_whenLoadMostPopularEndpointStatisticsOutOfTheTimeRage_thenEmptyResult() {
    // given
    var envId = UUID.randomUUID();
    var now = ZonedDateTime.parse(NOW_WITH_TIMEZONE);
    var payload = payloadForEndpointPopularity(toUTC(now));
    addApiLogActivity(envId.toString(), payload);
    var searchRequest =
        ApiStatisticsSearchRequest.builder()
            .env(envId.toString())
            .queryStart(now.minusDays(4))
            .queryEnd(now.minusDays(2))
            .build();
    // when
    var result = sut.loadMostPopularEndpointStatistics(searchRequest);
    // then
    assertThat(result.getEndpointUsages()).isEmpty();
  }

  @Test
  void givenApiActivityLogs_whenLoadMostPopularEndpointStatistics_thenReturnsEndpointsStatistics() {
    // given
    var envId = UUID.randomUUID();
    var now = ZonedDateTime.parse(NOW_WITH_TIMEZONE);
    var payload = payloadForEndpointPopularity(toUTC(now));
    addApiLogActivity(envId.toString(), payload);
    var searchRequest =
        ApiStatisticsSearchRequest.builder()
            .env(envId.toString())
            .queryStart(now.minusDays(2))
            .queryEnd(now)
            .build();
    // when
    var result = sut.loadMostPopularEndpointStatistics(searchRequest);
    // then
    assertThat(result.getEndpointUsages()).hasSize(7);
    assertThat(result.getEndpointUsages().get(0).getMethod()).isEqualTo("GET");
    assertThat(result.getEndpointUsages().get(0).getEndpoint())
        .isEqualTo("/mefApi/sonata/product/0");
    assertThat(result.getEndpointUsages().get(0).getUsage()).isEqualTo(30);
    assertThat(result.getEndpointUsages().get(0).getPopularity()).isEqualTo(30.0);
    assertThat(result.getEndpointUsages().get(6).getMethod()).isEqualTo("DELETE");
    assertThat(result.getEndpointUsages().get(6).getEndpoint())
        .isEqualTo("/mefApi/sonata/product/6");
    assertThat(result.getEndpointUsages().get(6).getUsage()).isEqualTo(5);
    assertThat(result.getEndpointUsages().get(6).getPopularity()).isEqualTo(5.0);
  }

  @Test
  void
      givenApiActivityLogs_whenLoadMostPopularEndpointStatisticsByBuyerId_thenReturnsEndpointsStatistics() {
    // given
    var envId = UUID.randomUUID();
    var now = ZonedDateTime.parse(NOW_WITH_TIMEZONE);
    var payload = payloadForEndpointPopularity(toUTC(now));
    addApiLogActivity(envId.toString(), payload);
    var searchRequest =
        ApiStatisticsSearchRequest.builder()
            .env(envId.toString())
            .queryStart(now.minusDays(2))
            .queryEnd(now)
            .buyerId(BUYER_ID_2)
            .build();
    // when
    var result = sut.loadMostPopularEndpointStatistics(searchRequest);
    // then
    assertThat(result.getEndpointUsages()).hasSize(1);
    assertThat(result.getEndpointUsages().get(0).getMethod()).isEqualTo("GET");
    assertThat(result.getEndpointUsages().get(0).getEndpoint())
        .isEqualTo("/mefApi/sonata/product/9");
    assertThat(result.getEndpointUsages().get(0).getUsage()).isEqualTo(2);
    assertThat(result.getEndpointUsages().get(0).getPopularity()).isEqualTo(2.0);
  }

  private List<String> payloadForEndpointPopularity(ZonedDateTime now) {
    var payload = new ArrayList<String>();
    payload.addAll(
        PayloadBuilder.builder()
            .method("GET")
            .path0("/mefApi/sonata/product/0")
            .path1("/hub/product/0")
            .httpStatus(200)
            .now(now)
            .number(30)
            .buyerId(BUYER_ID_1)
            .build()
            .createPayload());
    payload.addAll(
        PayloadBuilder.builder()
            .method("POST")
            .path0("/mefApi/sonata/product/1")
            .path1("/hub/product/1")
            .httpStatus(200)
            .now(now)
            .number(20)
            .buyerId(BUYER_ID_1)
            .build()
            .createPayload());
    payload.addAll(
        PayloadBuilder.builder()
            .method("DELETE")
            .path0("/mefApi/sonata/product/2")
            .path1("/hub/product/2")
            .httpStatus(201)
            .now(now)
            .number(12)
            .buyerId(BUYER_ID_1)
            .build()
            .createPayload());
    payload.addAll(
        PayloadBuilder.builder()
            .method("PUT")
            .path0("/mefApi/sonata/product/3")
            .path1("/hub/product/3")
            .httpStatus(204)
            .now(now)
            .number(10)
            .build()
            .createPayload());
    payload.addAll(
        PayloadBuilder.builder()
            .method("GET")
            .path0("/mefApi/sonata/product/4")
            .path1("/hub/product/4")
            .httpStatus(400)
            .now(now)
            .number(8)
            .buyerId(BUYER_ID_1)
            .build()
            .createPayload());
    payload.addAll(
        PayloadBuilder.builder()
            .method("POST")
            .path0("/mefApi/sonata/product/5")
            .path1("/hub/product/5")
            .httpStatus(401)
            .now(now)
            .number(6)
            .buyerId(BUYER_ID_1)
            .build()
            .createPayload());
    payload.addAll(
        PayloadBuilder.builder()
            .method("DELETE")
            .path0("/mefApi/sonata/product/6")
            .path1("/hub/product/6")
            .httpStatus(402)
            .now(now)
            .number(5)
            .buyerId(BUYER_ID_1)
            .build()
            .createPayload());
    payload.addAll(
        PayloadBuilder.builder()
            .method("PUT")
            .path0("/mefApi/sonata/product/7")
            .path1("/hub/product/7")
            .httpStatus(404)
            .now(now)
            .number(4)
            .buyerId(BUYER_ID_1)
            .build()
            .createPayload());
    payload.addAll(
        PayloadBuilder.builder()
            .method("GET")
            .path0("/mefApi/sonata/product/8")
            .path1("/hub/product/8")
            .httpStatus(500)
            .now(now)
            .number(3)
            .buyerId(BUYER_ID_1)
            .build()
            .createPayload());
    payload.addAll(
        PayloadBuilder.builder()
            .method("GET")
            .path0("/mefApi/sonata/product/9")
            .path1("/hub/product/9")
            .httpStatus(500)
            .now(now)
            .number(2)
            .buyerId(BUYER_ID_2)
            .build()
            .createPayload());
    return payload;
  }

  private void addApiLogActivity(String envId, List<String> payloads) {
    payloads.forEach(
        p -> {
          clientAPIAuditLogEventHandler.onEvent(
              envId, UUID.randomUUID().toString(), createEvent(p));
        });
  }

  private List<String> createPayloads(ZonedDateTime now) {
    var payload = new ArrayList<String>();
    payload.addAll(
        PayloadBuilder.builder()
            .method("GET")
            .path0("/mefApi/sonata/product/123")
            .path1("/hub/product/123")
            .httpStatus(200)
            .now(now)
            .number(10)
            .buyerId(BUYER_ID_1)
            .build()
            .createPayload());
    payload.addAll(
        PayloadBuilder.builder()
            .method("GET")
            .path0("/mefApi/sonata/product/234")
            .path1("/hub/product/234")
            .httpStatus(204)
            .now(now)
            .number(10)
            .buyerId(BUYER_ID_1)
            .build()
            .createPayload());
    payload.addAll(
        PayloadBuilder.builder()
            .method("GET")
            .path0("/mefApi/sonata/order/678")
            .path1("/hub/order/678")
            .httpStatus(401)
            .now(now)
            .number(10)
            .buyerId(BUYER_ID_1)
            .build()
            .createPayload());
    payload.addAll(
        PayloadBuilder.builder()
            .method("GET")
            .path0("/mefApi/sonata/product/789")
            .path1("/hub/product/789")
            .httpStatus(500)
            .now(now)
            .number(10)
            .buyerId("buyer2")
            .build()
            .createPayload());
    return payload;
  }

  private static ClientEvent createEvent(String json) {
    var clientEvent = new ClientEvent();
    clientEvent.setEventType(ClientEventTypeEnum.CLIENT_API_AUDIT_LOG);
    clientEvent.setClientId("127.0.1.1");
    clientEvent.setEventPayload(json);
    return clientEvent;
  }

  @lombok.Builder
  public static class PayloadBuilder {
    private String method;
    private String path0;
    private String path1;
    private int httpStatus;
    private ZonedDateTime now;
    private int number;
    private String buyerId;

    List<String> createPayload() {
      return IntStream.range(0, number)
          .mapToObj(
              operand -> {
                ZonedDateTime date = now.minusHours(operand);
                return createPayload(
                    method, path0, path1, httpStatus, date, date.plusSeconds(2), buyerId);
              })
          .toList();
    }

    private String createPayload(
        String method,
        String path0,
        String path1,
        Integer httpStatus,
        ZonedDateTime createdAt0,
        ZonedDateTime createdAt1,
        String buyerId) {
      var requestId = UUID.randomUUID();
      var apiActivityLog0 =
          getApiActivityLog0(method, path0, httpStatus, createdAt0, requestId, buyerId);
      var apiActivityLog1 =
          getApiActivityLog1(method, path1, httpStatus, createdAt1, requestId, buyerId);
      return JsonToolkit.toJson(List.of(apiActivityLog0, apiActivityLog1));
    }

    private ApiActivityLog getApiActivityLog0(
        String method,
        String path0,
        Integer httpStatus,
        ZonedDateTime createdAt0,
        UUID requestId,
        String buyerId) {
      var apiActivityLog0 = new ApiActivityLog();
      apiActivityLog0.setRequestId(requestId.toString());
      apiActivityLog0.setCallSeq(0);
      apiActivityLog0.setUri("http://localhost:8888/mef.sonata");
      apiActivityLog0.setMethod(method);
      apiActivityLog0.setPath(path0);
      apiActivityLog0.setHttpStatusCode(httpStatus);
      apiActivityLog0.setCreatedAt(createdAt0);
      apiActivityLog0.setBuyer(buyerId);
      return apiActivityLog0;
    }

    private ApiActivityLog getApiActivityLog1(
        String method,
        String path1,
        Integer httpStatus,
        ZonedDateTime createdAt1,
        UUID requestId,
        String buyerId) {
      var apiActivityLog1 = new ApiActivityLog();
      apiActivityLog1.setRequestId(requestId.toString());
      apiActivityLog1.setCallSeq(1);
      apiActivityLog1.setUri("http://localhost:8888/mef.sonata");
      apiActivityLog1.setMethod(method);
      apiActivityLog1.setPath(path1);
      apiActivityLog1.setHttpStatusCode(httpStatus);
      apiActivityLog1.setCreatedAt(createdAt1);
      apiActivityLog1.setBuyer(buyerId);
      return apiActivityLog1;
    }
  }
}
