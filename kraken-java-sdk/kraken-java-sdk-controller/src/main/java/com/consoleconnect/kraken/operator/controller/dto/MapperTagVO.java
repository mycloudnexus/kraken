package com.consoleconnect.kraken.operator.controller.dto;

import lombok.Data;

@Data
public class MapperTagVO extends TagInfoDto {
  private String version;
  private String tagId;
  private String mapperKey;
  private String componentKey;
  private ComponentExpandDTO.MappingMatrix mappingMatrix;
  private String status;
}
