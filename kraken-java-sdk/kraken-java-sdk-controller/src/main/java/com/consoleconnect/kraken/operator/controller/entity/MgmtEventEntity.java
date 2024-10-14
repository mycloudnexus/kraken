package com.consoleconnect.kraken.operator.controller.entity;

import com.consoleconnect.kraken.operator.core.entity.AbstractEntity;
import com.consoleconnect.kraken.operator.core.enums.EventStatusType;
import com.consoleconnect.kraken.operator.core.enums.MgmtEventType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "kraken_mgmt_event")
public class MgmtEventEntity extends AbstractEntity {
  @Enumerated(EnumType.STRING)
  private MgmtEventType eventType;

  private EventStatusType status;
  private String resourceId;
}
