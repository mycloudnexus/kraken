package com.consoleconnect.kraken.operator.core.request;

import java.time.ZonedDateTime;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class ApiStatisticsSearchRequest {
  String env;
  ZonedDateTime queryStart;
  ZonedDateTime queryEnd;
  String buyerId;
}
