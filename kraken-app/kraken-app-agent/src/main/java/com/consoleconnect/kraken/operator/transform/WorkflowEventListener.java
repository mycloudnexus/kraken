package com.consoleconnect.kraken.operator.transform;

import com.consoleconnect.kraken.operator.core.enums.AssetKindEnum;
import com.consoleconnect.kraken.operator.core.ingestion.AbstractAssetEventListener;
import com.consoleconnect.kraken.operator.core.ingestion.DataIngestionJob;
import com.consoleconnect.kraken.operator.core.model.AppProperty;
import com.consoleconnect.kraken.operator.core.model.FileDescriptor;
import com.consoleconnect.kraken.operator.core.model.UnifiedAsset;
import com.consoleconnect.kraken.operator.gateway.service.workflow.WorkflowTemplateTransformer;
import com.netflix.conductor.common.metadata.workflow.WorkflowDef;
import io.orkes.conductor.client.http.OrkesMetadataClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class WorkflowEventListener extends AbstractAssetEventListener {

  private final OrkesMetadataClient metadataClient;

  public WorkflowEventListener(
      ApplicationEventPublisher eventPublisher,
      AppProperty appProperty,
      OrkesMetadataClient metadataClient) {
    super(eventPublisher, appProperty);
    this.metadataClient = metadataClient;
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
    log.info(
        "Deploying workflow to conductor, {} v{}", workflowDef.getName(), workflowDef.getVersion());
    try {
      metadataClient.registerWorkflowDef(workflowDef);
    } catch (Exception e) {
      log.error("Failed to register workflow", e);
    }
    log.info("Deploying workflow completed");
  }
}
