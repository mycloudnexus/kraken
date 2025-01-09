package com.consoleconnect.kraken.operator.transform;

import static com.consoleconnect.kraken.operator.core.enums.AssetLinkKindEnum.*;

import com.consoleconnect.kraken.operator.core.enums.AssetKindEnum;
import com.consoleconnect.kraken.operator.core.enums.DeployStatusEnum;
import com.consoleconnect.kraken.operator.core.event.IngestionDataResult;
import com.consoleconnect.kraken.operator.core.exception.KrakenException;
import com.consoleconnect.kraken.operator.core.ingestion.AbstractAssetEventListener;
import com.consoleconnect.kraken.operator.core.ingestion.DataIngestionJob;
import com.consoleconnect.kraken.operator.core.model.*;
import com.consoleconnect.kraken.operator.core.service.UnifiedAssetService;
import com.consoleconnect.kraken.operator.core.toolkit.DateTime;
import com.consoleconnect.kraken.operator.gateway.service.workflow.WorkflowTemplateTransformer;
import com.netflix.conductor.common.metadata.workflow.WorkflowDef;
import io.orkes.conductor.client.http.OrkesMetadataClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class WorkflowEventListener extends AbstractAssetEventListener {

  public static final String KEY_WORKFLOW_DEF = "workflowDef";

  private final OrkesMetadataClient metadataClient;

  private final UnifiedAssetService unifiedAssetService;

  public WorkflowEventListener(
      final ApplicationEventPublisher eventPublisher,
      final AppProperty appProperty,
      final OrkesMetadataClient metadataClient,
      final UnifiedAssetService unifiedAssetService) {
    super(eventPublisher, appProperty);
    this.metadataClient = metadataClient;
    this.unifiedAssetService = unifiedAssetService;
  }

  public AssetKindEnum getKind() {
    return AssetKindEnum.COMPONENT_API_WORK_FLOW;
  }

  @Override
  public void onPostPersist(
      String productId, FileDescriptor fileDescriptor, UnifiedAsset asset, DataIngestionJob job) {
    log.info("Transforming workflow");
    WorkflowTemplateTransformer transformer = new WorkflowTemplateTransformer();
    WorkflowDef workflowDef = transformer.transfer(asset);
    try {
      int version = computeNewVersion(asset, workflowDef.getName());
      workflowDef.setVersion(version);
      log.info("Deploying workflow to conductor, {} v{}", workflowDef.getName(), version);
      metadataClient.registerWorkflowDef(workflowDef);
      createWorkflowDeployment(asset, workflowDef);
    } catch (Exception e) {
      log.error("Failed to register workflow", e);
    }
    log.info("Deploying workflow completed");
  }

  private UnifiedAsset createWorkflowDeployment(UnifiedAsset workflow, WorkflowDef workflowDef) {
    final int version = workflowDef.getVersion();
    final String key = workflow.getMetadata().getKey() + "." + version;
    final UnifiedAsset deployment =
        UnifiedAsset.of(
            AssetKindEnum.COMPONENT_API_WORK_FLOW_DEPLOYMENT.getKind(),
            key,
            workflow.getMetadata().getName() + "." + version);

    SyncMetadata syncMetadata = new SyncMetadata("", "", DateTime.nowInUTCString());
    deployment.getMetadata().setStatus(DeployStatusEnum.SUCCESS.name());
    deployment.getMetadata().setVersion(version);

    IngestionDataResult result =
        unifiedAssetService.syncAsset(
            workflow.getMetadata().getKey(), deployment, syncMetadata, true);
    if (result.getCode() != 200) {
      throw new KrakenException(result.getCode(), result.getMessage());
    }
    return deployment;
  }

  private int computeNewVersion(UnifiedAsset asset, String processName) {
    WorkflowDef workflowDef = metadataClient.getWorkflowDef(processName, null);
    return workflowDef != null ? workflowDef.getVersion() + 1 : asset.getMetadata().getVersion();
  }
}
