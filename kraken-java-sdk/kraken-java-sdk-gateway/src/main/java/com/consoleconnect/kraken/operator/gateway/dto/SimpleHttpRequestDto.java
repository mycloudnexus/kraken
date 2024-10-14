package com.consoleconnect.kraken.operator.gateway.dto;

import java.time.ZonedDateTime;
import lombok.Data;

@Data
public class SimpleHttpRequestDto {
  private String id;
  private Object request;
  private Object response;
  private String externalId;
  private String bizType;
  private String uri;
  private String path;
  private ZonedDateTime createdAt;
  private Object queryParameters;
}
