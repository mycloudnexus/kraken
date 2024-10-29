package com.consoleconnect.kraken.operator.controller.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class ApiMapperDeploymentDTO extends ComponentExpandDTO.TargetMappingDetail {
  private String componentName;
  private String componentKey;
  private String envId;
  private String envName;
  private String createAt;
  private String createBy;
  private String userName;
  private String releaseKey;
  private String releaseId;
  private String tagId;
  private String version;
  private String subVersion;
  private String status;
  private String verifiedBy;
  private String verifiedAt;
  private boolean verifiedStatus;
  private boolean productionEnable;
}
