package com.consoleconnect.kraken.operator.controller.dto.start;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeploymentInfoDto {
  private Boolean atLeastOneApiDeployedToStage;
  private Boolean atLeastOneBuyerRegistered;
  private Boolean atLeastOneApiDeployedToProduction;
}
