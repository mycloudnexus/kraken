package com.consoleconnect.kraken.operator.controller.dto.push;

import java.time.ZonedDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreatePushApiActivityRequest {
  private ZonedDateTime startTime;
  private ZonedDateTime endTime;
  private String envId;
}
