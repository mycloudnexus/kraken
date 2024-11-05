package com.consoleconnect.kraken.operator.controller.model;

import com.consoleconnect.kraken.operator.core.enums.AssetKindEnum;
import java.util.List;
import lombok.Data;

@Data
public class MgmtProperty {
  public static final String DEFAULT_TOKEN_EXPIRED_SECONDS = "3153600000";
  private boolean enabled;
  private String defaultEnv = "stage";
  private boolean mgmtServerEnabled = false;
  private String productName = "";

  private APIToken apiToken = new APIToken();
  private String buyerTokenExpiredSeconds = DEFAULT_TOKEN_EXPIRED_SECONDS;
  private ClientConnection clientConnection = new ClientConnection();
  private TemplateUpgradeProperty templateUpgrade = new TemplateUpgradeProperty();

  @Data
  public static class ClientConnection {
    private long heartbeatInterval = 20; // seconds
    private String secretKey;
  }

  @Data
  public static class TemplateUpgradeProperty {
    List<String> distributeKinds =
        List.of(
            AssetKindEnum.COMPONENT_API.getKind(),
            AssetKindEnum.COMPONENT_API_TARGET_MAPPER.getKind(),
            AssetKindEnum.COMPONENT_API_TARGET.getKind(),
            AssetKindEnum.COMPONENT_API_SPEC.getKind(),
            AssetKindEnum.PRODUCT_MAPPING_MATRIX.getKind());
    List<String> mergeLabelKinds = List.of(AssetKindEnum.COMPONENT_API_TARGET_MAPPER.getKind());
  }
}
