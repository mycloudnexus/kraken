package com.consoleconnect.kraken.operator.controller.dto.statistics;

import java.time.LocalDate;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ErrorBreakdown {
  private LocalDate date;
  private Map<String, Long> errors;
}
