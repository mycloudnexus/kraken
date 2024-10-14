package com.consoleconnect.kraken.operator.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AssetStatusEnum {
  ACTIVATED("activated"),
  DEACTIVATED("deactivated");
  final String kind;
}
