package com.consoleconnect.kraken.operator.controller.dto;

import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Data;

@Data
public class CreateAPIMapperDeploymentRequest {

  @NotNull private String componentId;
  List<String> mapperKeys;
  @NotNull private String envId;
}
