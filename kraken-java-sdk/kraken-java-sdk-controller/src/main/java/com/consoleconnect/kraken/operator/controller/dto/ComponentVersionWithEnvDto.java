package com.consoleconnect.kraken.operator.controller.dto;

import java.time.ZonedDateTime;
import lombok.Data;

@Data
public class ComponentVersionWithEnvDto {
  String id;
  String name;
  String version;
  String key;
  ZonedDateTime createdAt;
  EnvironmentDto env;
}
