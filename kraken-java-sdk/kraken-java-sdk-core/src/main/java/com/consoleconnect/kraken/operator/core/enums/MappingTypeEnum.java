package com.consoleconnect.kraken.operator.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MappingTypeEnum {
  ENUM("enum", "normal enumeration"),
  DISCRETE_VAR("discrete_var", "discrete variable"),
  CONTINUOUS_VAR("continuous_var", "continuous variable");
  private final String kind;
  private final String desc;
}
