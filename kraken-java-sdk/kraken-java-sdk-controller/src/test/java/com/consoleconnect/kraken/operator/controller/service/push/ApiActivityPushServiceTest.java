package com.consoleconnect.kraken.operator.controller.service.push;

import static com.consoleconnect.kraken.operator.controller.service.push.ApiActivityPushService.NO_API_ACTIVITIES_FOUND;
import static com.consoleconnect.kraken.operator.controller.service.push.ApiActivityPushService.PUSH_API_ACTIVITY_LOGS_IS_DISABLED;
import static com.consoleconnect.kraken.operator.controller.service.push.ApiActivityPushService.THE_SAME_PARAMETERS_ALREADY_EXISTS_ERROR;
import static com.consoleconnect.kraken.operator.core.service.UnifiedAssetService.getSearchPageRequest;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;

import com.consoleconnect.kraken.operator.config.TestApplication;
import com.consoleconnect.kraken.operator.controller.dto.push.CreatePushApiActivityRequest;
import com.consoleconnect.kraken.operator.controller.dto.push.PushApiActivityLogHistory;
import com.consoleconnect.kraken.operator.controller.model.Environment;
import com.consoleconnect.kraken.operator.controller.service.EnvironmentService;
import com.consoleconnect.kraken.operator.core.entity.ApiActivityLogEntity;
import com.consoleconnect.kraken.operator.core.enums.EventStatusType;
import com.consoleconnect.kraken.operator.core.enums.MgmtEventType;
import com.consoleconnect.kraken.operator.core.exception.KrakenException;
import com.consoleconnect.kraken.operator.core.model.AppProperty;
import com.consoleconnect.kraken.operator.core.repo.ApiActivityLogRepository;
import com.consoleconnect.kraken.operator.core.repo.MgmtEventRepository;
import com.consoleconnect.kraken.operator.core.request.PushLogSearchRequest;
import com.consoleconnect.kraken.operator.core.toolkit.JsonToolkit;
import com.consoleconnect.kraken.operator.core.toolkit.PagingHelper;
import com.consoleconnect.kraken.operator.test.AbstractIntegrationTest;
import com.consoleconnect.kraken.operator.test.MockIntegrationTest;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

@ActiveProfiles("test-auth-server-enabled")
@MockIntegrationTest
@ContextConfiguration(classes = TestApplication.class)
class ApiActivityPushServiceTest extends AbstractIntegrationTest {

  @Autowired private ApiActivityPushService sut;
  @Autowired private MgmtEventRepository mgmtEventRepository;
  @Autowired private EnvironmentService environmentService;
  @SpyBean private AppProperty appProperty;
  @SpyBean private ApiActivityLogRepository apiActivityLogRepository;

  @Test
  void givenApiLogSearchParam_whenCreatePushApiActivityLogInfo_thenSaveEvent() {
    // given
    var env = environmentService.findAll().get(0);
    var userId = "userId1";
    var endTime = ZonedDateTime.parse("2024-10-10T00:00:00+01:00");
    var startTime = endTime.minusDays(3);
    givenApiActivityLogs(endTime, env);
    var request = new CreatePushApiActivityRequest(startTime, endTime, env.getId());
    // when
    var created = sut.createPushApiActivityLogInfo(request, userId);
    // then
    var byId =
        mgmtEventRepository
            .findById(created.getId())
            .orElseThrow(
                () -> new RuntimeException("There should be entity with id: " + created.getId()));
    assertThat(byId.getId()).isEqualTo(created.getId());
    assertThat(byId.getEventType()).isEqualTo(MgmtEventType.PUSH_API_ACTIVITY_LOG.name());
    assertThat(byId.getStatus()).isEqualTo(EventStatusType.ACK.name());
    var payload = JsonToolkit.fromJson(byId.getPayload(), PushLogActivityLogInfo.class);
    assertThat(payload.getUser()).isEqualTo(userId);
    assertThat(payload.getStartTime()).isEqualTo(toUtcString(startTime));
    assertThat(payload.getEndTime()).isEqualTo(toUtcString(endTime));
    assertThat(payload.getEnvId()).isEqualTo(env.getId());
    assertThat(payload.getEnvName()).isEqualTo(env.getName());
  }

  @Test
  void
      givenApiLogSearchParamForTheSameEnvAndSameTimeRangeInACKStatus_whenCreatePushApiActivityLogInfo_thenError() {
    // given
    var env = environmentService.findAll().get(0);
    var userId = "userId1";
    var endTime = ZonedDateTime.parse("2024-10-10T00:00:00+01:00").minusDays(1);
    var startTime = endTime.minusDays(3);
    givenApiActivityLogs(endTime, env);
    var request = new CreatePushApiActivityRequest(startTime, endTime, env.getId());
    sut.createPushApiActivityLogInfo(request, userId);
    // when
    var krakenException =
        assertThrows(
            KrakenException.class, () -> sut.createPushApiActivityLogInfo(request, userId));
    // then
    assertThat(krakenException.getCode()).isEqualTo(400);
  }

  @Test
  void
      givenApiLogSearchParamForTheSameEnvAndSameTimeRangeInProgressStatus_whenCreatePushApiActivityLogInfo_thenError() {
    // given
    var env = environmentService.findAll().get(0);
    var userId = "userId1";
    var endTime = ZonedDateTime.parse("2024-10-10T00:00:00+01:00").minusDays(2);
    var startTime = endTime.minusDays(3);
    givenApiActivityLogs(endTime, env);
    var request = new CreatePushApiActivityRequest(startTime, endTime, env.getId());
    var pushApiActivityLogInfo = sut.createPushApiActivityLogInfo(request, userId);
    var mgmtEventEntity =
        mgmtEventRepository
            .findById(pushApiActivityLogInfo.getId())
            .orElseThrow(() -> new RuntimeException("Push log not found."));
    mgmtEventEntity.setStatus(EventStatusType.IN_PROGRESS.name());
    mgmtEventRepository.save(mgmtEventEntity);
    // whe
    var krakenException =
        assertThrows(
            KrakenException.class, () -> sut.createPushApiActivityLogInfo(request, userId));
    // then
    assertThat(krakenException.getCode()).isEqualTo(400);
    assertThat(krakenException.getMessage()).isEqualTo(THE_SAME_PARAMETERS_ALREADY_EXISTS_ERROR);
  }

  @Test
  void
      givenPushedApiActivityLogs_whenCreatePushApiActivityLogInfo_thenReturnPushLogHistoryInDescOrder() {
    // given
    givenPushApiActivityLogs();
    var pageRequest =
        getSearchPageRequest(
            PagingHelper.DEFAULT_PAGE, PagingHelper.DEFAULT_SIZE, Sort.Direction.DESC, "createdAt");
    // when
    var result = sut.searchHistory(PushLogSearchRequest.builder().build(), pageRequest);
    // then
    var logs = result.getData();
    assertThat(logs).hasSizeGreaterThan(2);
    verifyIfLogsOrderedByCreatedAtDesc(logs);
  }

  @Test
  void givenPushApiActivityLogEnabled_whenIsPushApiActivityLogEnabled_thenReturnTrue() {
    // given
    // when
    var result = sut.isPushApiActivityLogEnabled();
    // then
    assertThat(result.isEnabled()).isTrue();
  }

  @Test
  void givenPushApiActivityLogDisabled_whenIsPushApiActivityLogEnabled_thenReturnFalse() {
    // given
    givenDisabledPushActivityLogExternal();
    // when
    var result = sut.isPushApiActivityLogEnabled();
    // then
    assertThat(result.isEnabled()).isFalse();
  }

  @Test
  void givenPushApiActivityLogDisabled_whenCreatePushApiActivityLogInfo_thenReturnsError() {
    // given
    givenDisabledPushActivityLogExternal();
    var env = environmentService.findAll().get(0);
    var userId = "userId1";
    var endTime = ZonedDateTime.parse("2024-10-10T00:00:00+01:00").minusDays(1);
    var startTime = endTime.minusDays(3);
    var request = new CreatePushApiActivityRequest(startTime, endTime, env.getId());
    // when
    var krakenException =
        assertThrows(
            KrakenException.class, () -> sut.createPushApiActivityLogInfo(request, userId));
    // then
    assertThat(krakenException.getCode()).isEqualTo(400);
    assertThat(krakenException.getMessage()).isEqualTo(PUSH_API_ACTIVITY_LOGS_IS_DISABLED);
  }

  @Test
  void givenNoApiActivityLogsToPush_whenCreatePushApiActivityLogInfo_thenReturnsError() {
    // given
    var env = environmentService.findAll().get(0);
    var userId = "userId1";
    var endTime = ZonedDateTime.parse("2022-10-10T00:00:00+01:00").minusDays(1);
    var startTime = endTime.minusDays(3);
    var request = new CreatePushApiActivityRequest(startTime, endTime, env.getId());
    // when
    var krakenException =
        assertThrows(
            KrakenException.class, () -> sut.createPushApiActivityLogInfo(request, userId));
    // then
    assertThat(krakenException.getCode()).isEqualTo(400);
    assertThat(krakenException.getMessage()).isEqualTo(NO_API_ACTIVITIES_FOUND);
  }

  private void givenDisabledPushActivityLogExternal() {
    AppProperty.PushActivityLogExternal pushActivityLogExternal =
        new AppProperty.PushActivityLogExternal();
    pushActivityLogExternal.setEnabled(false);
    AppProperty.Features appFeatures = new AppProperty.Features();
    appFeatures.setPushActivityLogExternal(pushActivityLogExternal);
    doReturn(appFeatures).when(appProperty).getFeatures();
  }

  private void verifyIfLogsOrderedByCreatedAtDesc(List<PushApiActivityLogHistory> logs) {
    for (int i = 0; i < logs.size() - 1; i++) {
      assertThat(logs.get(i).getCreatedAt().isAfter(logs.get(i + 1).getCreatedAt())).isTrue();
    }
  }

  private void givenPushApiActivityLogs() {
    var env = environmentService.findAll().get(0);
    for (int i = 1; i < 4; i++) {
      var request = pushApiActivityRequest(env, i);
      givenApiActivityLogs(request.getEndTime(), env);
      sut.createPushApiActivityLogInfo(request, "userId1");
    }
  }

  private CreatePushApiActivityRequest pushApiActivityRequest(Environment env, int i) {
    var endTime = ZonedDateTime.now().minusDays(i + 5);
    var startTime = endTime.minusDays(3);
    return new CreatePushApiActivityRequest(startTime, endTime, env.getId());
  }

  private String toUtcString(ZonedDateTime zonedDateTime) {
    return zonedDateTime
        .withZoneSameInstant(ZoneOffset.UTC)
        .format(DateTimeFormatter.ISO_ZONED_DATE_TIME);
  }

  private void givenApiActivityLogs(ZonedDateTime endTime, Environment env) {
    var entity = new ApiActivityLogEntity();
    entity.setEnv(env.getId());
    entity.setCallSeq(0);
    entity.setCreatedAt(endTime.minusHours(1));
    entity.setMethod("POST");
    entity.setPath("/path");
    entity.setUri("/uri");
    entity.setRequestId(UUID.randomUUID().toString());
    apiActivityLogRepository.save(entity);
  }
}
