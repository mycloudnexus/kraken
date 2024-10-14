package com.consoleconnect.kraken.operator.core.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum RegisterActionTypeEnum {
  REGISTER,
  UNREGISTER,
  READ;

  @JsonCreator
  public static RegisterActionTypeEnum fromString(String text) {
    for (RegisterActionTypeEnum b : RegisterActionTypeEnum.values()) {
      if (b.name().equalsIgnoreCase(text)) {
        return b;
      }
    }
    return null;
  }
}
