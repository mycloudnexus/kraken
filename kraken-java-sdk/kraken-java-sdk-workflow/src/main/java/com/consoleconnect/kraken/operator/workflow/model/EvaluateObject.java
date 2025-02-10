package com.consoleconnect.kraken.operator.workflow.model;

import java.util.Map;
import lombok.Data;

@Data
public class EvaluateObject {
  private Map<String, Map<String, String>> value;
  private String bodyExpression;
  private String urlExpression;
}
