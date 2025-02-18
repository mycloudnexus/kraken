package com.consoleconnect.kraken.operator.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OperatorEnum {
  EQUAL("=="),
  NOT_EQUAL("!="),
  GREATER_THAN(">"),
  GREATER_THAN_OR_EQUAL_TO(">="),
  LESS_THAN_OR_EQUAL_TO("<="),
  IN("in");
  private String description;
}
