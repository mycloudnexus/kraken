package com.consoleconnect.kraken.operator.core.event;

import lombok.Data;

@Data
public class HeartBeatUploadEvent {
  private String fqdn;
  private String ipAddress;
  private String role;
  private String planeType;
  private String envId;
  private String envName;
  private String appVersion;
}
