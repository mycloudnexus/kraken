package com.consoleconnect.kraken.operator.core.model.facet;

import lombok.Data;

@Data
public class ComponentTransformerFacets {

  public static final String FACET_SCRIPT_KEY = "script";

  private Script script;

  @Data
  public static class Script {
    private String engine;
    private String path;
    private String code;
  }
}
