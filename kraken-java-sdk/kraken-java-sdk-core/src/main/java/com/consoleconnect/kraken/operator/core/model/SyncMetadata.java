package com.consoleconnect.kraken.operator.core.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SyncMetadata {
  private String fullPath;
  private String syncedSha;
  private String syncedAt;
  private String syncedBy;
  private boolean extendCommon;

  public SyncMetadata(String fullPath, String syncedSha, String syncedAt) {
    this.fullPath = fullPath;
    this.syncedSha = syncedSha;
    this.syncedAt = syncedAt;
  }

  public SyncMetadata(String fullPath, String syncedSha, String syncedAt, String syncedBy) {
    this.fullPath = fullPath;
    this.syncedSha = syncedSha;
    this.syncedAt = syncedAt;
    this.syncedBy = syncedBy;
  }

  public SyncMetadata(
      String fullPath, String syncedSha, String syncedAt, String syncedBy, boolean extendCommon) {
    this.fullPath = fullPath;
    this.syncedSha = syncedSha;
    this.syncedAt = syncedAt;
    this.syncedBy = syncedBy;
    this.extendCommon = extendCommon;
  }
}
