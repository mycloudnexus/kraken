package com.consoleconnect.kraken.operator.core.entity;

import com.consoleconnect.kraken.operator.core.model.SyncMetadata;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import java.time.ZonedDateTime;
import java.util.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;

@Entity
@Getter
@Setter
@Table(
    name = "kraken_asset",
    indexes = {
      @Index(name = "kraken_asset_idx_kind", columnList = "kind"),
      @Index(name = "kraken_asset_idx_kind_key", columnList = "kind,key"),
      @Index(name = "kraken_asset_idx_kind_name", columnList = "kind,name"),
      @Index(name = "kraken_asset_idx_parentId", columnList = "parent_id"),
      @Index(name = "kraken_asset_idx_status", columnList = "status"),
      @Index(name = "kraken_asset_idx_kind_createdAt", columnList = "kind,created_at"),
      @Index(name = "kraken_asset_idx_kind_status", columnList = "kind,status")
    },
    uniqueConstraints = {
      @UniqueConstraint(
          name = "kraken_asset_uni_key",
          columnNames = {"key"})
    })
public class UnifiedAssetEntity extends AbstractEntity {

  @Column(name = "key", nullable = false)
  private String key;

  @Column(name = "parent_id")
  private String parentId;

  @Column(name = "kind")
  private String kind;

  @Column(name = "api_version")
  private String apiVersion;

  @Column(name = "name")
  private String name;

  @Column(name = "version")
  private Integer version;

  @Column(name = "mapper_key")
  private String mapperKey;

  @Column(name = "description", length = 1024)
  private String description;

  @Column(name = "logo")
  private String logo;

  @Column(name = "status")
  private String status;

  @Type(JsonType.class)
  @Column(name = "tags", columnDefinition = "json")
  private Set<String> tags = new HashSet<>();

  @Type(JsonType.class)
  @Column(name = "labels", columnDefinition = "json")
  private Map<String, String> labels = new HashMap<>();

  @Type(JsonType.class)
  @Column(name = "sync_metadata", columnDefinition = "jsonb")
  private SyncMetadata syncMetadata;

  @Column(name = "deleted_at", nullable = true)
  private ZonedDateTime deletedAt;

  @Column(name = "deleted_by", nullable = true)
  private String deletedBy;

  @Type(JsonType.class)
  @Column(name = "metadata", columnDefinition = "jsonb")
  private Map<String, Object> metadata = new HashMap<>();

  @OneToMany(mappedBy = "asset", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  private Set<AssetFacetEntity> facets = new HashSet<>();

  @OneToMany(mappedBy = "asset", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  private Set<AssetLinkEntity> links = new HashSet<>();
}
