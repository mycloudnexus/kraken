package com.consoleconnect.kraken.operator.core.request;

import java.time.ZonedDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class LogSearchRequest {
  String env;
  String requestId;
  ZonedDateTime queryStart;
  ZonedDateTime queryEnd;
  String productType;
  String method;
  String path;
  Integer statusCode;
  List<String> methods;
  List<Integer> statusCodes;
}
