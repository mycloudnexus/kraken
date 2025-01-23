package com.consoleconnect.kraken.operator.sync.service;

import static org.mockito.Mockito.*;

import com.consoleconnect.kraken.operator.core.entity.ApiActivityLogEntity;
import com.consoleconnect.kraken.operator.core.enums.LifeStatusEnum;
import com.consoleconnect.kraken.operator.core.enums.SyncStatusEnum;
import com.consoleconnect.kraken.operator.core.model.HttpResponse;
import com.consoleconnect.kraken.operator.core.repo.ApiActivityLogRepository;
import com.consoleconnect.kraken.operator.core.toolkit.DateTime;
import com.consoleconnect.kraken.operator.sync.CustomConfig;
import com.consoleconnect.kraken.operator.test.AbstractIntegrationTest;
import com.consoleconnect.kraken.operator.test.MockIntegrationTest;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ContextConfiguration;

@MockIntegrationTest
@ContextConfiguration(classes = {CustomConfig.class})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PushLogServiceTest extends AbstractIntegrationTest {
  @Autowired ApiActivityLogRepository apiActivityLogRepository;
  @SpyBean private PushLogService pushLogService;

  @Test
  void givenUnSyncedLogs_whenSync_thenLogsSynced() {

    // mock pushEvent
    doReturn(HttpResponse.ok(null)).when(pushLogService).pushEvent(Mockito.any());

    // given
    ApiActivityLogEntity apiActivityLogEntity = createApiActivityLogEntity();

    // when
    pushLogService.runIt();

    // then
    verify(pushLogService, times(1)).pushEvent(Mockito.any());
    Optional<ApiActivityLogEntity> apiActivityLogEntityOptional =
        apiActivityLogRepository.findById(apiActivityLogEntity.getId());
    Assertions.assertTrue(apiActivityLogEntityOptional.isPresent());

    Assertions.assertEquals(
        SyncStatusEnum.SYNCED, apiActivityLogEntityOptional.get().getSyncStatus());
    Assertions.assertNotNull(apiActivityLogEntityOptional.get().getSyncedAt());

    // when run it again
    pushLogService.runIt();
    verify(pushLogService, times(1)).pushEvent(Mockito.any());
  }

  private ApiActivityLogEntity createApiActivityLogEntity() {
    ApiActivityLogEntity apiActivityLogEntity = new ApiActivityLogEntity();
    apiActivityLogEntity.setSyncStatus(SyncStatusEnum.UNDEFINED);
    apiActivityLogEntity.setLifeStatus(LifeStatusEnum.LIVE);
    apiActivityLogEntity.setRequestId(UUID.randomUUID().toString());
    apiActivityLogEntity.setMethod("GET");
    apiActivityLogEntity.setPath("/api/v1/test");
    apiActivityLogEntity.setUri("http://localhost:8080/api/v1/test");
    apiActivityLogEntity.setCreatedAt(DateTime.futureInUTC(ChronoUnit.MINUTES, -100));
    return apiActivityLogRepository.save(apiActivityLogEntity);
  }
}
