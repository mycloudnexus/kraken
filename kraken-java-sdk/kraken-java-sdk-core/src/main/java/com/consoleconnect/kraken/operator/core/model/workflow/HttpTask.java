package com.consoleconnect.kraken.operator.core.model.workflow;

import com.consoleconnect.kraken.operator.core.model.facet.ComponentAPITargetFacets;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HttpTask extends AbstractTask {
  private ComponentAPITargetFacets.Endpoint endpoints;
  private String rejectCondition;
}
