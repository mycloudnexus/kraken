package com.consoleconnect.kraken.operator.sync.service;

import static org.mockito.Mockito.*;

import com.consoleconnect.kraken.operator.core.entity.ApiActivityLogEntity;
import com.consoleconnect.kraken.operator.core.enums.SyncStatusEnum;
import com.consoleconnect.kraken.operator.core.repo.ApiActivityLogRepository;
import com.consoleconnect.kraken.operator.sync.CustomConfig;
import com.consoleconnect.kraken.operator.test.AbstractIntegrationTest;
import com.consoleconnect.kraken.operator.test.MockIntegrationTest;
import java.time.ZonedDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ContextConfiguration;

@MockIntegrationTest
@ContextConfiguration(classes = {CustomConfig.class})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DeleteLogServiceTest extends AbstractIntegrationTest {
  @Autowired ApiActivityLogRepository apiActivityLogRepository;
  @SpyBean private DeleteLogService deleteLogService;

  @Test
  void givenUnSyncedLogs_whenSync_thenLogsSynced() {

    // given
    ApiActivityLogEntity apiActivityLogEntity = new ApiActivityLogEntity();
    apiActivityLogEntity.setSyncStatus(SyncStatusEnum.UNDEFINED);
    apiActivityLogEntity.setRequestId(UUID.randomUUID().toString());
    apiActivityLogEntity.setMethod("GET");
    apiActivityLogEntity.setCreatedAt(ZonedDateTime.now().minusMonths(5));
    apiActivityLogEntity.setPath("/api/v1/test");
    apiActivityLogEntity.setUri("http://localhost:8080/api/v1/test");

    apiActivityLogEntity = apiActivityLogRepository.save(apiActivityLogEntity);
    var result = this.apiActivityLogRepository.findAll();
    Assertions.assertEquals(result.size(), 1);

    // when run it again
    deleteLogService.runIt();

    result = this.apiActivityLogRepository.findAll();
    Assertions.assertEquals(result.size(), 0);
  }
}
