package com.consoleconnect.kraken.operator.workflow.service;

import com.consoleconnect.kraken.CustomConfig;
import com.consoleconnect.kraken.operator.core.model.AppProperty;
import com.consoleconnect.kraken.operator.test.AbstractIntegrationTest;
import com.consoleconnect.kraken.operator.test.MockIntegrationTest;
import com.consoleconnect.kraken.operator.workflow.config.WorkflowConfig;
import java.util.ArrayList;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

@MockIntegrationTest
@ActiveProfiles("enable-workflow-cluster")
@ContextConfiguration(classes = CustomConfig.class)
@Slf4j
class WorkflowTaskConfigTest extends AbstractIntegrationTest {
  @Autowired private WorkflowConfig config;
  @Autowired private AppProperty appProperty;

  @Test
  void givenDataPlaneContext_whenApiClient_thenReturnSuccess() {
    Assertions.assertNotNull(config.getApiClient(appProperty));
  }

  @Test
  void givenDisableClusterContext_whenStart_thenReturnSuccess() {
    appProperty.getWorkflow().setClusterUrl(new ArrayList<>());
    Assertions.assertDoesNotThrow(() -> config.init());
  }
}
