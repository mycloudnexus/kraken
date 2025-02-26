package com.consoleconnect.kraken.operator.core.client;

import lombok.Data;

@Data
public class ClientWorkflowTerminate {
  private String requestId;
  private String workflowInstanceId;
  private String status;
  private String errorMessage;
}
