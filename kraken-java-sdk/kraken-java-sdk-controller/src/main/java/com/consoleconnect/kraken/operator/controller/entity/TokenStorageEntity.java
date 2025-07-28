package com.consoleconnect.kraken.operator.controller.entity;

import com.consoleconnect.kraken.operator.core.entity.AbstractEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(
    name = "kraken_mgmt_token_storage",
    uniqueConstraints = {
      @UniqueConstraint(
          name = "kraken_mgmt_token_storage_uni_idx",
          columnNames = {"asset_id"})
    })
public class TokenStorageEntity extends AbstractEntity {

  @Column(name = "asset_id")
  private String assetId;

  @Column(name = "token", columnDefinition = "TEXT")
  private String token;

  @Column(name = "expired_at")
  private Date expiredAt;
}
