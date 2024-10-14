package com.consoleconnect.kraken.operator.core.dto;

import lombok.Data;

@Data
public class AssetLinkDto {
  private UnifiedAssetDto targetAsset;
  private String relationship;

  private String id;
  private String createdAt;
  private String createdBy;
  private String updatedAt;
  private String updatedBy;
}
