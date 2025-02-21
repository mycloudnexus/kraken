package com.consoleconnect.kraken.operator.gateway.model;

import java.util.List;
import lombok.Data;

@Data
public class HttpResponseContext {
  private int status;
  private Object body;
  private List<String> deletePaths;
}
