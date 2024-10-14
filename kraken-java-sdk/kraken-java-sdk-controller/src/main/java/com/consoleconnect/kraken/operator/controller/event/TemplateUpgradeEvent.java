package com.consoleconnect.kraken.operator.controller.event;

import lombok.Data;

@Data
public class TemplateUpgradeEvent {
  private String templateUpgradeId;
  private String envId;
  private String templateDeploymentId;
  private String userId;
}
