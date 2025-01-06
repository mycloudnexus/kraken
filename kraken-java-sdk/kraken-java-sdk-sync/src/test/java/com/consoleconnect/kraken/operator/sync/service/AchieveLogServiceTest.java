package com.consoleconnect.kraken.operator.sync.service;

import com.consoleconnect.kraken.operator.core.repo.ApiActivityLogRepository;
import com.consoleconnect.kraken.operator.sync.CustomConfig;
import com.consoleconnect.kraken.operator.test.AbstractIntegrationTest;
import com.consoleconnect.kraken.operator.test.MockIntegrationTest;
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
class AchieveLogServiceTest extends AbstractIntegrationTest {
  @Autowired ApiActivityLogRepository apiActivityLogRepository;
  @SpyBean private AchieveApiActivityLogService achieveApiActivityLogService;

  @Test
  void givenAchieveSuccess_whenAchieveAll_thenSuccess() {

    // when
    achieveApiActivityLogService.runIt();
    Assertions.assertNotNull(this.achieveApiActivityLogService);
  }
}
