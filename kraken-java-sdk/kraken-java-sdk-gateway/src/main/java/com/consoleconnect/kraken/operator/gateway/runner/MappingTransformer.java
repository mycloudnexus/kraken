package com.consoleconnect.kraken.operator.gateway.runner;

import static com.consoleconnect.kraken.operator.core.toolkit.ConstructExpressionUtil.convertToJsonPointer;

import com.consoleconnect.kraken.operator.core.dto.StateValueMappingDto;
import com.consoleconnect.kraken.operator.core.model.facet.ComponentAPIFacets;
import com.consoleconnect.kraken.operator.core.model.facet.ComponentAPITargetFacets;
import com.consoleconnect.kraken.operator.core.toolkit.JsonToolkit;
import com.consoleconnect.kraken.operator.gateway.template.SpELEngine;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

public interface MappingTransformer {
  String REPLACEMENT_KEY_PREFIX = "@{{";
  String REPLACEMENT_KEY_SUFFIX = "}}";
  String ARRAY_WILD_MASK = "[*]";
  String ARRAY_FIRST_ELE = "[0]";
  String TARGET_VALUE_MAPPER_KEY = "targetValueMapping";
  String JSON_PATH_EXPRESSION_PREFIX = "$.";
  String DOT = ".";
  String ENUM_KIND = "enum";
  String LENGTH_FUNC = "length()";
  String LEFT_SQUARE_BRACKET = "[";
  String RIGHT_SQUARE_BRACKET = "]";
  String FORWARD_DOWNSTREAM = "forwardDownstream";
  String REQUEST_BODY = "requestBody.";
  String RESPONSE_BODY = "responseBody";

  @Slf4j
  final class LogHolder {}

  /**
   * Transform mappers into response body
   *
   * @param endpoint the endpoint definition
   * @return the response body replaced by mappers
   */
  default String transform(
      ComponentAPITargetFacets.Endpoint endpoint, StateValueMappingDto responseTargetMapperDto) {
    String responseBody = endpoint.getResponseBody();
    ComponentAPITargetFacets.Mappers mappers = endpoint.getMappers();
    if (Objects.isNull(mappers) || CollectionUtils.isEmpty(mappers.getResponse())) {
      return com.consoleconnect.kraken.operator.core.toolkit.StringUtils.compact(responseBody);
    }
    String compactedResponseBody =
        com.consoleconnect.kraken.operator.core.toolkit.StringUtils.compact(responseBody);
    LogHolder.log.info("compactedResponseBody:{}", compactedResponseBody);
    List<ComponentAPITargetFacets.Mapper> response = mappers.getResponse();
    for (ComponentAPITargetFacets.Mapper mapper : response) {
      // Preparing check and delete path for final result
      if (StringUtils.isNotBlank(mapper.getCheckPath())
          && StringUtils.isNotBlank(mapper.getDeletePath())) {
        responseTargetMapperDto
            .getTargetCheckPathMapper()
            .put(mapper.getCheckPath(), mapper.getDeletePath());
      }
      // Reading response string, and find the target node
      // Using the source value to replace the value of target node
      if (StringUtils.isBlank(mapper.getSource())
          && StringUtils.isBlank(mapper.getDefaultValue())) {
        continue;
      }
      String convertedSource =
          convertSource(
              mapper.getSource(),
              mapper.getDefaultValue(),
              mapper.getTargetType(),
              mapper.getReplaceStar());
      String convertedTarget = convertTarget(mapper.getTarget());
      addTargetValueMapping(mapper, responseTargetMapperDto, convertedTarget);
      LogHolder.log.info(
          "converted source:{}, converted target:{},", convertedSource, convertedTarget);
      compactedResponseBody =
          JsonToolkit.generateJson(
              convertToJsonPointer(mapper.getTarget().replace(REQUEST_BODY, StringUtils.EMPTY)),
              convertedSource,
              compactedResponseBody);
    }
    LogHolder.log.info("response mapper transform result:{}", compactedResponseBody);
    return com.consoleconnect.kraken.operator.core.toolkit.StringUtils.compact(
        compactedResponseBody);
  }

  default void addTargetValueMapping(
      ComponentAPITargetFacets.Mapper mapper,
      StateValueMappingDto responseTargetMapperDto,
      String target) {
    if (mapper.getTargetType() == null
        || !ENUM_KIND.equalsIgnoreCase(mapper.getTargetType())
        || ENUM_KIND.equalsIgnoreCase(mapper.getTargetType())
            && MapUtils.isEmpty(mapper.getValueMapping())) {
      return;
    }
    Map<String, Map<String, String>> targetPathValueMapping =
        responseTargetMapperDto.getTargetPathValueMapping();
    Map<String, String> valueMappings =
        targetPathValueMapping.getOrDefault(target, new HashMap<>());
    if (MapUtils.isNotEmpty(mapper.getValueMapping())) {
      valueMappings.putAll(mapper.getValueMapping());
    }
    targetPathValueMapping.put(target, valueMappings);
  }

  default String renderStatus(StateValueMappingDto responseTargetMapperDto, String responseBody) {
    for (Map.Entry<String, Map<String, String>> entry :
        responseTargetMapperDto.getTargetPathValueMapping().entrySet()) {
      try {
        String targetPath = entry.getKey();
        Map<String, String> valueMapping = entry.getValue();
        LogHolder.log.info("render state targetPath:{}, valueMapping:{}", targetPath, valueMapping);
        String pathExpression = JSON_PATH_EXPRESSION_PREFIX + targetPath;
        responseBody = rewriteValueByJsonPath(pathExpression, responseBody, valueMapping);
      } catch (Exception e) {
        LogHolder.log.error("render status error!", e);
      }
    }
    return responseBody;
  }

  default String deleteNodeByPath(Map<String, String> checkPathMap, String json) {
    DocumentContext doc = JsonPath.parse(json);
    checkPathMap.forEach(
        (key, value) -> {
          Object obj = null;
          try {
            obj = doc.read(key);
          } catch (Exception e) {
            String err = String.format("Json Path read key error, key:%s", key);
            LogHolder.log.error(err, e);
            return;
          }
          if (null == obj || (obj instanceof String str && (StringUtils.isBlank(str)))) {
            doc.delete(value);
          } else if (obj instanceof Integer i && i <= 0) {
            doc.delete(value);
          } else if (obj instanceof Boolean b && !b) {
            doc.delete(value);
          } else {
            LogHolder.log.warn("Reserved key:{}, value:{}", key, value);
          }
        });
    return doc.jsonString();
  }

  default String calculateBasedOnResponseBody(String responseBody, Map<String, Object> context) {
    String replace = responseBody.replace("((", "${").replace("))", "}");
    LogHolder.log.info("calculateBasedOnResponseBody replace:{}", replace);
    Object obj = JsonToolkit.fromJson(replace, Object.class);
    LogHolder.log.info("calculateBasedOnResponseBody obj:{}", obj);
    return SpELEngine.evaluate(obj, context, true);
  }

  default String rewriteValueByJsonPath(
      String pathExpression, String jsonData, Map<String, String> map) {
    if (StringUtils.isEmpty(pathExpression) || StringUtils.isBlank(jsonData)) {
      return jsonData;
    }
    DocumentContext doc = JsonPath.parse(jsonData);
    int idx = pathExpression.lastIndexOf(ARRAY_WILD_MASK);
    if (idx > 0) {
      String arrayRoot = pathExpression.substring(0, idx);
      if (arrayRoot.endsWith(DOT)) {
        arrayRoot = arrayRoot + LENGTH_FUNC;
      } else {
        arrayRoot = arrayRoot + DOT + LENGTH_FUNC;
      }
      int length = doc.read(arrayRoot);
      for (int i = 0; i < length; i++) {
        String exp =
            pathExpression.substring(0, idx)
                + LEFT_SQUARE_BRACKET
                + i
                + RIGHT_SQUARE_BRACKET
                + pathExpression.substring(idx + ARRAY_WILD_MASK.length());
        overwritePathValue(doc, exp, map);
      }
    } else {
      overwritePathValue(doc, pathExpression, map);
    }

    return doc.jsonString();
  }

  default void overwritePathValue(
      DocumentContext doc, String pathExpression, Map<String, String> map) {
    String origin = doc.read(pathExpression);
    if (StringUtils.isNotBlank(origin)) {
      doc.set(pathExpression, map.get(origin) == null ? origin : map.get(origin));
    }
  }

  default String convertTarget(String customizedExpress) {
    if (null == customizedExpress || !customizedExpress.startsWith(REPLACEMENT_KEY_PREFIX)) {
      return customizedExpress;
    }
    customizedExpress = customizedExpress.replace(REQUEST_BODY, StringUtils.EMPTY);
    // Remove the leading "@" and double curly braces
    return customizedExpress.substring(
        REPLACEMENT_KEY_PREFIX.length(),
        customizedExpress.length() - REPLACEMENT_KEY_SUFFIX.length());
  }

  default String convertSource(
      String source, String defaultValue, String targetType, Boolean replaceStar) {
    if (null == source || !source.startsWith(REPLACEMENT_KEY_PREFIX)) {
      return (StringUtils.isBlank(source) ? defaultValue : source);
    }
    // Remove the leading "@" and double curly braces
    String strippedValue =
        source.substring(
            REPLACEMENT_KEY_PREFIX.length(), source.length() - REPLACEMENT_KEY_SUFFIX.length());
    LogHolder.log.info("source strippedValue:{}, targetType:{}", strippedValue, targetType);
    int loc = strippedValue.lastIndexOf(ARRAY_WILD_MASK);
    if (!strippedValue.startsWith(RESPONSE_BODY)) {
      if (strippedValue.startsWith(ARRAY_FIRST_ELE) || strippedValue.startsWith(ARRAY_WILD_MASK)) {
        strippedValue = RESPONSE_BODY + strippedValue;
      } else {
        strippedValue = RESPONSE_BODY + "." + strippedValue;
      }
    }
    if (loc > 0) {
      if (Objects.equals(targetType, ENUM_KIND)) {
        return String.format("${%s}", strippedValue);
      }
      if (Boolean.TRUE.equals(replaceStar)) {
        return String.format("${%s}", replaceStar(strippedValue));
      }
    }
    return "${" + strippedValue + "}";
  }

  default void mergeMapper(
      ComponentAPITargetFacets facets, ComponentAPITargetFacets.Endpoint endpoint) {
    if (StringUtils.isBlank(facets.getEndpoints().get(0).getPath())) {
      facets.getEndpoints().get(0).setPath(endpoint.getPath());
    }
    if (StringUtils.isBlank(facets.getEndpoints().get(0).getMethod())) {
      facets.getEndpoints().get(0).setMethod(endpoint.getMethod());
    }
    facets.getEndpoints().get(0).setMappers(endpoint.getMappers());
    facets.getEndpoints().get(0).setServerKey(endpoint.getServerKey());
  }

  default String replaceStar(String str) {
    if (StringUtils.isBlank(str)) {
      return str;
    }
    return str.replace(ARRAY_WILD_MASK, ARRAY_FIRST_ELE);
  }

  default Boolean forwardDownstream(ComponentAPIFacets.Action config) {
    if (MapUtils.isNotEmpty(config.getEnv()) && config.getEnv().get(FORWARD_DOWNSTREAM) != null) {
      return Boolean.valueOf(config.getEnv().get(FORWARD_DOWNSTREAM));
    }
    if (MapUtils.isNotEmpty(config.getWith())
        && config.getWith().get(FORWARD_DOWNSTREAM) != null
        && config.getWith().get(FORWARD_DOWNSTREAM) instanceof Boolean forward) {
      return forward;
    }
    return Boolean.TRUE;
  }
}
