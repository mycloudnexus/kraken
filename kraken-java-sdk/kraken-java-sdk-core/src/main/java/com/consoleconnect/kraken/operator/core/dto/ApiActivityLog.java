package com.consoleconnect.kraken.operator.core.dto;

import com.consoleconnect.kraken.operator.core.enums.SyncStatusEnum;
import java.time.ZonedDateTime;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class ApiActivityLog extends AbstractHttpModel {

  private String requestIp;
  private String responseIp;
  private Integer callSeq;

  private ZonedDateTime triggeredAt;

  private ZonedDateTime syncedAt;
  private SyncStatusEnum syncStatus;

  private String clientId;
  private String buyer;
  private String buyerId;
  private String buyerName;
}
