package com.consoleconnect.kraken.operator.core.model;

import java.util.Map;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiActivityRequestLog {
  private String env;

  private String responseIp;

  private String requestId;

  private String uri;

  private String path;

  private String method;

  private Integer httpStatusCode;

  private Map<String, String> queryParameters;

  private Map<String, String> headers;

  protected Object request;

  protected Object response;
}
