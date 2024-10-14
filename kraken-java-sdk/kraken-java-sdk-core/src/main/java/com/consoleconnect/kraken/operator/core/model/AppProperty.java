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

  private List<String> filterHeaders;
  private List<String> filterPaths;
  private List<String> initializeExcludeAssets = new ArrayList<>();
  private List<String> queryExcludeAssetKeys = new ArrayList<>();
  private List<String> queryExcludeAssetKinds = new ArrayList<>();
  private Map<String, String> apiSpecOrderBy = new HashMap<>();
  private Map<String, String> apiOrderBy = new HashMap<>();
  private Map<String, String> apiTargetMapperOrderBy = new HashMap<>();
}
