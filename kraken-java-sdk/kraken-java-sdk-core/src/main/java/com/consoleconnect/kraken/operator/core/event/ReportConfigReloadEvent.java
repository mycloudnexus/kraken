package com.consoleconnect.kraken.operator.core.event;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class ReportConfigReloadEvent {
  private String productReleaseId;
  private String status;
  private String reason;
}
