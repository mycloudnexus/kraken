package com.consoleconnect.kraken.operator.sync.service.push;

import static org.assertj.core.api.Assertions.assertThat;

import com.consoleconnect.kraken.operator.core.entity.ApiActivityLogEntity;
import com.consoleconnect.kraken.operator.core.entity.MgmtEventEntity;
import com.consoleconnect.kraken.operator.core.enums.EventStatusType;
import com.consoleconnect.kraken.operator.core.enums.MgmtEventType;
import com.consoleconnect.kraken.operator.core.model.HttpResponse;
import com.consoleconnect.kraken.operator.core.repo.ApiActivityLogRepository;
import com.consoleconnect.kraken.operator.core.repo.MgmtEventRepository;
import com.consoleconnect.kraken.operator.core.toolkit.JsonToolkit;
import com.consoleconnect.kraken.operator.sync.CustomConfig;
import com.consoleconnect.kraken.operator.test.AbstractIntegrationTest;
import com.consoleconnect.kraken.operator.test.MockIntegrationTest;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import lombok.SneakyThrows;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@MockIntegrationTest
@ContextConfiguration(classes = {CustomConfig.class})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PushAPIActivityLogSchedulerTest extends AbstractIntegrationTest {

  public static final String ENV_ID = "envId1";
  static MockWebServer mockWebServer = new MockWebServer();
  static final String NOW_WITH_TIMEZONE = "2023-10-24T05:00:00+02:00";
  static final String BUYER_ID_1 = "buyerId1";

  @Autowired private PushAPIActivityLogScheduler sut;
  @Autowired private ApiActivityLogRepository apiActivityLogRepository;
  @Autowired private MgmtEventRepository mgmtEventRepository;

  @BeforeAll
  @SneakyThrows
  static void setUp() {
    mockWebServer.start(23456);
  }

  @AfterAll
  @SneakyThrows
  static void tearDown() {
    mockWebServer.shutdown();
  }

  @Test
  void givenApiLogs_whenPushApiActivityLogToExternalSystem_thenAllLogsSentAndEventInStatusDone() {
    // given
    givenExternalServerResponses();

    var endTime = ZonedDateTime.parse(NOW_WITH_TIMEZONE);
    var startTime = ZonedDateTime.parse(NOW_WITH_TIMEZONE).minusDays(1);
    var logs = createLogs(toUTC(endTime), ENV_ID);
    apiActivityLogRepository.saveAll(logs);
    var logEvent = createPushApiActivityLogEvent(ENV_ID, startTime, endTime, "userId1");
    // when
    var sent = sut.pushApiActivityLogToExternalSystem();
    // then
    var done =
        mgmtEventRepository
            .findById(logEvent.getId())
            .orElseThrow(
                () -> new RuntimeException("There should be entity with id: " + logEvent.getId()));
    assertThat(done.getStatus()).isEqualTo(EventStatusType.DONE.name());
    assertThat(sent).hasSize(2);
    verifyPage0(sent.get(0), logEvent);
    verifyPage1(sent.get(1), logEvent);
  }

  private void verifyPage0(PushExternalSystemPayload page0, MgmtEventEntity logEvent) {
    assertThat(page0.getData().getTotal()).isEqualTo(40);
    assertThat(page0.getData().getPage()).isEqualTo(0);
    assertThat(page0.getData().getSize()).isEqualTo(30);
    assertThat(page0.getData().getData()).hasSize(30);
    verifyData(page0, logEvent);
  }

  private static void verifyData(PushExternalSystemPayload page0, MgmtEventEntity logEvent) {
    assertThat(page0.getId()).isEqualTo(logEvent.getId());
    var payload = JsonToolkit.fromJson(logEvent.getPayload(), PushLogActivityLogInfo.class);
    assertThat(page0.getEnvName()).isEqualTo(payload.getEnvName());
    assertThat(page0.getStartTime()).isEqualTo(payload.getStartTime());
    assertThat(page0.getEndTime()).isEqualTo(payload.getEndTime());
  }

  private void verifyPage1(PushExternalSystemPayload page1, MgmtEventEntity logEvent) {
    assertThat(page1.getData().getTotal()).isEqualTo(40);
    assertThat(page1.getData().getPage()).isEqualTo(1);
    assertThat(page1.getData().getSize()).isEqualTo(30);
    assertThat(page1.getData().getData()).hasSize(10);
    verifyData(page1, logEvent);
  }

  @Test
  void givenApiLogs_whenPushApiActivityLogToExternalSystemAndReturnError_thenEventInStatusFailed() {
    // given
    givenFailedExternalSystemResponse();
    var endTime = ZonedDateTime.parse(NOW_WITH_TIMEZONE);
    var startTime = ZonedDateTime.parse(NOW_WITH_TIMEZONE).minusDays(1);
    var logs = createLogs(toUTC(endTime), ENV_ID);
    apiActivityLogRepository.saveAll(logs);
    var logEvent = createPushApiActivityLogEvent(ENV_ID, startTime, endTime, "userId1");
    // when
    var sent = sut.pushApiActivityLogToExternalSystem();
    // then
    var done =
        mgmtEventRepository
            .findById(logEvent.getId())
            .orElseThrow(
                () -> new RuntimeException("There should be entity with id: " + logEvent.getId()));
    assertThat(done.getStatus()).isEqualTo(EventStatusType.FAILED.name());
    assertThat(sent).isEmpty();
  }

  private static void givenFailedExternalSystemResponse() {
    mockResponse(500);
  }

  private static void givenExternalServerResponses() {
    IntStream.range(0, 2)
        .forEach(
            it -> {
              mockResponse(200);
            });
  }

  private static void mockResponse(int code) {
    MockResponse mockResponse = new MockResponse();
    mockResponse.setResponseCode(code);
    mockResponse.setBody(JsonToolkit.toJson(HttpResponse.ok("Sync")));
    mockResponse.addHeader("Content-Type", "application/json");
    mockWebServer.enqueue(mockResponse);
  }

  private List<ApiActivityLogEntity> createLogs(ZonedDateTime now, String envId) {
    var payload = new ArrayList<ApiActivityLogEntity>();
    payload.addAll(
        PayloadBuilder.builder()
            .envId(envId)
            .method("GET")
            .path0("/mefApi/sonata/product/123")
            .path1("/hub/product/123")
            .httpStatus(200)
            .now(now.minusHours(1))
            .number(10)
            .buyerId(BUYER_ID_1)
            .build()
            .createPayload());
    payload.addAll(
        PayloadBuilder.builder()
            .envId(envId)
            .method("GET")
            .path0("/mefApi/sonata/product/234")
            .path1("/hub/product/234")
            .httpStatus(204)
            .now(now.minusHours(2))
            .number(10)
            .buyerId(BUYER_ID_1)
            .build()
            .createPayload());
    payload.addAll(
        PayloadBuilder.builder()
            .envId(envId)
            .method("GET")
            .path0("/mefApi/sonata/order/678")
            .path1("/hub/order/678")
            .httpStatus(401)
            .now(now.minusHours(3))
            .number(10)
            .buyerId(BUYER_ID_1)
            .build()
            .createPayload());
    payload.addAll(
        PayloadBuilder.builder()
            .envId(envId)
            .method("GET")
            .path0("/mefApi/sonata/product/789")
            .path1("/hub/product/789")
            .httpStatus(500)
            .now(now.minusHours(4))
            .number(10)
            .buyerId("buyer2")
            .build()
            .createPayload());
    return payload;
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
    private String envId;

    List<ApiActivityLogEntity> createPayload() {
      return IntStream.range(0, number)
          .mapToObj(
              operand -> {
                ZonedDateTime date = now.minusHours(operand);
                return createEntity(
                    method, path0, path1, httpStatus, date, date.plusSeconds(2), buyerId, envId);
              })
          .flatMap(stream -> stream)
          .toList();
    }

    private Stream<ApiActivityLogEntity> createEntity(
        String method,
        String path0,
        String path1,
        Integer httpStatus,
        ZonedDateTime createdAt0,
        ZonedDateTime createdAt1,
        String buyerId,
        String envId) {
      var requestId = UUID.randomUUID();
      var apiActivityLog0 =
          getApiActivityLog0(method, path0, httpStatus, createdAt0, requestId, buyerId, envId);
      var apiActivityLog1 =
          getApiActivityLog1(method, path1, httpStatus, createdAt1, requestId, buyerId, envId);
      return Stream.of(apiActivityLog0, apiActivityLog1);
    }

    private ApiActivityLogEntity getApiActivityLog0(
        String method,
        String path0,
        Integer httpStatus,
        ZonedDateTime createdAt0,
        UUID requestId,
        String buyerId,
        String envId) {
      var apiActivityLog0 = new ApiActivityLogEntity();
      apiActivityLog0.setEnv(envId);
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

    private ApiActivityLogEntity getApiActivityLog1(
        String method,
        String path1,
        Integer httpStatus,
        ZonedDateTime createdAt1,
        UUID requestId,
        String buyerId,
        String envId) {
      var apiActivityLog1 = new ApiActivityLogEntity();
      apiActivityLog1.setEnv(envId);
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

  private ZonedDateTime toUTC(ZonedDateTime now) {
    return now.withZoneSameInstant(ZoneId.of("UTC"));
  }

  public MgmtEventEntity createPushApiActivityLogEvent(
      String envId, ZonedDateTime startTime, ZonedDateTime endTime, String userId) {
    var entity = new MgmtEventEntity();
    entity.setStatus(EventStatusType.ACK.name());
    entity.setEventType(MgmtEventType.PUSH_API_ACTIVITY_LOG.name());
    entity.setPayload(createData(envId, startTime, endTime, userId));
    return mgmtEventRepository.save(entity);
  }

  private PushLogActivityLogInfo createData(
      String envId, ZonedDateTime startTime, ZonedDateTime endTime, String userId) {
    var data = new PushLogActivityLogInfo();
    data.setUser(userId);
    data.setEnvId(envId);
    data.setStartTime(startTime);
    data.setEndTime(endTime);
    return data;
  }
}
