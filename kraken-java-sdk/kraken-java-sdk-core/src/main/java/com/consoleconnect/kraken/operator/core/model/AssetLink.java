package com.consoleconnect.kraken.operator.core.model;

import lombok.Data;

@Data
public class AssetLink {
  private String targetAssetKey;
  private String relationship;
}
