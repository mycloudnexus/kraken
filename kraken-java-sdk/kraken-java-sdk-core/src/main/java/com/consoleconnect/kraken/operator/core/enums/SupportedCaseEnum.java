package com.consoleconnect.kraken.operator.core.enums;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum SupportedCaseEnum {
  ONE_TO_ONE("1:1"),
  ONE_TO_MANY("1:m"),
  ONE_TO_ONE_AND_ONE_TO_MANY("1:1 and 1:m");
  private String description;
}
