package com.consoleconnect.kraken.operator.controller.entity;

import com.consoleconnect.kraken.operator.core.entity.AbstractEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(
    name = "kraken_mgmt_product_env_client",
    uniqueConstraints = {
      @UniqueConstraint(
          name = "kraken_mgmt_product_env_client_uni_idx",
          columnNames = {"env_id", "client_ip", "kind"})
    })
public class EnvironmentClientEntity extends AbstractEntity {
  @Column(name = "env_id", nullable = false, unique = false)
  private String envId;

  @Column(name = "client_ip", nullable = false, unique = false)
  private String clientIp;

  @Column(name = "kind", nullable = false, unique = false)
  private String kind;

  @Size(max = 255)
  @Column(name = "status")
  private String status;

  @Column(name = "reason")
  private String reason;
}
