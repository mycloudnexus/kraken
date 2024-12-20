package com.consoleconnect.kraken.operator.core.entity;

import com.consoleconnect.kraken.operator.core.enums.SyncStatusEnum;
import jakarta.persistence.*;
import java.time.ZonedDateTime;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(
    name = "kraken_api_log_activity",
    indexes = {
      @Index(name = "kraken_api_log_activity_idx_createAt", columnList = "created_at"),
      @Index(name = "kraken_api_log_activity_idx_request_id", columnList = "request_id"),
      @Index(name = "kraken_api_log_activity_idx_env", columnList = "env")
    },
    uniqueConstraints = {
      @UniqueConstraint(
          name = "kraken_api_log_activity_uni_idx",
          columnNames = {"request_id", "call_seq"})
    })
public class ApiActivityLogEntity extends AbstractHttpEntity {

  @Column(name = "request_ip", nullable = true, unique = false)
  private String requestIp;

  @Column(name = "response_ip", nullable = true, unique = false)
  private String responseIp;

  @Column(name = "call_seq", nullable = true, unique = false)
  private Integer callSeq;

  @Column(name = "synced_at", nullable = true, unique = false)
  private ZonedDateTime syncedAt;

  @Enumerated(EnumType.STRING)
  @Column(name = "sync_status", nullable = true, unique = false)
  private SyncStatusEnum syncStatus;

  @Column(name = "buyer", nullable = true, unique = false)
  private String buyer;

  @OneToOne(mappedBy = "log")
  private HttpRequestBodyEntity requestBody;
}
