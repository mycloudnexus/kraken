package com.consoleconnect.kraken.operator.core.model.facet;

import java.util.List;
import java.util.Map;
import lombok.Data;

@Data
public class CompatibilityFacets {
  // kraken_version,List<mapping_template_version>
  Map<String, List<String>> compatibility;
}
