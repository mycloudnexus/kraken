package com.consoleconnect.kraken.operator.sync.service.push;

import com.consoleconnect.kraken.operator.core.dto.ComposedHttpRequest;
import com.consoleconnect.kraken.operator.core.toolkit.Paging;
import java.time.ZonedDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PushExternalSystemPayload {
  private UUID id;
  private ZonedDateTime startTime;
  private ZonedDateTime endTime;
  private String envName;
  private Paging<ComposedHttpRequest> data;
}
