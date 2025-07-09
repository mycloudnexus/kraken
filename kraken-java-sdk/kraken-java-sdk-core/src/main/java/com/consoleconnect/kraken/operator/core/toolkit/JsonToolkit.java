package com.consoleconnect.kraken.operator.core.toolkit;

import com.consoleconnect.kraken.operator.core.exception.KrakenException;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import java.util.Iterator;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

public class JsonToolkit {

  private JsonToolkit() {}

  public static ObjectMapper createObjectMapper() {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    objectMapper.registerModule(new JavaTimeModule());
    objectMapper.registerModule(new JodaModule());

    return objectMapper;
  }

  private static final TypeReference<Map<String, String>> STRING_MAP_TYPE_REFERENCE =
      new TypeReference<Map<String, String>>() {};

  public static String toJson(Object data) {
    if (data == null) {
      return null;
    }
    try {
      return createObjectMapper().writeValueAsString(data);
    } catch (JsonProcessingException ex) {
      throw KrakenException.internalError(ex.getMessage());
    }
  }

  public static <T> T fromJson(String json, Class<T> classOfT) {
    try {
      return createObjectMapper().readValue(json, classOfT);
    } catch (JsonProcessingException ex) {
      throw KrakenException.internalError(ex.getMessage());
    }
  }

  public static Map<String, Object> convertToMap(Object obj) {
    return JsonToolkit.fromJson(
        JsonToolkit.toJson(obj), new TypeReference<Map<String, Object>>() {});
  }

  public static <T> T fromJson(Object object, Class<T> classOfT) {
    return fromJson(toJson(object), classOfT);
  }

  public static <T> T fromJson(Object object, TypeReference<T> valueTypeRef) {
    return fromJson(toJson(object), valueTypeRef);
  }

  public static <T> T fromJson(String json, TypeReference<T> valueTypeRef) {
    try {
      return createObjectMapper().readValue(json, valueTypeRef);
    } catch (JsonProcessingException ex) {
      throw KrakenException.internalError(ex.getMessage());
    }
  }

  public static String toPrettyJson(Object data) {
    try {
      return createObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(data);
    } catch (JsonProcessingException ex) {
      throw KrakenException.internalError(ex.getMessage());
    }
  }

  public static Map<String, String> toMap(String json) {
    return fromJson(json, STRING_MAP_TYPE_REFERENCE);
  }

  public static String parseValue(String path, String json) {
    DocumentContext jsonContext = JsonPath.parse(json);
    return jsonContext.read(path);
  }

  /**
   * @param jsonPointerPath format should be '/a/b/c'
   * @param value
   * @param initJson
   * @return
   */
  public static String generateJson(String jsonPointerPath, String value, String initJson) {
    initJson = StringUtils.isBlank(initJson) ? "{}" : initJson;
    try {
      ObjectMapper mapper = new ObjectMapper();
      JsonNode root = mapper.readValue(initJson, JsonNode.class);
      setJsonPointerValue(root, JsonPointer.compile(jsonPointerPath), new TextNode(value), mapper);
      return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(root);
    } catch (JsonProcessingException e) {
      throw KrakenException.internalError(e.getMessage());
    }
  }

  public static void setJsonPointerValue(
      JsonNode node, JsonPointer pointer, JsonNode value, ObjectMapper mapper) {
    JsonPointer parentPointer = pointer.head();
    JsonNode parentNode = node.at(parentPointer);
    String fieldName = pointer.last().toString().substring(1);

    if (parentNode.isMissingNode() || parentNode.isNull()) {
      parentNode =
          StringUtils.isNumeric(fieldName) ? mapper.createArrayNode() : mapper.createObjectNode();
      // recursively reconstruct hierarchy
      setJsonPointerValue(node, parentPointer, parentNode, mapper);
    }

    if (parentNode.isArray()) {
      ArrayNode arrayNode = (ArrayNode) parentNode;
      setArrayNodeValue(arrayNode, fieldName, value);
    } else if (parentNode.isObject()) {
      ((ObjectNode) parentNode).set(fieldName, value);
    }
  }

  private static void setArrayNodeValue(ArrayNode arrayNode, String fieldName, JsonNode value) {
    if (StringUtils.isNumeric(fieldName)) {
      int index = Integer.parseInt(fieldName);
      // expand array in case index is greater than array size (like JavaScript does)
      for (int i = arrayNode.size(); i <= index; i++) {
        arrayNode.addNull();
      }
      arrayNode.set(index, value);
    } else {
      // If fieldName is not numeric, replace the value of the field name
      Iterator<JsonNode> elements = arrayNode.elements();
      while (elements.hasNext()) {
        JsonNode element = elements.next();
        if (element.isObject() && StringUtils.isNotBlank(fieldName)) {
          ((ObjectNode) element).set(fieldName, value);
          // stop iterating after first match
          return;
        }
      }
    }
  }

  public static void validateJson(String json) {
    fromJson(json, Map.class);
  }
}
