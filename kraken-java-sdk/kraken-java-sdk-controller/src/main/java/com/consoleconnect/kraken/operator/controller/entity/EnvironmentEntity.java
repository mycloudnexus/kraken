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
    name = "kraken_mgmt_product_env",
    uniqueConstraints = {
      @UniqueConstraint(
          name = "kraken_mgmt_product_env_uni_idx",
          columnNames = {"product_id", "name"})
    })
public class EnvironmentEntity extends AbstractEntity {

  @Column(name = "product_id", nullable = false, unique = false)
  private String productId;

  @Column(name = "name", nullable = false, unique = false)
  private String name;

  @Column(name = "description", nullable = true, unique = false)
  private String description;

  @Size(max = 255)
  @Column(name = "repo")
  private String repo;

  @Size(max = 255)
  @Column(name = "status")
  private String status;
}
