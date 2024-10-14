package com.consoleconnect.kraken.operator.controller.dto;

import lombok.Data;

@Data
public class CreateProductionUpgradeRequest extends CreateUpgradeRequest {
  String productEnvId;
}
