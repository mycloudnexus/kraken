package com.consoleconnect.kraken.operator.controller.dto;

import lombok.Data;

@Data
public class TemplateUpgradeDeploymentVO {
  private String deploymentId;
  private String templateUpgradeId;
  private String templateUpgradeDeploymentId;
  private String envName;
  private String productVersion;
  private String productSpec;
  private String publishDate;
  private String upgradeBy;
  private String envId;
  private String status;
  private String createdAt;
  private String updatedAt;
}
