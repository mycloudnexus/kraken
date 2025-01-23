package com.consoleconnect.kraken.operator.controller.dto;

import com.consoleconnect.kraken.operator.core.entity.UnifiedAssetEntity;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SaveWorkflowTemplateDTO {
  private UnifiedAssetEntity mappingTemplate;
  private UnifiedAssetEntity workflowTemplate;
}
