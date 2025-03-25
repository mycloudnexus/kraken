package com.consoleconnect.kraken.operator.core.dto;

import java.util.List;
import lombok.Data;

@Data
public class SourceCheckItem {
  private String sourceType;
  private Boolean discrete;
  private Object evaluateValue;
  private String paramName;
  private List<String> valueList;
  private String target;
  private Boolean allowValueLimit;
  private Boolean systemValueLimit;
}
