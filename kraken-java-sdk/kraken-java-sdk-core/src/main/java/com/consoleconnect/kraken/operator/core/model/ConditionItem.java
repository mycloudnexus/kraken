package com.consoleconnect.kraken.operator.core.model;

import com.consoleconnect.kraken.operator.core.enums.OperatorEnum;
import lombok.Data;

@Data
public class ConditionItem {
  private OperatorEnum operator;
  private String value;
  // example: task.a.b
  private String expression;
}
