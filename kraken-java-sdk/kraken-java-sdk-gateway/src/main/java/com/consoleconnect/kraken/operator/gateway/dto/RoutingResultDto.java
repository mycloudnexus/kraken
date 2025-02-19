package com.consoleconnect.kraken.operator.gateway.dto;

import lombok.Data;

@Data
public class RoutingResultDto {
  private String productType;
  private String productAction;
  private String targetAPIConfigKey;
  private String matrixConfigKey;
  private String errorCode;
  private String errorMsg;
}
