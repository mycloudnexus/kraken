package com.consoleconnect.kraken.operator.core.client;

import java.time.ZonedDateTime;
import lombok.Data;

@Data
public class ClientInstanceHeartbeat {
  private String instanceId;
  private ZonedDateTime updatedAt;
}
