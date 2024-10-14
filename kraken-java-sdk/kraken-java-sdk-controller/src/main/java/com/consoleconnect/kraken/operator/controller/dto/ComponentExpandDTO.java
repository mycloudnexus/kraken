package com.consoleconnect.kraken.operator.controller.dto;

import java.util.List;
import lombok.Data;

@Data
public class ComponentExpandDTO {
  private List<TargetMappingDetail> details;
  private String componentName;

  @Data
  public static class TargetMappingDetail {
    private String targetKey;
    private String targetMapperKey;
    private String description;
    private String path;
    private String method;
    private String mappingStatus;
    private String updatedAt;
    private String updatedBy;
    private String lastDeployedAt;
    private String lastDeployedBy;
    private String lastDeployedStatus;
    private boolean requiredMapping = true;
    private boolean diffWithStage = true;
    private String productType;
    private String actionType;
    MappingMatrix mappingMatrix;
    private String orderBy;
  }

  @Data
  public static class MappingMatrix {
    private String productType;
    private String actionType;
    private Boolean provideAlternative;
    private String addressType;
    private String quoteLevel;
    private Boolean syncMode;
  }
}
