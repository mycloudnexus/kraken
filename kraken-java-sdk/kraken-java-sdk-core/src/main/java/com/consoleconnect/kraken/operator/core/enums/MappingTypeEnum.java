package com.consoleconnect.kraken.operator.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MappingTypeEnum {
  ENUM("enum"),
  CUSTOMIZED_ENUM("customized_enum");
  private final String name;
}
