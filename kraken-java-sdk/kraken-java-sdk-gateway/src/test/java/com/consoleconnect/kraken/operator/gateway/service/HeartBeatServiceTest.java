package com.consoleconnect.kraken.operator.gateway.service;

import com.consoleconnect.kraken.operator.data.entity.InstanceHeartbeatEntity;
import com.consoleconnect.kraken.operator.data.repo.HeartbeatRepository;
import com.consoleconnect.kraken.operator.gateway.CustomConfig;
import com.consoleconnect.kraken.operator.test.AbstractIntegrationTest;
import com.consoleconnect.kraken.operator.test.MockIntegrationTest;
import java.util.List;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ContextConfiguration;

@Slf4j
@MockIntegrationTest
@ContextConfiguration(classes = CustomConfig.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class HeartBeatServiceTest extends AbstractIntegrationTest {
  @SpyBean private HeartBeatService heartBeatService;

  @Autowired private HeartbeatRepository heartbeatRepository;

  @SneakyThrows
  @Test
  void givenSchedulerEnabled_whenScheduled_thenHeartbeatInDb() {
    heartBeatService.heartBeat();
    Assertions.assertTrue(heartbeatRepository.count() > 0);
    List<InstanceHeartbeatEntity> heartbeats = heartbeatRepository.findAll();
    Assertions.assertNotNull(heartbeats);
    InstanceHeartbeatEntity heartbeatEntity = heartbeats.get(0);
    Assertions.assertNotNull(heartbeatEntity.getAppVersion());
  }
}
