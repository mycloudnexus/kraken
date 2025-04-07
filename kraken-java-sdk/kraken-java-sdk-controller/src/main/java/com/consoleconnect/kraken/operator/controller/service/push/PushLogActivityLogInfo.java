package com.consoleconnect.kraken.operator.controller.service.push;

import lombok.Data;

@Data
public class PushLogActivityLogInfo {
  private String user;
  private String startTime;
  private String envId;
  private String envName;
  private String endTime;
}
