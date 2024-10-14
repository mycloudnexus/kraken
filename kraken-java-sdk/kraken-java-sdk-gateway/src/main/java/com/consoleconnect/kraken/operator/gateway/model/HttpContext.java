package com.consoleconnect.kraken.operator.gateway.model;

import lombok.Data;

@Data
public class HttpContext {
  private HttpRequestContext request;
  private HttpResponseContext response;
}
