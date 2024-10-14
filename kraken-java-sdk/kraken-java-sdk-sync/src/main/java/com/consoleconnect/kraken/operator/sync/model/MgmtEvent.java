package com.consoleconnect.kraken.operator.sync.model;

import com.consoleconnect.kraken.operator.core.enums.EventStatusType;
import com.consoleconnect.kraken.operator.core.enums.MgmtEventType;
import java.time.ZonedDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MgmtEvent {
  private String id;
  private EventStatusType status;
  private MgmtEventType eventType;
  private String resourceId;
  private ZonedDateTime createdAt;
}
