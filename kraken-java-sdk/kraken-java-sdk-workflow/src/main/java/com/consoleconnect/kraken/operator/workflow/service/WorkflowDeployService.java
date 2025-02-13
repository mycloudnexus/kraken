package com.consoleconnect.kraken.operator.workflow.service;

import com.consoleconnect.kraken.operator.core.enums.AssetKindEnum;
import com.consoleconnect.kraken.operator.core.enums.DeployStatusEnum;
import com.consoleconnect.kraken.operator.core.event.IngestionDataResult;
import com.consoleconnect.kraken.operator.core.exception.KrakenException;
import com.consoleconnect.kraken.operator.core.model.SyncMetadata;
import com.consoleconnect.kraken.operator.core.model.UnifiedAsset;
import com.consoleconnect.kraken.operator.core.service.UnifiedAssetService;
import com.consoleconnect.kraken.operator.core.toolkit.DateTime;
import com.consoleconnect.kraken.operator.core.toolkit.JsonToolkit;
import com.consoleconnect.kraken.operator.workflow.model.WorkflowDeploymentFacets;
import com.fasterxml.jackson.core.type.TypeReference;
import com.netflix.conductor.common.metadata.workflow.WorkflowDef;
import io.orkes.conductor.client.http.OrkesMetadataClient;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class WorkflowDeployService {

  public static final String LABEL_WORKFLOW_DEF_VERSION = "workflow-def-version";

  private final OrkesMetadataClient metadataClient;

  private final UnifiedAssetService unifiedAssetService;

  private final WorkflowTemplateTransformer workflowTemplateTransformer;

  public AssetKindEnum getKind() {
    return AssetKindEnum.COMPONENT_API_WORK_FLOW;
  }

  public void deployWorkflow(UnifiedAsset asset) {
    log.info("Transforming workflow");
    try {
      WorkflowDef workflowDef = workflowTemplateTransformer.transfer(asset);
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
    WorkflowDeploymentFacets workflowDeploymentFacets =
        WorkflowDeploymentFacets.builder()
            .assetVersion(workflow.getMetadata().getVersion())
            .targetVersion(version)
            .build();
    Map<String, Object> facets =
        JsonToolkit.fromJson(
            JsonToolkit.toJson(workflowDeploymentFacets), new TypeReference<>() {});
    deployment.setFacets(facets);

    IngestionDataResult result =
        unifiedAssetService.syncAsset(
            workflow.getMetadata().getKey(), deployment, syncMetadata, true);
    if (result.getCode() != HttpStatus.OK.value()) {
      throw new KrakenException(result.getCode(), result.getMessage());
    }
    return deployment;
  }

  private int computeNewVersion(UnifiedAsset asset, String processName) {
    WorkflowDef workflowDef = metadataClient.getWorkflowDef(processName, null);
    return workflowDef != null ? workflowDef.getVersion() + 1 : asset.getMetadata().getVersion();
  }
}
