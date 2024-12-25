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
    name = "kraken_api_log_body_activity",
    indexes = {@Index(name = "kraken_api_log_activity_idx_createAt", columnList = "created_at")})
public class ApiActivityLogBodyEntity extends AbstractEntity {

  @OneToOne
  @JoinColumn(name = "api_log_id")
  private ApiActivityLogEntity apiActivityLog;

  @Column(name = "request", nullable = true, unique = false, columnDefinition = "jsonb")
  @Type(JsonType.class)
  private Object request;

  @Column(name = "response", nullable = true, unique = false, columnDefinition = "jsonb")
  @Type(JsonType.class)
  private Object response;
}
