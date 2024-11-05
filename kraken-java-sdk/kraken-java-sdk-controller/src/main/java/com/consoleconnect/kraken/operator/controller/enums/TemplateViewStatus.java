package com.consoleconnect.kraken.operator.controller.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TemplateViewStatus {
  UPGRADED("Upgraded"),
  NOT_UPGRADED("Not upgraded"),
  UPGRADING("Upgrading"),
  DEPRECATED("Deprecated");
  private final String status;
}
