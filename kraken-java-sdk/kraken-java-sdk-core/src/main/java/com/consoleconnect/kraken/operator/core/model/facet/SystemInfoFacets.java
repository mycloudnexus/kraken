package com.consoleconnect.kraken.operator.core.model.facet;

import lombok.Data;

@Data
public class SystemInfoFacets {
  private String appVersion;
  private String productVersion;
  private SystemStatus status;

  public enum SystemStatus {
    RUNNING,
    UPGRADING
  }
}
