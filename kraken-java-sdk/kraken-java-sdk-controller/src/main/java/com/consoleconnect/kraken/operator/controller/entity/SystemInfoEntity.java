package com.consoleconnect.kraken.operator.controller.entity;

import com.consoleconnect.kraken.operator.core.entity.AbstractEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "kraken_mgmt_system_info")
public class SystemInfoEntity extends AbstractEntity {

  @Column(name = "control_product_version", nullable = true, unique = false)
  private String controlProductVersion;

  @Column(name = "stage_product_version", nullable = true, unique = false)
  private String stageProductVersion;

  @Column(name = "production_product_version", nullable = true, unique = false)
  private String productionProductVersion;

  @Column(name = "control_app_version", nullable = true, unique = false)
  private String controlAppVersion;

  @Column(name = "stage_app_version", nullable = true, unique = false)
  private String stageAppVersion;

  @Column(name = "production_app_version", nullable = true, unique = false)
  private String productionAppVersion;

  @Column(name = "product_key", nullable = true, unique = false)
  private String productKey;

  @Column(name = "product_spec", nullable = true, unique = false)
  private String productSpec;

  @Column(name = "key", nullable = false, unique = true)
  private String key;

  @Column(name = "description", nullable = true, unique = false)
  private String description;

  @Size(max = 255)
  @Column(name = "status")
  private String status;
}
