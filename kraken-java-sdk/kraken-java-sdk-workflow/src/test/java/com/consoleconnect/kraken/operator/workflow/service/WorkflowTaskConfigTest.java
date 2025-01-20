package com.consoleconnect.kraken.operator.workflow.service;

import com.consoleconnect.kraken.CustomConfig;
import com.consoleconnect.kraken.operator.core.model.AppProperty;
import com.consoleconnect.kraken.operator.test.AbstractIntegrationTest;
import com.consoleconnect.kraken.operator.test.MockIntegrationTest;
import com.consoleconnect.kraken.operator.workflow.config.WorkflowConfig;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@MockIntegrationTest
@ContextConfiguration(classes = CustomConfig.class)
@Slf4j
class WorkflowTaskConfigTest extends AbstractIntegrationTest {
  @Autowired private WorkflowConfig config;
  @Autowired private AppProperty appProperty;

  @Test
  void givenDataPlaneContext_whenGetBuildinTasks_thenReturnSuccess() {
    Assertions.assertNotNull(config.getApiClient(appProperty));
  }
}
