package com.consoleconnect.kraken.operator.controller.dto.push;

import java.time.ZonedDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PushApiActivityLogHistory {
  private UUID id;
  private ZonedDateTime createdAt;
  private String envName;
  private ZonedDateTime startTime;
  private ZonedDateTime endTime;
  private String pushedBy;
  private String status;
}
