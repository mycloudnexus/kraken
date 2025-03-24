package com.consoleconnect.kraken.operator.controller.model;

import com.consoleconnect.kraken.operator.core.dto.DeployComponentError;
import java.util.List;
import lombok.Data;

@Data
public class DeploymentFacet {
  public static final String KEY_COMPONENT_TAGS = "componentTags";

  public static final String KEY_ERRORS = "errors";

  public static final String KEY_FAILURE_REASON = "failureReason";

  List<ComponentTag> componentTags;

  List<DeployComponentError> errors;

  DeployComponentError failureReason;
}
