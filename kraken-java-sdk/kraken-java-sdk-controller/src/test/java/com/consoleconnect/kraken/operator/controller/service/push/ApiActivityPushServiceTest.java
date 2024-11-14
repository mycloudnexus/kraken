package com.consoleconnect.kraken.operator.controller.service.push;

import static com.consoleconnect.kraken.operator.core.service.UnifiedAssetService.getSearchPageRequest;
import static org.assertj.core.api.Assertions.assertThat;

import com.consoleconnect.kraken.operator.config.TestApplication;
import com.consoleconnect.kraken.operator.controller.dto.push.CreatePushApiActivityRequest;
import com.consoleconnect.kraken.operator.controller.dto.push.PushApiActivityLogHistory;
import com.consoleconnect.kraken.operator.controller.model.Environment;
import com.consoleconnect.kraken.operator.controller.service.EnvironmentService;
import com.consoleconnect.kraken.operator.core.enums.EventStatusType;
import com.consoleconnect.kraken.operator.core.enums.MgmtEventType;
import com.consoleconnect.kraken.operator.core.repo.MgmtEventRepository;
import com.consoleconnect.kraken.operator.core.request.PushLogSearchRequest;
import com.consoleconnect.kraken.operator.core.toolkit.JsonToolkit;
import com.consoleconnect.kraken.operator.core.toolkit.PagingHelper;
import com.consoleconnect.kraken.operator.test.AbstractIntegrationTest;
import com.consoleconnect.kraken.operator.test.MockIntegrationTest;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
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

  @Test
  void givenApiLogSearchParam_whenCreatePushApiActivityLogInfo_thenSaveEvent() {
    // given
    var env = environmentService.findAll().get(0);
    var userId = "userId1";
    var endTime = ZonedDateTime.now().truncatedTo(ChronoUnit.MILLIS);
    var startTime = endTime.minusDays(3);
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
    assertThat(payload.getStartTime()).isEqualTo(startTime);
    assertThat(payload.getEndTime()).isEqualTo(endTime);
    assertThat(payload.getEnvId()).isEqualTo(env.getId());
    assertThat(payload.getEnvName()).isEqualTo(env.getName());
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
    assertThat(logs.size()).isGreaterThan(2);
    verifyIfLogsOrderedByCreatedAtDesc(logs);
  }

  private void verifyIfLogsOrderedByCreatedAtDesc(List<PushApiActivityLogHistory> logs) {
    for (int i = 0; i < logs.size() - 1; i++) {
      assertThat(logs.get(i).getCreatedAt().isAfter(logs.get(i + 1).getCreatedAt())).isTrue();
    }
  }

  @SneakyThrows
  private void givenPushApiActivityLogs() {
    var env = environmentService.findAll().get(0);
    for (int i = 0; i < 3; i++) {
      Thread.sleep(10);
      var request = pushApiActivityRequest(env);
      sut.createPushApiActivityLogInfo(request, "userId1");
    }
  }

  private CreatePushApiActivityRequest pushApiActivityRequest(Environment env) {
    var endTime = ZonedDateTime.now().truncatedTo(ChronoUnit.MILLIS);
    var startTime = endTime.minusDays(3);
    return new CreatePushApiActivityRequest(startTime, endTime, env.getId());
  }
}
