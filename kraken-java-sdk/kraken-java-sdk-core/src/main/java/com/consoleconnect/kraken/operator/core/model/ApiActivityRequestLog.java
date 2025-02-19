package com.consoleconnect.kraken.operator.core.model;

import java.util.Map;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiActivityRequestLog {

  private String requestId;

  private Integer callSeq;

  private String uri;

  private String path;

  private String method;

  private Map<String, String> queryParameters;

  private Map<String, String> headers;

  private String requestIp;

  private String responseIp;

  private String activityRequestLogId;

  private Object request;
}
