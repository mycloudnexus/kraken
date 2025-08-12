package com.consoleconnect.kraken.operator.controller.model;

import lombok.Data;

@Data
public class UpdateAipAvailabilityRequest {
  private String mapperKey;
  private boolean disabled;
  private String envName;
  private String version;
}
