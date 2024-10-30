package com.consoleconnect.kraken.operator.core.model.facet;

import java.util.Map;
import lombok.Data;

@Data
public class ProductReleaseDownloadFacets {
  public static final String KEY = "contentMap";
  Map<String, String> contentMap;
}
