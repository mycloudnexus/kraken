package com.consoleconnect.kraken.operator.core.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ActionTypeEnum {
  CACHE_REQUEST_BODY("buildin@cache-request-body"),
  MODIFY_REQUEST_BODY("buildin@modify-request-body"),
  MODIFY_RESPONSE_BODY("buildin@modify-response-body"),
  MOCK_RESPONSE("buildin@mock-response"),
  REWRITE_PATH("buildin@rewrite-path"),
  WORKFLOW("buildin@workflow"),
  DB("buildin@db"),
  REGISTER_EVENT("buildin@register-event"),
  PATTERN("buildin@pattern"),
  ENGINE_JAVASCRIPT("engine@javascript"),
  ENGINE_SPEL("engine@spel"),
  LOAD_TARGET_API_CONFIG("buildin@load-target-api-config"),
  DUMMY("buildin@dummy"),
  MAPPING_MATRIX_CHECKER("buildin@mapping-matrix-checker"),
  UNDEFINED("undefined");

  private final String kind;

  @JsonCreator
  public static ActionTypeEnum fromString(String text) {
    for (ActionTypeEnum b : ActionTypeEnum.values()) {
      if (b.kind.equalsIgnoreCase(text) || b.name().equalsIgnoreCase(text)) {
        return b;
      }
    }
    return ActionTypeEnum.UNDEFINED;
  }
}
