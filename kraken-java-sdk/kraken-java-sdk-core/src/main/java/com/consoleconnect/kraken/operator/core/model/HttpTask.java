package com.consoleconnect.kraken.operator.core.model;

import com.consoleconnect.kraken.operator.core.enums.JoinOperatorEnum;
import com.consoleconnect.kraken.operator.core.model.facet.ComponentAPITargetFacets;
import java.util.List;
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
    private JoinOperatorEnum join;
    private List<ConditionItem> conditionItems;
    private String buildInTask;
  }
}
