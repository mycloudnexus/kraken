package com.consoleconnect.kraken.operator.gateway.service.workflow;

import com.consoleconnect.kraken.operator.core.model.UnifiedAsset;
import com.consoleconnect.kraken.operator.core.toolkit.JsonToolkit;
import com.consoleconnect.kraken.operator.core.toolkit.YamlToolkit;
import com.consoleconnect.kraken.operator.gateway.CustomConfig;
import com.consoleconnect.kraken.operator.test.AbstractIntegrationTest;
import com.consoleconnect.kraken.operator.test.MockIntegrationTest;
import com.netflix.conductor.common.metadata.workflow.WorkflowDef;
import java.util.Optional;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ContextConfiguration;

@MockIntegrationTest
@ContextConfiguration(classes = CustomConfig.class)
@Slf4j
class WorkflowTemplateTransformerTest extends AbstractIntegrationTest {

  @Test
  @SneakyThrows
  void givenComponentWorkflowFacets_whenTransform_thenSuccess() {
    String s = readFileToString("/mockData/api-workflow.order.eline.delete.yaml");
    Optional<UnifiedAsset> unifiedAsset = YamlToolkit.parseYaml(s, UnifiedAsset.class);
    WorkflowTemplateTransformer transformer = new WorkflowTemplateTransformer();
    WorkflowDef transfer = transformer.transfer(unifiedAsset.get());
    log.info("transfer result: {}", JsonToolkit.toJson(transfer));
    Assertions.assertNotNull(transfer.getTasks());
  }
}
