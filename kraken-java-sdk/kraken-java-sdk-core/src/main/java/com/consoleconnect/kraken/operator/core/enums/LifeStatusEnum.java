package com.consoleconnect.kraken.operator.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum LifeStatusEnum {
  LIVE("LIVE"),
  ARCHIVED("ARCHIVED");

  private final String status;
}
