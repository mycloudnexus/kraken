package com.consoleconnect.kraken.operator.controller.dto;

import com.consoleconnect.kraken.operator.core.model.UnifiedAsset;
import lombok.Data;

@Data
public class SaveWorkflowTemplateRequest {
  private UnifiedAsset mappingTemplate;
  private UnifiedAsset workflowTemplate;
}
