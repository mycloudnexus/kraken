package com.consoleconnect.kraken.operator.core.toolkit;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.util.Objects;

public class JsonDiffTool {
  private JsonDiffTool() {}

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  static {
    OBJECT_MAPPER.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
  }

  public static boolean diff(String oldJson, String newJson) {
    if (Objects.isNull(oldJson) || Objects.isNull(newJson)) {
      return false;
    }
    JsonNode oldJ = JsonToolkit.fromJson(oldJson, JsonNode.class);
    JsonNode newJ = JsonToolkit.fromJson(newJson, JsonNode.class);
    return !oldJ.equals(newJ);
  }
}
