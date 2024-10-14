package com.consoleconnect.kraken.operator.controller.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EndpointAudit {
  private String resource;
  private String resourceId;
  private String action;
  private String userId;
}
