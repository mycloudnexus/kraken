package com.consoleconnect.kraken.operator.core.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;

@Entity
@Getter
@Setter
@Table(
    name = "kraken_asset_facet",
    indexes = {
      @Index(name = "kraken_asset_facet_idx_key", columnList = "key"),
      @Index(name = "kraken_asset_facet_idx_assetId", columnList = "asset_id")
    })
public class AssetFacetEntity extends AbstractEntity {

  @ManyToOne
  @JoinColumn(name = "asset_id", nullable = false)
  @JsonIgnore
  private UnifiedAssetEntity asset;

  @Column(name = "key", nullable = false)
  private String key;

  @Type(JsonType.class)
  @Column(name = "payload", columnDefinition = "jsonb")
  private Object payload;
}
