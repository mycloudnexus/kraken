package com.consoleconnect.kraken.operator.core.dto;

import java.time.ZonedDateTime;
import java.util.Map;
import lombok.Data;

@Data
public class AbstractHttpModel {
  String env;
  String requestId;
  String uri;
  String path;
  String method;
  Map<String, String> queryParameters;
  Map<String, String> headers;
  Object request;
  Object response;
  ZonedDateTime createdAt;
  ZonedDateTime updatedAt;
  Integer httpStatusCode;
}
