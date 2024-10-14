package com.consoleconnect.kraken.operator.core.toolkit;

import com.consoleconnect.kraken.operator.core.exception.KrakenException;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import java.util.Map;

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
}
