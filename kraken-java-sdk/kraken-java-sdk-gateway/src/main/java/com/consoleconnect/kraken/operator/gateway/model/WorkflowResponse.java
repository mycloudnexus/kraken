package com.consoleconnect.kraken.operator.gateway.model;

import java.util.Map;
import lombok.Data;

@Data
public class WorkflowResponse {
  private Map<String, ItemResponse> result;

  @Data
  public static class ItemResponse {
    private String id;
    private Object response;
  }
}
