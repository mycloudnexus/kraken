package com.consoleconnect.kraken.operator.core.client;

import lombok.Data;

@Data
public class ServerAPIDto {
  private String mapperKey;
  private String serverKey;
  private String method;
  private String path;
}
