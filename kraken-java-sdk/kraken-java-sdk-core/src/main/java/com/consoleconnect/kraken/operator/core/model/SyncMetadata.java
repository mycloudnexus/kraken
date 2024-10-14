package com.consoleconnect.kraken.operator.core.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SyncMetadata {
  private String fullPath;
  private String syncedSha;
  private String syncedAt;
  private String syncedBy;

  public SyncMetadata(String fullPath, String syncedSha, String syncedAt) {
    this.fullPath = fullPath;
    this.syncedSha = syncedSha;
    this.syncedAt = syncedAt;
  }
}
