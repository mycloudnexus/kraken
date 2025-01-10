package com.consoleconnect.kraken.operator.workflow.service;

import com.consoleconnect.kraken.operator.core.enums.AssetKindEnum;
import com.consoleconnect.kraken.operator.core.ingestion.AbstractAssetEventListener;
import com.consoleconnect.kraken.operator.core.ingestion.DataIngestionJob;
import com.consoleconnect.kraken.operator.core.model.AppProperty;
import com.consoleconnect.kraken.operator.core.model.FileDescriptor;
import com.consoleconnect.kraken.operator.core.model.UnifiedAsset;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class WorkflowEventListener extends AbstractAssetEventListener {

  private final WorkflowDeployService workflowDeployService;

  public WorkflowEventListener(
      final ApplicationEventPublisher eventPublisher,
      final AppProperty appProperty,
      final WorkflowDeployService workflowDeployService) {
    super(eventPublisher, appProperty);
    this.workflowDeployService = workflowDeployService;
  }

  public AssetKindEnum getKind() {
    return AssetKindEnum.COMPONENT_API_WORK_FLOW;
  }

  @Override
  public void onPostPersist(
      String productId, FileDescriptor fileDescriptor, UnifiedAsset asset, DataIngestionJob job) {
    this.workflowDeployService.deployWorkflow(asset);
  }
}
