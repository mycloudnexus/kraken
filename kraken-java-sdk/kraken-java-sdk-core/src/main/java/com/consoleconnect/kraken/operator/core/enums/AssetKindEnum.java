package com.consoleconnect.kraken.operator.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AssetKindEnum {
  WORKSPACE("kraken.workspace"),
  PRODUCT("kraken.product"),
  PRODUCT_DEPLOYMENT("kraken.product-deployment"),
  PRODUCT_TEMPLATE_DEPLOYMENT("kraken.product.template-deployment"),
  PRODUCT_TEMPLATE_CONTROL_DEPLOYMENT("kraken.product.template-control-deployment"),
  PRODUCT_TEMPLATE_UPGRADE("kraken.product.template-upgrade"),
  PRODUCT_ENV("kraken.product-env"),
  PRODUCT_BUYER("kraken.product-buyer"),
  COMPONENT_API("kraken.component.api"),
  COMPONENT_API_TAG("kraken.component.api-tag"),
  COMPONENT_API_TARGET_MAPPER_TAG("kraken.component.api-target-mapper-tag"),
  COMPONENT_API_SYSTEM_TEMPLATE_TAG("kraken.component.api-system-template-tag"),
  COMPONENT_API_SPEC("kraken.component.api-spec"),
  COMPONENT_API_TARGET("kraken.component.api-target"),
  COMPONENT_API_TARGET_SPEC("kraken.component.api-target-spec"),
  COMPONENT_API_TARGET_MAPPER("kraken.component.api-target-mapper"),
  COMPONENT_TRANSFORMER("kraken.component.transformer"),
  COMPONENT_WORKFLOW("kraken.component.workflow"),
  PRODUCT_MAPPING_MATRIX("kraken.product.mapping.matrix"),
  PRODUCT_RELEASE_DOWNLOAD("kraken.product.release.download"),
  COMPONENT_API_SERVER("kraken.component.api-server"),
  SYSTEM_INFO("kraken.product.system"),
  PRODUCT_COMPATIBILITY("kraken.product.compatibility");

  private final String kind;
}
