package com.consoleconnect.kraken.operator.workflow.service;

import static org.mockito.ArgumentMatchers.*;

import com.consoleconnect.kraken.operator.core.entity.UnifiedAssetEntity;
import com.consoleconnect.kraken.operator.core.enums.AssetKindEnum;
import com.consoleconnect.kraken.operator.core.model.SyncMetadata;
import com.consoleconnect.kraken.operator.core.model.UnifiedAsset;
import com.consoleconnect.kraken.operator.core.service.UnifiedAssetService;
import com.consoleconnect.kraken.operator.core.toolkit.YamlToolkit;
import com.consoleconnect.kraken.operator.test.AbstractIntegrationTest;
import com.consoleconnect.kraken.operator.test.MockIntegrationTest;
import io.orkes.conductor.client.http.OrkesMetadataClient;
import lombok.SneakyThrows;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

@MockIntegrationTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class WorkflowEventListenerTest extends AbstractIntegrationTest {

  @Autowired WorkflowEventListener workflowEventListener;

  @Autowired UnifiedAssetService assetService;

  @MockBean OrkesMetadataClient metaDataClient;

  @SneakyThrows
  @Test
  void givenOrderDeploymentReceived_whenDeploy_thenSuccess() {

    Mockito.doNothing().when(metaDataClient).registerWorkflowDef(any());

    UnifiedAsset workflowAsset = createWorkflow();
    workflowEventListener.onPostPersist("", null, workflowAsset, null);

    Mockito.verify(metaDataClient, Mockito.times(1)).registerWorkflowDef(any());

    UnifiedAssetEntity deployment =
        assetService.findOneByIdOrKey("mef.sonata.api-workflow.order.eline.delete.1");
    Assertions.assertEquals(
        AssetKindEnum.COMPONENT_API_WORK_FLOW_DEPLOYMENT.getKind(), deployment.getKind());
  }

  @SneakyThrows
  private UnifiedAsset createWorkflow() {
    String s = readFileToString("/mockData/api-workflow.order.eline.delete.yaml");
    UnifiedAsset workflowAsset = YamlToolkit.parseYaml(s, UnifiedAsset.class).get();

    SyncMetadata syncMetadata = new SyncMetadata();
    assetService.syncAsset(null, workflowAsset, syncMetadata, true);

    return workflowAsset;
  }
}
