package com.consoleconnect.kraken.operator.sync.service.push;

import java.time.ZonedDateTime;
import lombok.Data;

@Data
public class PushLogActivityLogInfo {
  private String user;
  private ZonedDateTime startTime;
  private String envId;
  private String envName;
  private ZonedDateTime endTime;
}
