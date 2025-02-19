package com.consoleconnect.kraken.operator.core.model;

import com.consoleconnect.kraken.operator.core.entity.ApiActivityLogEntity;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiActivityResponseLog {

  private ApiActivityLogEntity apiActivityLog;

  private Integer httpStatusCode;

  private String responseIp;

  protected Object response;
}
