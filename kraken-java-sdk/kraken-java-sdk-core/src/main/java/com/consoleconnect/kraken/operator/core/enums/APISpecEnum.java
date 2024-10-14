package com.consoleconnect.kraken.operator.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum APISpecEnum {
  BASE("base"),
  CUSTOMIZED("customized");
  private final String kind;
}
