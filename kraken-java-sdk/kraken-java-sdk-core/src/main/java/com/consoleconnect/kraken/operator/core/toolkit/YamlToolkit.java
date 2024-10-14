package com.consoleconnect.kraken.operator.core.toolkit;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class YamlToolkit {

  public static final ObjectMapper mapper =
      new ObjectMapper(
          new YAMLFactory()
              .enable(YAMLGenerator.Feature.MINIMIZE_QUOTES)
              .disable(YAMLGenerator.Feature.SPLIT_LINES)
              .enable(YAMLGenerator.Feature.INDENT_ARRAYS_WITH_INDICATOR));

  static {
    mapper.findAndRegisterModules();
    mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    mapper.enable(SerializationFeature.INDENT_OUTPUT);
    mapper.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
  }

  private YamlToolkit() {}

  public static <T> Optional<T> parseYaml(String yaml, Class<T> clazz) {
    try {
      return Optional.of(mapper.readValue(yaml, clazz));
    } catch (Exception e) {
      errorMsg(e);
      return Optional.empty();
    }
  }

  private static void errorMsg(Exception ex) {
    log.warn("parseYaml failed:{}", ex.getMessage());
  }

  public static <T> Optional<T> parseYaml(String yaml, TypeReference<T> typeReference) {
    try {
      return Optional.of(mapper.readValue(yaml, typeReference));
    } catch (Exception e) {
      errorMsg(e);
      return Optional.empty();
    }
  }

  public static Optional<String> toYaml(Object obj) {
    try {
      return Optional.of(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj));
    } catch (Exception e) {
      errorMsg(e);
      return Optional.empty();
    }
  }
}
