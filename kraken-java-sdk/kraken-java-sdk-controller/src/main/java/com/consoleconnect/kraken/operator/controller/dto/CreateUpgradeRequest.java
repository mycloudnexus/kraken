package com.consoleconnect.kraken.operator.controller.dto;

import lombok.Data;

@Data
public class CreateUpgradeRequest {
  String templateUpgradeId;
  String stageEnvId;
}
