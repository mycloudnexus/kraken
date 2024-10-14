package com.consoleconnect.kraken.operator.core.event;

import lombok.Data;

@Data
public class DeploymentEvent {
  private String envId;
  private String productReleaseId;
  private String productId;
}
