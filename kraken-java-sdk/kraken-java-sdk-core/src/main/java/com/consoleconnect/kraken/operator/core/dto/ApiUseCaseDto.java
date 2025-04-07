package com.consoleconnect.kraken.operator.core.dto;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import lombok.Data;

@Data
public class ApiUseCaseDto {
  private String componentApiKey;
  private String mapperKey;
  private String workflowKey;
  private String targetKey;
  private String mappingMatrixKey;

  public List<String> membersExcludeApiKey() {
    return Stream.of(targetKey, mapperKey, workflowKey, mappingMatrixKey)
        .filter(Objects::nonNull)
        .toList();
  }

  public List<String> membersDeployable(boolean workflowEnabled) {
    return workflowEnabled
        ? Stream.of(targetKey, mapperKey, workflowKey, mappingMatrixKey)
            .filter(Objects::nonNull)
            .toList()
        : Stream.of(targetKey, mapperKey, mappingMatrixKey).filter(Objects::nonNull).toList();
  }
}
