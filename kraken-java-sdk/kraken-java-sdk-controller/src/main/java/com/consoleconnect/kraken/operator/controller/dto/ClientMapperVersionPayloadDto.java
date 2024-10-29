package com.consoleconnect.kraken.operator.controller.dto;

import lombok.Data;

@Data
public class ClientMapperVersionPayloadDto extends TagInfoDto {
  String mapperKey;
  String version;
  String subVersion;
  String deploymentId;
  String createdBy;
  String createdAt;
}
