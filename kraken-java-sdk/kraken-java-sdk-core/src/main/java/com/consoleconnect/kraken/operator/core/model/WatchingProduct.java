package com.consoleconnect.kraken.operator.core.model;

import java.util.List;
import lombok.Data;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
public class WatchingProduct {
  private String productId;
  private String fullPath;

  private List<String> assetFullPaths;
}
