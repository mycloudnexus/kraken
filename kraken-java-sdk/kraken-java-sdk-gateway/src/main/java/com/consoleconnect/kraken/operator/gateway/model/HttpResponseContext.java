package com.consoleconnect.kraken.operator.gateway.model;

import lombok.Data;

@Data
public class HttpResponseContext {
  private int status;
  private Object body;
}
