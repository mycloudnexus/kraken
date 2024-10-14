package com.consoleconnect.kraken.operator.controller.dto;

import com.consoleconnect.kraken.operator.core.model.facet.ComponentAPISpecFacets;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;
import lombok.Data;

@Data
public class CreateAPIServerRequest {
  private String key;
  @NotNull private String name;
  private String description;

  private ComponentAPISpecFacets.APISpec baseSpec;

  private List<String> selectedAPIs;
  private Map<String, String> environments;
}
