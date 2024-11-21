package com.consoleconnect.kraken.operator.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MappingTypeEnum {
  ENUM("enum"),
  DISCRETE("discrete");
  private final String name;
}
