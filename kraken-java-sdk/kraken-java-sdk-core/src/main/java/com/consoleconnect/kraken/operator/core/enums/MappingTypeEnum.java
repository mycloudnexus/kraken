package com.consoleconnect.kraken.operator.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MappingTypeEnum {
  ENUM("enum", true,"normal enumeration, can be shown repeated in front side UI"),
  CONSTANT_NUM("number", true, "constant number"),
  DISCRETE_STR(
      "string",
      true, "discrete string variable, similar with enum but only be shown at once in front side UI"),
  DISCRETE_INT(Constants.INT_VAL, true, "discrete integer variable"),
  CONTINUOUS_DOUBLE("double", false, "continuous double variable"),
  CONTINUOUS_INT(Constants.INT_VAL, false, "continuous integer variable");
  private final String kind;
  private final Boolean discrete;
  private final String desc;

  private static class Constants {
    public static final String INT_VAL = "integer";
  }
}
