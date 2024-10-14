package com.consoleconnect.kraken.operator.gateway.service;

import com.consoleconnect.kraken.operator.data.repo.HeartbeatRepository;
import com.consoleconnect.kraken.operator.gateway.CustomConfig;
import com.consoleconnect.kraken.operator.test.AbstractIntegrationTest;
import com.consoleconnect.kraken.operator.test.MockIntegrationTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@Slf4j
@MockIntegrationTest
@ContextConfiguration(classes = CustomConfig.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class HeartBeatServiceTest extends AbstractIntegrationTest {
  @Autowired HeartBeatService heartBeatService;

  @Autowired private HeartbeatRepository heartbeatRepository;

  @Test
  void givenSchedulerEnabled_whenScheduled_thenHeartbeatInDb() {
    heartBeatService.heartBeat();
    Assertions.assertTrue(heartbeatRepository.count() > 0);
  }
}
