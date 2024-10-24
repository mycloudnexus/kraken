package com.consoleconnect.kraken.operator.sync.model;

import java.util.List;
import lombok.Data;

@Data
public class SyncProperty {
  private ControlPlane controlPlane = new ControlPlane();
  private List<String> acceptAssetKinds = List.of();
  private boolean assetConfigOverwriteFlag = false;

  @Data
  public static class ControlPlane {
    private boolean enabled;
    private String url;
    private String token;
    private String tokenHeader = "Authorization";
    private String retrieveProductReleaseDetailEndpoint =
        "/v2/callback/audits/releases/%s/components";
    protected String defaultProductId = "mef.sonata";

    private String latestDeploymentEndpoint = "/v2/callback/audits/deployments/latest";
    private String apiServerEndpoint = "/v2/callback/audits/api-servers";
    private String syncFromServerEndpoint = "/v2/callback/audits/sync-server-asset";
    private String scanEventEndpoint = "/v2/callback/event";

    private String pushEventEndpoint = "/client/events";
  }
}
