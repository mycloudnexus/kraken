package com.consoleconnect.kraken.operator.core.entity;

import com.consoleconnect.kraken.operator.core.enums.EventStatusType;
import com.consoleconnect.kraken.operator.core.enums.MgmtEventType;
import lombok.Data;

@Data
public class MgmtEventDto {
  MgmtEventType eventType;
  EventStatusType status;
  String resourceId;
  Object payload;
}
