package com.consoleconnect.kraken.operator.workflow.model;

import lombok.Data;

@Data
public class WorkflowTerminateEvent {
  private String id;
  private String status;
  private String errorMsg;
}
