package com.consoleconnect.kraken.operator.controller.dto;

import lombok.Data;

@Data
public class LatestDeploymentDTO {
  private String envId;
  private String envName;
  private String mapperKey;
  private String runningVersion;
  private String status;
  private String createAt;
  private String createBy;
}
