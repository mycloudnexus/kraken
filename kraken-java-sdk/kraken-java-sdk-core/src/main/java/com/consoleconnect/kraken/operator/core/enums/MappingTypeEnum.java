package com.consoleconnect.kraken.operator.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MappingTypeEnum {
  ENUM("enum", "normal enumeration, can be shown repeated in front side UI"),
  CONSTANT_NUM("constantNumber", "constant number"),
  DISCRETE_STR(
      "discreteStr",
      "discrete string variable, similar with enum but only be shown at once in front side UI"),
  DISCRETE_INT("discreteInt", "discrete integer variable"),
  CONTINUOUS_DOUBLE("continuousDouble", "continuous double variable"),
  CONTINUOUS_INT("continuousInt", "continuous integer variable");
  private final String kind;
  private final String desc;
}
