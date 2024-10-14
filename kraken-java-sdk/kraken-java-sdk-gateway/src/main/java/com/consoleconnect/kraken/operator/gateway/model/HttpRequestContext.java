package com.consoleconnect.kraken.operator.gateway.model;

import java.util.Map;
import lombok.Data;

@Data
public class HttpRequestContext {
  private String method;
  private String path;
  private String uri;
  private Object body;
  private Map<String, String> headers;
  private Map<String, String> queryParams;
}
