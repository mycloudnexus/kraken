package com.consoleconnect.kraken.operator.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MappingStatusEnum {
  INCOMPLETE("incomplete"),
  COMPLETE("complete");

  final String desc;
}
