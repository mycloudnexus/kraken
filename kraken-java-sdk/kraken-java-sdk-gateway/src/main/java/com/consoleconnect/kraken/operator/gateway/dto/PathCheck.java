package com.consoleconnect.kraken.operator.gateway.dto;

import com.consoleconnect.kraken.operator.core.enums.ExpectTypeEnum;

public record PathCheck(
    String name,
    String path,
    ExpectTypeEnum expectType,
    String value,
    String errorMsg,
    Integer code,
    String expectedValueType) {

  public PathCheck withUpdatedPath(String updatedPath) {
    return new PathCheck(name, updatedPath, expectType, value, errorMsg, code, expectedValueType);
  }
}
