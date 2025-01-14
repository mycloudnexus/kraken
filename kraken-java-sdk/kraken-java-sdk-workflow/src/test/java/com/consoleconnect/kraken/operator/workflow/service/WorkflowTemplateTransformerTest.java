package com.consoleconnect.kraken.operator.workflow.service;

import com.consoleconnect.kraken.CustomConfig;
import com.consoleconnect.kraken.operator.core.model.UnifiedAsset;
import com.consoleconnect.kraken.operator.core.toolkit.JsonToolkit;
import com.consoleconnect.kraken.operator.core.toolkit.YamlToolkit;
import com.consoleconnect.kraken.operator.test.AbstractIntegrationTest;
import com.consoleconnect.kraken.operator.test.MockIntegrationTest;
import com.consoleconnect.kraken.operator.workflow.config.WorkflowConfig;
import com.netflix.conductor.common.metadata.workflow.WorkflowDef;
import java.io.IOException;
import java.util.Optional;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@MockIntegrationTest
@ContextConfiguration(classes = CustomConfig.class)
@Slf4j
class WorkflowTemplateTransformerTest extends AbstractIntegrationTest {

  @Autowired WorkflowConfig.BuildInTask buildInTask;
  @Autowired WorkflowTemplateTransformer transformer;
  @Autowired WorkflowConfig workflowConfig;

  @Test
  @SneakyThrows
  void givenComponentWorkflowFacets_whenTransform_thenSuccess() {
    verifyWorkflow("/mockData/api-workflow.order.uni.add.yaml");
    verifyWorkflow("/mockData/api-workflow.order.eline.delete.yaml");
  }

  private void verifyWorkflow(String workflow) throws IOException {
    String s = readFileToString(workflow);
    Optional<UnifiedAsset> unifiedAsset = YamlToolkit.parseYaml(s, UnifiedAsset.class);
    WorkflowDef transfer = transformer.transfer(unifiedAsset.get());
    log.info("transfer result: {}", JsonToolkit.toJson(transfer));
    Assertions.assertNotNull(transfer.getTasks());
  }
}
