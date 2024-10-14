package com.consoleconnect.kraken.operator.controller.dto;

import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Data;

@Data
public class DeployToProductionRequest {

  private List<TagInfoDto> tagInfos;
  @NotNull private String sourceEnvId;
  @NotNull private String targetEnvId;
}
