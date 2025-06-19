package com.consoleconnect.kraken.operator.core.enums;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import lombok.Getter;

@Getter
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
  COMPONENT_API_WORK_FLOW("kraken.component.api-workflow"),
  COMPONENT_API_WORK_FLOW_DEPLOYMENT("kraken.component.api-workflow-deployment"),
  COMPONENT_TRANSFORMER("kraken.component.transformer"),
  PRODUCT_MAPPING_MATRIX("kraken.product.mapping.matrix"),
  PRODUCT_RELEASE_DOWNLOAD("kraken.product.release.download"),
  COMPONENT_API_SERVER("kraken.component.api-server"),
  COMPONENT_SELLER_CONTACT("kraken.component.seller-contact"),
  PRODUCT_APP_KRAKEN("kraken.product.app.kraken"),
  PRODUCT_COMPATIBILITY("kraken.product.compatibility"),
  COMPONENT_API_AVAILABILITY("kraken.component.api-availability");

  AssetKindEnum(String kind) {
    this.kind = kind;
    AssetKindEnum.Holder.holderMap.put(kind, this);
  }

  private final String kind;

  private static class Holder {
    private static final Map<String, AssetKindEnum> holderMap = new HashMap<>();
  }

  public static AssetKindEnum kindOf(String kind) {
    return Objects.isNull(kind) ? null : Holder.holderMap.getOrDefault(kind, null);
  }
}
