package com.consoleconnect.kraken.operator.controller.dto.statistics;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EndpointUsage {
  private String method;
  private String endpoint;
  private Long usage;
  private double popularity;
}
