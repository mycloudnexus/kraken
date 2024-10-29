package com.consoleconnect.kraken.operator.controller.dto;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import lombok.Data;

@Data
public class ApiUseCaseDto {
  private String componentApiKey;
  private String mapperKey;
  private String targetKey;
  private String mappingMatrixKey;

  public List<String> membersExcludeApiKey() {
    return Stream.of(targetKey, mapperKey, mappingMatrixKey).filter(Objects::nonNull).toList();
  }
}
