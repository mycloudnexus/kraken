package com.consoleconnect.kraken.operator.service;

import com.consoleconnect.kraken.operator.config.TestApplication;
import com.consoleconnect.kraken.operator.controller.service.HeartBeatService;
import com.consoleconnect.kraken.operator.core.entity.EnvironmentClientEntity;
import com.consoleconnect.kraken.operator.core.enums.ClientReportTypeEnum;
import com.consoleconnect.kraken.operator.core.enums.EnvNameEnum;
import com.consoleconnect.kraken.operator.core.repo.EnvironmentClientRepository;
import com.consoleconnect.kraken.operator.core.toolkit.IpUtils;
import com.consoleconnect.kraken.operator.test.AbstractIntegrationTest;
import com.consoleconnect.kraken.operator.test.MockIntegrationTest;
import java.util.Optional;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ContextConfiguration;

@Slf4j
@MockIntegrationTest
@ContextConfiguration(classes = {TestApplication.class})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class HeartBeatServiceTest extends AbstractIntegrationTest {

  @SpyBean private HeartBeatService heartBeatService;
  @Autowired private EnvironmentClientRepository environmentClientRepository;

  @SneakyThrows
  @Test
  void givenSchedulerEnabled_whenScheduled_thenHeartbeatInDb() {
    heartBeatService.heartBeat();
    Optional<EnvironmentClientEntity> optionalEnvironmentClient =
        environmentClientRepository.findOneByEnvIdAndClientKeyAndKind(
            EnvNameEnum.UNDEFINED.name(),
            IpUtils.getHostAddress(),
            ClientReportTypeEnum.HEARTBEAT_CONTROL_PLANE.name());
    Assertions.assertTrue(optionalEnvironmentClient.isPresent());
    EnvironmentClientEntity entity = optionalEnvironmentClient.get();
    Assertions.assertNotNull(entity.getAppVersion());
  }
}
