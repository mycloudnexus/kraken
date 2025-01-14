package com.consoleconnect.kraken.operator.gateway.model;

import java.util.HashMap;
import java.util.Map;
import lombok.Data;

@Data
public class WorkflowPayload {
  private Map<String, HttpPayload> payload = new HashMap<>();
  private Object headers;
  private String id;
  private String requestId;

  @Data
  public static class HttpPayload {
    private String url;
    private String method;
    private Object body;
  }
}
