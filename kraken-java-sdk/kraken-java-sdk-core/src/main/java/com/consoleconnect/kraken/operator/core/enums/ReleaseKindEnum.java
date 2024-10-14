package com.consoleconnect.kraken.operator.core.enums;

import lombok.Getter;

@Getter
public enum ReleaseKindEnum {
  COMPONENT_LEVEL("component"),
  API_LEVEL("api"),
  SYSTEM_TEMPLATE_MIXED("system-template-mixed");
  private final String kind;

  ReleaseKindEnum(String kind) {
    this.kind = kind;
  }
}
