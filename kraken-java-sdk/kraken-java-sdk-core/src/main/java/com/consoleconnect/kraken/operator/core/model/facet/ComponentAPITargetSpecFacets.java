package com.consoleconnect.kraken.operator.core.model.facet;

import lombok.Data;

@Data
public class ComponentAPITargetSpecFacets {
  private ComponentAPISpecFacets.APISpec spec;
  private ComponentAPISpecFacets.APISpec doc;
}
