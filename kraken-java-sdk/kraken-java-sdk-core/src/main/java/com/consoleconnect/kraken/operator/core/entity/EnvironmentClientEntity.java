package com.consoleconnect.kraken.operator.core.entity;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;

@Entity
@Getter
@Setter
@Table(
    name = "kraken_mgmt_product_env_client",
    indexes = {
      @Index(
          name = "kraken_mgmt_product_env_client_env_id_client_key_kind_idx",
          columnList = "env_id,client_key,kind")
    })
public class EnvironmentClientEntity extends AbstractEntity {
  @Column(name = "env_id", nullable = false, unique = false)
  private String envId;

  @Column(name = "client_key", nullable = false, unique = false)
  private String clientKey;

  @Column(name = "kind", nullable = false, unique = false)
  private String kind;

  @Size(max = 255)
  @Column(name = "status")
  private String status;

  @Column(name = "reason")
  private String reason;

  @Column(name = "env_name")
  private String envName;

  @Column(name = "fqdn")
  private String fqdn;

  @Column(name = "role")
  private String role;

  @Column(name = "app_version", nullable = true)
  private String appVersion;

  @Type(JsonType.class)
  @Column(name = "payload", columnDefinition = "jsonb")
  private Object payload;
}
