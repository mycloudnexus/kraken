package com.consoleconnect.kraken.operator.core.model;

import java.util.Map;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiActivityResponseLog {
  private String env;

  private String requestId;

  private int callSeq;

  private String uri;

  private String path;

  private String method;

  private Integer httpStatusCode;

  private Map<String, String> queryParameters;

  private Map<String, String> headers;

  protected String response;
}
