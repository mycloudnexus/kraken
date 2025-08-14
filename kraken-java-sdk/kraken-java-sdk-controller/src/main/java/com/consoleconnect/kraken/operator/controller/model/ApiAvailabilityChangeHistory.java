package com.consoleconnect.kraken.operator.controller.model;

import lombok.Data;

@Data
public class ApiAvailabilityChangeHistory {
  private String mapperKey;
  private String updatedAt;
  private String updatedBy;
  private boolean available;
  private String version;
  private String env;
}
