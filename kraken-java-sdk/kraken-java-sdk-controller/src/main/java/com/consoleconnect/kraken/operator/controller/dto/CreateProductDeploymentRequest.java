package com.consoleconnect.kraken.operator.controller.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;
import lombok.Data;

@Data
public class CreateProductDeploymentRequest {
  private String name;
  private String description;

  @NotNull private String envId;
  @NotEmpty private List<String> tagIds;
  private Map<String, String> tagIdMappers;
}
