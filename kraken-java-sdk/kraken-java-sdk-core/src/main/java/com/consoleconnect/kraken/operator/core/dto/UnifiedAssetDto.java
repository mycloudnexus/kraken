package com.consoleconnect.kraken.operator.core.dto;

import com.consoleconnect.kraken.operator.core.model.SyncMetadata;
import com.consoleconnect.kraken.operator.core.model.UnifiedAsset;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class UnifiedAssetDto extends UnifiedAsset {
  private String id;
  private String parentId;
  private String createdAt;
  private String createdBy;
  private String updatedAt;
  private String updatedBy;
  private SyncMetadata syncMetadata;
  private String mappingStatus;
}
