package com.consoleconnect.kraken.operator.workflow.service;

import com.consoleconnect.kraken.CustomConfig;
import com.consoleconnect.kraken.operator.core.model.UnifiedAsset;
import com.consoleconnect.kraken.operator.core.toolkit.JsonToolkit;
import com.consoleconnect.kraken.operator.core.toolkit.YamlToolkit;
import com.consoleconnect.kraken.operator.test.AbstractIntegrationTest;
import com.consoleconnect.kraken.operator.test.MockIntegrationTest;
import com.consoleconnect.kraken.operator.workflow.config.WorkflowConfig;
import com.netflix.conductor.common.metadata.workflow.WorkflowDef;
import java.util.Collections;
import java.util.Optional;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@MockIntegrationTest
@ContextConfiguration(classes = CustomConfig.class)
@Slf4j
class WorkflowTemplateTransformerTest extends AbstractIntegrationTest {

  @Autowired WorkflowConfig.BuildInTask buildInTask;
  @Autowired WorkflowTemplateTransformer transformer;

  @BeforeEach
  void init() {
    this.buildInTask.getParams().put("empty_task", Collections.emptyMap());
    this.buildInTask.getParams().put("fail_order_task", Collections.emptyMap());
    this.buildInTask.getParams().put("reject_order_task", Collections.emptyMap());
    this.buildInTask.getParams().put("process_order_task", Collections.emptyMap());
    this.buildInTask.getParams().put("notify_task", Collections.emptyMap());
  }

  @Test
  @SneakyThrows
  void givenComponentWorkflowFacets_whenTransform_thenSuccess() {
    String s = readFileToString("/mockData/api-workflow.order.eline.delete.yaml");
    Optional<UnifiedAsset> unifiedAsset = YamlToolkit.parseYaml(s, UnifiedAsset.class);
    WorkflowDef transfer = transformer.transfer(unifiedAsset.get());
    log.info("transfer result: {}", JsonToolkit.toJson(transfer));
    Assertions.assertNotNull(transfer.getTasks());
  }
}
