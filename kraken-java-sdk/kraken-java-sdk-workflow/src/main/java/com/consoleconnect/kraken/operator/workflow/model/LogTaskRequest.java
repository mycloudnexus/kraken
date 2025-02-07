package com.consoleconnect.kraken.operator.workflow.model;

import lombok.Data;

@Data
public class LogTaskRequest {
  private Object requestPayload;
  private Object responsePayload;
  private String requestId;
}
