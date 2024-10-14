package com.consoleconnect.kraken.operator.controller.dto;

import java.util.List;
import lombok.Data;

@Data
public class TemplateUpgradeReleaseVO {
  private String templateUpgradeId;
  private String name;
  private String releaseVersion;
  private String releaseDate;
  private String description;
  private List<TemplateUpgradeDeploymentVO> deployments;
  private boolean showUpgradeButton = false;
}
