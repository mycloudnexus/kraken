package com.consoleconnect.kraken.operator.data.entity;

import com.consoleconnect.kraken.operator.core.entity.AbstractEntity;
import jakarta.persistence.*;
import java.time.ZonedDateTime;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(
    name = "kraken_instance_heartbeat_entity",
    indexes = {@Index(name = "kraken_instance_heartbeat_idx_id", columnList = "instance_id")})
public class InstanceHeartbeatEntity extends AbstractEntity {

  @Column(name = "instance_id", nullable = false, unique = true)
  private String instanceId;

  @Column(name = "fqdn", nullable = true)
  private String fqdn;

  @Column(name = "role", nullable = true)
  private String role;

  @Column(name = "app_version", nullable = true)
  private String appVersion;

  @Column(name = "start_up_at", nullable = true, unique = false)
  private ZonedDateTime startUpAt;
}
