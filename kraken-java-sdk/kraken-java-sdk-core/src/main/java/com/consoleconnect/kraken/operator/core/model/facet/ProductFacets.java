package com.consoleconnect.kraken.operator.core.model.facet;

import java.util.List;
import lombok.Data;

@Data
public class ProductFacets {
  private List<String> componentPaths;
  private List<String> templateUpgradePaths;
  private List<String> sampleMapperDataPaths;
  private List<String> sampleConfigPaths;
}
