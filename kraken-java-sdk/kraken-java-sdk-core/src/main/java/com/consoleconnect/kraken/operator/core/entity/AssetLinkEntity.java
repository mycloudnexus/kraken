package com.consoleconnect.kraken.operator.core.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import java.util.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;

@Entity
@Getter
@Setter
@Table(
    name = "kraken_asset_link",
    indexes = {
      @Index(name = "kraken_asset_link_idx_assetId", columnList = "asset_id"),
      @Index(name = "kraken_asset_link_idx_targetAssetKey", columnList = "target_asset_key"),
      @Index(
          name = "kraken_asset_link_idx_assetId_targetAssetKey",
          columnList = "asset_id, target_asset_key")
    })
public class AssetLinkEntity extends AbstractEntity {

  @ManyToOne
  @JoinColumn(name = "asset_id", nullable = false)
  @JsonIgnore
  private UnifiedAssetEntity asset;

  @Column(name = "target_asset_key", nullable = false)
  private String targetAssetKey;

  @Column(name = "relationship", nullable = false)
  private String relationship;

  @Column(name = "component_group")
  private String group;

  @Type(JsonType.class)
  @Column(name = "metadata", columnDefinition = "jsonb")
  private Map<String, Object> metadata = new HashMap<>();
}
