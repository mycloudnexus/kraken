package com.consoleconnect.kraken.operator.core.model.facet;

import com.consoleconnect.kraken.operator.core.model.HttpTask;
import java.util.List;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ComponentWorkflowFacets {
  private WorkflowMetaData metaData;
  private List<ComponentAPITargetFacets.Mapper> validationMapper;
  private List<HttpTask> validationStage;
  private List<HttpTask> preparationStage;
  private List<HttpTask> executionStage;

  @Data
  public static class WorkflowMetaData {
    private String workflowName;
    private String externalId;
  }
}
