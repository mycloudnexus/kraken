package com.consoleconnect.kraken.operator.core.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.web.server.ServerWebExchange;

@Data
@AllArgsConstructor
public class ExceptionEvent {
  private Object exception;
  private Integer code;
  private ServerWebExchange exchange;
}
