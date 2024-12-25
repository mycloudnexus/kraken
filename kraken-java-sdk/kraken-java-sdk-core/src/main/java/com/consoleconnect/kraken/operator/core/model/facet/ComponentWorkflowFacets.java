package com.consoleconnect.kraken.operator.core.model.facet;

import com.consoleconnect.kraken.operator.core.model.workflow.AbstractTask;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ComponentWorkflowFacets {
  private String id;
  private String key;
  private String mapperKey;
  private String externalId;
  private List<AbstractTask> preparationStage;
  private List<AbstractTask> executionStage;
}
