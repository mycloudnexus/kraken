package com.consoleconnect.kraken.operator.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AchieveScopeEnum {
  BASIC("BASIC"),
  DETAIL("DETAIL");

  private final String scope;
}
