package com.consoleconnect.kraken.operator.core.model;

import lombok.Data;

@Data
public class FilterRule {
  private String queryPath;
  private String filterKey;
  private String filterVal;
  private String filterPath;
}
