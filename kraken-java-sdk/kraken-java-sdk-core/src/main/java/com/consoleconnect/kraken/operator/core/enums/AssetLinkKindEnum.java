package com.consoleconnect.kraken.operator.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AssetLinkKindEnum {
  COMPONENT_API("kraken.component.api"),
  IMPLEMENTATION_WORKFLOW("implementation.workflow"),
  IMPLEMENTATION_TARGET("implementation.target"),
  IMPLEMENTATION_TARGET_MAPPER("implementation.target-mapper"),
  IMPLEMENTATION_MAPPING_MATRIX("implementation.matrix"),
  IMPLEMENTATION_STANDARD_API_SPEC("implementation.standard.api-spec"),
  DEPLOYMENT_API_TAG("deployment.component.api-tag"),
  DEPLOYMENT_COMPONENT_API("deployment.component.api"),
  ;
  private final String kind;
}
