package com.consoleconnect.kraken.operator.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum JoinOperatorEnum {
  OR("||"),
  AND("&&");
  private String description;
}
