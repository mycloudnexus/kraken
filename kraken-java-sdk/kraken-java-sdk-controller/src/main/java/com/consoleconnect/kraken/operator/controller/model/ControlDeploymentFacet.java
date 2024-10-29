package com.consoleconnect.kraken.operator.controller.model;

import com.consoleconnect.kraken.operator.controller.dto.UpgradeTuple;
import lombok.Data;

@Data
public class ControlDeploymentFacet {
  public static final String KEY = "upgradeTuple";
  UpgradeTuple upgradeTuple;
}
