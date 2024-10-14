package com.consoleconnect.kraken.operator.controller.model;

import java.util.List;
import lombok.Data;

@Data
public class DeploymentFacet {
  public static final String KEY_COMPONENT_TAGS = "componentTags";

  List<ComponentTag> componentTags;
}
