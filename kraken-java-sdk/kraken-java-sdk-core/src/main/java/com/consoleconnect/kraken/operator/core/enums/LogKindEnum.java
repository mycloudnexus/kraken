package com.consoleconnect.kraken.operator.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum LogKindEnum {
  DATA_PLANE("data-plane"),
  CONTROL_PLANE("control-plane"),
  MANAGE_PLANE("manage-plane");

  private final String kind;
}
