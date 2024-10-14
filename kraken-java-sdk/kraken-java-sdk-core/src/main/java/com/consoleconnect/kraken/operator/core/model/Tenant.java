package com.consoleconnect.kraken.operator.core.model;

import com.consoleconnect.kraken.operator.core.enums.AssetKindEnum;
import java.util.List;
import lombok.Data;

@Data
public class Tenant {
  private String workspacePath = "classpath:kraken.yaml";

  private ComponentOperation componentOperation = new ComponentOperation();

  @Data
  public static class ComponentOperation {
    private boolean enabled;
    private List<String> updatableAssetKinds =
        List.of(
            AssetKindEnum.COMPONENT_API_TARGET.getKind(),
            AssetKindEnum.COMPONENT_API_TARGET_SPEC.getKind());

    private List<String> creatableAssetKinds =
        List.of(
            AssetKindEnum.COMPONENT_API_TARGET.getKind(),
            AssetKindEnum.COMPONENT_API_TARGET_SPEC.getKind());
  }
}
