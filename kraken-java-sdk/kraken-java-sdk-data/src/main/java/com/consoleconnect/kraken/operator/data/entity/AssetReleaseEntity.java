package com.consoleconnect.kraken.operator.data.entity;

import com.consoleconnect.kraken.operator.core.entity.AbstractEntity;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;

@Entity
@Getter
@Setter
@Table(name = "kraken_asset_release")
public class AssetReleaseEntity extends AbstractEntity {

  @Column(name = "product_key", nullable = false)
  private String productKey;

  @Column(name = "product_version", nullable = false)
  private String version;

  @Type(JsonType.class)
  @Column(name = "payload", columnDefinition = "jsonb")
  private Object payload;
}
