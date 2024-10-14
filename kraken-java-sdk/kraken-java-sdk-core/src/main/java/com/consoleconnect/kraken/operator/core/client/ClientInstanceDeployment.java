package com.consoleconnect.kraken.operator.core.client;

import lombok.Data;

@Data
public class ClientInstanceDeployment {
  String instanceId;
  String status;
  String reason;
  String productReleaseId;
}
