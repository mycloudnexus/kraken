package com.consoleconnect.kraken.operator.controller.model;

import lombok.Data;

@Data
public class ComponentTag {
  private String parentComponentId;

  /**
   * for api level deployment, tag.parentComponentKey is mapperKey tag.parentComponentId is primary
   * id of mapper
   */
  private String parentComponentKey;

  private String parentComponentName;
  private String tagId;
  private String version;
}
