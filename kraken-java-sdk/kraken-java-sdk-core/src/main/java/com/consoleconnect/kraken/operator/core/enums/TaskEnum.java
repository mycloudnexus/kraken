package com.consoleconnect.kraken.operator.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TaskEnum {
  HTTP("http"),
  SWITCH("switch");
  private String name;
}
