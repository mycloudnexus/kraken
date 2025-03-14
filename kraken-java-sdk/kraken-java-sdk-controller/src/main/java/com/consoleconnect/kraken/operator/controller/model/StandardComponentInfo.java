package com.consoleconnect.kraken.operator.controller.model;

import com.consoleconnect.kraken.operator.core.model.facet.ComponentAPISpecFacets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Data;

@Data
public class StandardComponentInfo {
  private String name;
  private String componentKey;
  private List<String> supportedProductTypes = new ArrayList<>();
  private Map<String, String> labels = new HashMap<>();
  private String logo;
  private ComponentAPISpecFacets.APISpec baseSpec;
  private ComponentAPISpecFacets.APISpec customizedSpec;
  private Integer apiCount;
}
