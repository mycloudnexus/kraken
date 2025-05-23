package com.consoleconnect.kraken.operator.core.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Data;

@Data
public class AppProperty {
  private Tenant tenant = new Tenant();
  private Map<String, Object> env;

  private String modifyUseCase;
  private List<String> filterHeaders;
  private List<String> filterPaths;
  private List<String> deletePaths;
  private List<String> initializeExcludeAssets = new ArrayList<>();
  private List<String> queryExcludeAssetKeys = new ArrayList<>();
  private List<String> noRequiredMappingKeys = new ArrayList<>();
  private List<String> queryExcludeAssetKinds = new ArrayList<>();
  private Map<String, String> apiSpecOrderBy = new HashMap<>();
  private Map<String, String> apiOrderBy = new HashMap<>();
  private Map<String, String> apiTargetMapperOrderBy = new HashMap<>();
  private Features features;
  private WorkflowConfig workflow = new WorkflowConfig();
  private DualVersionConfig dualVersionConfig = new DualVersionConfig();
  private List<String> productTypes = new ArrayList<>();
  private Map<String, List<String>> supportedProductTypes = new HashMap<>();

  @Data
  public static class Features {
    private PushActivityLogExternal pushActivityLogExternal;
    private ExtendCommonConfig extendCommonConfig;
  }

  @Data
  public static class ExtendCommonConfig {
    private boolean enabled;
  }

  @Data
  public static class PushActivityLogExternal {
    private boolean enabled;
  }

  @Data
  public static class WorkflowConfig {
    private String baseUrl = "http://base-url.com";
    private List<String> clusterUrl = new ArrayList<>();
    private String key;
    private String secret;
    private Integer pollTimes = 30;
    private Long pollInterval = 2000l;
    private boolean enabled = false;
    private boolean enableRegisterWorker = false;
    private Integer pollingIntervalMills = 100;
    // required by conductor workflow definition
    private String email = "kraken@example.com";
    private WorkflowDeploymentConfig deployment;
  }

  @Data
  public static class WorkflowDeploymentConfig {
    private boolean enabled = false;
  }

  @Data
  public static class DualVersionConfig {
    boolean enabled = false;
    Map<String, String> tableMaps = new HashMap<>();

    public DualVersionConfig() {
      tableMaps.put("kraken_asset", "kraken_asset_v2");
      tableMaps.put("kraken_asset_link", "kraken_asset_link_v2");
      tableMaps.put("kraken_asset_facet", "kraken_asset_facet_v2");
    }
  }
}
