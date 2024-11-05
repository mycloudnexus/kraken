package com.consoleconnect.kraken.operator.controller.dto.statistics;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RequestStatistics {
  private LocalDate date;
  private Long success;
  private Long error;
}
