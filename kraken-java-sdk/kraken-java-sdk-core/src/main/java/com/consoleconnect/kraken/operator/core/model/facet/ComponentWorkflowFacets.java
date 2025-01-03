package com.consoleconnect.kraken.operator.core.model.facet;

import com.consoleconnect.kraken.operator.core.model.workflow.HttpTask;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ComponentWorkflowFacets {
  private String mapperKey;
  private String externalId;
  private List<HttpTask> validationStage;
  private List<HttpTask> preparationStage;
  private List<HttpTask> executionStage;
}
