package com.consoleconnect.kraken.operator.core.entity;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;

@Entity
@Getter
@Setter
@Table(
    name = "kraken_mgmt_event",
    indexes = {
      @Index(name = "kraken_mgmt_event_idx_eventType_status", columnList = "event_type,status"),
      @Index(name = "kraken_mgmt_event_idx_status", columnList = "status")
    })
public class MgmtEventEntity extends AbstractEntity {
  @Column(name = "event_type", nullable = true, unique = false)
  private String eventType;

  @Column(name = "status", nullable = true, unique = false)
  private String status;

  @Column(name = "resource_id", nullable = true, unique = false)
  private String resourceId;

  @Type(JsonType.class)
  @Column(name = "payload", columnDefinition = "jsonb")
  private Object payload;
}
