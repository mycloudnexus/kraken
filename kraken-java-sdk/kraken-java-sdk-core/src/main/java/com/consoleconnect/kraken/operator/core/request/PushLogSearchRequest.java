package com.consoleconnect.kraken.operator.core.request;

import java.time.ZonedDateTime;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class PushLogSearchRequest {
  ZonedDateTime queryStart;
  ZonedDateTime queryEnd;
}