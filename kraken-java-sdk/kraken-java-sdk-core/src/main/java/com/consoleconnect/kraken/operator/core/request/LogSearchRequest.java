package com.consoleconnect.kraken.operator.core.request;

import java.time.ZonedDateTime;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class LogSearchRequest {
  String env;
  String requestId;
  ZonedDateTime queryStart;
  ZonedDateTime queryEnd;
  String method;
  String path;
  Integer statusCode;
}
