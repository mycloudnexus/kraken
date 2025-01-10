package com.consoleconnect.kraken.operator.workflow.service;

import com.consoleconnect.kraken.operator.core.enums.AssetKindEnum;
import com.consoleconnect.kraken.operator.core.enums.DeployStatusEnum;
import com.consoleconnect.kraken.operator.core.event.IngestionDataResult;
import com.consoleconnect.kraken.operator.core.exception.KrakenException;
import com.consoleconnect.kraken.operator.core.model.SyncMetadata;
import com.consoleconnect.kraken.operator.core.model.UnifiedAsset;
import com.consoleconnect.kraken.operator.core.service.UnifiedAssetService;
import com.consoleconnect.kraken.operator.core.toolkit.DateTime;
import com.netflix.conductor.common.metadata.workflow.WorkflowDef;
import io.orkes.conductor.client.http.OrkesMetadataClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class WorkflowDeployService {

  public static final String KEY_WORKFLOW_DEF = "workflowDef";

  public static final String LABEL_WORKFLOW_DEF_VERSION = "workflow-def-version";

  private final OrkesMetadataClient metadataClient;

  private final UnifiedAssetService unifiedAssetService;

  private final WorkflowTemplateTransformer workflowTemplateTransformer;

  public WorkflowDeployService(
      final OrkesMetadataClient metadataClient,
      final UnifiedAssetService unifiedAssetService,
      final WorkflowTemplateTransformer workflowTemplateTransformer) {
    this.metadataClient = metadataClient;
    this.unifiedAssetService = unifiedAssetService;
    this.workflowTemplateTransformer = workflowTemplateTransformer;
  }

  public AssetKindEnum getKind() {
    return AssetKindEnum.COMPONENT_API_WORK_FLOW;
  }

  public void deployWorkflow(UnifiedAsset asset) {
    log.info("Transforming workflow");
    WorkflowDef workflowDef = workflowTemplateTransformer.transfer(asset);
    try {
      int version = computeNewVersion(asset, workflowDef.getName());
      workflowDef.setVersion(version);
      log.info("Deploying workflow to conductor, {} v{}", workflowDef.getName(), version);
      metadataClient.registerWorkflowDef(workflowDef);
      createWorkflowDeployment(asset, workflowDef);
    } catch (Exception e) {
      log.error("Failed to register workflow", e);
      throw KrakenException.internalError("Failed to deploy conductor");
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
    deployment.getMetadata().getLabels().put(LABEL_WORKFLOW_DEF_VERSION, "" + version);

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
