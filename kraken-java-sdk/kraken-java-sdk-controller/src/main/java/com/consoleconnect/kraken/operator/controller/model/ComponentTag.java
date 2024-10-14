package com.consoleconnect.kraken.operator.controller.model;

import lombok.Data;

@Data
public class ComponentTag {
  private String parentComponentId;
  private String parentComponentKey;
  private String parentComponentName;
  private String tagId;
  private String version;
}
