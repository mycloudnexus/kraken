package com.consoleconnect.kraken.operator.core.client;

import com.consoleconnect.kraken.operator.core.dto.DeployComponentError;
import java.util.List;
import lombok.Data;

@Data
public class ClientInstanceDeployment {
  String instanceId;
  String status;
  private List<DeployComponentError> errors;
  String productReleaseId;
}
