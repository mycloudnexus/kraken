package com.consoleconnect.kraken.operator.core.model.workflow;

import com.consoleconnect.kraken.operator.core.model.facet.ComponentAPITargetFacets;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HttpTask extends AbstractTask {
  private ComponentAPITargetFacets.Endpoint endpoint;
  private ConditionCheck conditionCheck;
  private String notificationUrl;

  @Data
  public static class ConditionCheck {
    private String caseExpression;
    private String buildInTask;
  }
}
