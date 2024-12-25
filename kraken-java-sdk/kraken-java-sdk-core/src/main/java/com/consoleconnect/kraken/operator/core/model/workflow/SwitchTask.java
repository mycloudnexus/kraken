package com.consoleconnect.kraken.operator.core.model.workflow;

import java.util.List;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SwitchTask extends AbstractTask {
  private List<DecisionCase> decisionCases;
  private String defaultNextTask;

  @Data
  static class DecisionCase {
    private String onCondition;
    private String then;
  }
}
