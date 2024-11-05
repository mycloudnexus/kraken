package com.consoleconnect.kraken.operator.controller.dto;

import java.util.List;
import lombok.Data;

@Data
public class TemplateUpgradeReleaseVO {
  private String templateUpgradeId;
  private String name;
  private String productVersion;
  private String productSpec;
  private String publishDate;
  private String description;
  private List<TemplateUpgradeDeploymentVO> deployments;
  private boolean showStageUpgradeButton = false;
  private boolean showProductionUpgradeButton = false;
  private String status;
}
