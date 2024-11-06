package com.consoleconnect.kraken.operator.controller.dto.start;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StartGuideInfoDto {
  private SellerApiServerRegistrationInfoDto sellerApiServerRegistrationInfo;
  private ApiMappingInfoDto apiMappingInfo;
  private DeploymentInfoDto deploymentInfo;
}
