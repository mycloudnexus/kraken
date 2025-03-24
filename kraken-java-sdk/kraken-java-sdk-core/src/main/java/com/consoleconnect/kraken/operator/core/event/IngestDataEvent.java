package com.consoleconnect.kraken.operator.core.event;

import com.consoleconnect.kraken.operator.core.enums.AssetKindEnum;
import com.consoleconnect.kraken.operator.core.model.UnifiedAsset;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class IngestDataEvent {
  private String parentId; // parentId can be parsed from asset
  @NotNull private String fullPath;
  private boolean enforceSync = false;
  private boolean commonExtend = false;
  private AssetKindEnum kind;
  private UnifiedAsset asset;

  private String userId;
  private boolean mergeLabels;

  public IngestDataEvent(String fullPath) {
    this(null, fullPath, false, null, false, null);
  }

  public IngestDataEvent(String parentId, String fullPath) {
    this(parentId, fullPath, false, null, false, null);
  }

  public IngestDataEvent(String parentId, String fullPath, boolean mergeLabels, String userId) {
    this(parentId, fullPath, false, null, mergeLabels, userId);
  }

  public IngestDataEvent(
      String parentId,
      String fullPath,
      boolean enforceSync,
      AssetKindEnum kind,
      boolean mergeLabels,
      String userId) {
    this.parentId = parentId;
    this.fullPath = fullPath;
    this.enforceSync = enforceSync;
    this.kind = kind;
    this.mergeLabels = mergeLabels;
    this.userId = userId;
  }
}
