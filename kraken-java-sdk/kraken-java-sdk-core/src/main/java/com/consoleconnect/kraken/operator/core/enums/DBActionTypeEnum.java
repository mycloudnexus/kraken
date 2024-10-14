package com.consoleconnect.kraken.operator.core.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum DBActionTypeEnum {
  CREATE,
  UPDATE,
  READ;

  @JsonCreator
  public static DBActionTypeEnum fromString(String text) {
    for (DBActionTypeEnum b : DBActionTypeEnum.values()) {
      if (b.name().equalsIgnoreCase(text)) {
        return b;
      }
    }
    return null;
  }
}
