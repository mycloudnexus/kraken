package com.consoleconnect.kraken.operator.workflow.model;

import java.util.Map;
import lombok.Data;

@Data
public class EvaluateResult {
  private String url;
  private Map<String, Object> body;
  private String singleResult;
}
