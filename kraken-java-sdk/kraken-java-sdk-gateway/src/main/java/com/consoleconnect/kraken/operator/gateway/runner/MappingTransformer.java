package com.consoleconnect.kraken.operator.gateway.runner;

import com.consoleconnect.kraken.operator.core.dto.ResponseTargetMapperDto;
import com.consoleconnect.kraken.operator.core.model.facet.ComponentAPIFacets;
import com.consoleconnect.kraken.operator.core.model.facet.ComponentAPITargetFacets;
import com.consoleconnect.kraken.operator.core.toolkit.JsonToolkit;
import com.consoleconnect.kraken.operator.gateway.template.SpELEngine;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import java.util.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

public interface MappingTransformer {
  String REPLACEMENT_KEY_PREFIX = "@{{";
  String REPLACEMENT_KEY_SUFFIX = "}}";
  String TARGET_PATTERN = "@\\{\\{([^}]+)\\}\\}";
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

  @Slf4j
  final class LogHolder {}

  /**
   * Transform mappers into response body
   *
   * @param endpoint the endpoint definition
   * @return the response body replaced by mappers
   */
  default String transform(
      ComponentAPITargetFacets.Endpoint endpoint, ResponseTargetMapperDto responseTargetMapperDto) {
    String responseBody = endpoint.getResponseBody();
    ComponentAPITargetFacets.Mappers mappers = endpoint.getMappers();
    if (Objects.isNull(mappers) || CollectionUtils.isEmpty(mappers.getResponse())) {
      return com.consoleconnect.kraken.operator.core.toolkit.StringUtils.compact(responseBody);
    }
    String compactedResp =
        com.consoleconnect.kraken.operator.core.toolkit.StringUtils.compact(responseBody);
    LogHolder.log.info("compactedResp:{}", compactedResp);
    List<ComponentAPITargetFacets.Mapper> response = mappers.getResponse();
    Map<String, String> target2Source = new HashMap<>();
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
      Pair<String, String> sourcePair =
          convertSource(
              mapper.getSource(),
              mapper.getDefaultValue(),
              mapper.getTargetType(),
              mapper.getReplaceStar());
      String target = convertTarget(mapper.getTarget());
      addTargetValueMapping(mapper, responseTargetMapperDto, target);
      LogHolder.log.info(
          "target:{}, source left:{}, source right:{}",
          target,
          sourcePair.getLeft(),
          sourcePair.getRight());
      target2Source.put(target, sourcePair.getRight());
    }
    String result = search(compactedResp, target2Source);
    LogHolder.log.info("response mapper transform result:{}", result);
    return result;
  }

  default void addTargetValueMapping(
      ComponentAPITargetFacets.Mapper mapper,
      ResponseTargetMapperDto responseTargetMapperDto,
      String target) {
    if (mapper.getTargetType() == null
        || !ENUM_KIND.equalsIgnoreCase(mapper.getTargetType())
        || ENUM_KIND.equalsIgnoreCase(mapper.getTargetType())
            && MapUtils.isEmpty(mapper.getValueMapping())) {
      return;
    }
    responseTargetMapperDto.getTargetPathMapper().add(target);
    responseTargetMapperDto
        .getTargetValueMapper()
        .putAll(
            MapUtils.isEmpty(mapper.getValueMapping())
                ? Collections.emptyMap()
                : mapper.getValueMapping());
  }

  default String renderStatus(
      ResponseTargetMapperDto responseTargetMapperDto, String responseBody) {
    try {
      LogHolder.log.info(
          "renderStatus targetValueMapping:{}", responseTargetMapperDto.getTargetValueMapper());
      List<String> pathExpressionList = new ArrayList<>();
      for (String path : responseTargetMapperDto.getTargetPathMapper()) {
        LogHolder.log.info("render state targetPathMapping:{}", path);
        String pathExpression = JSON_PATH_EXPRESSION_PREFIX + path;
        pathExpressionList.add(pathExpression);
      }
      return rewriteValueByJsonPath(
          pathExpressionList, responseBody, responseTargetMapperDto.getTargetValueMapper());
    } catch (Exception e) {
      LogHolder.log.error("render status error!", e);
      return responseBody;
    }
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
            doc.set(value, null);
          } else if (obj instanceof Integer i && i <= 0) {
            doc.set(value, null);
          } else if (obj instanceof Boolean b && !b) {
            doc.set(value, null);
          } else {
            LogHolder.log.warn("Reserved key:{}, value:{}", key, value);
          }
        });
    return doc.jsonString();
  }

  default String calculateBasedOnResponseBody(String responseBody, Map<String, Object> context) {
    String replace = responseBody.replace("((", "${").replace("))", "}");
    return SpELEngine.evaluate(JsonToolkit.fromJson(replace, Object.class), context, true);
  }

  default String rewriteValueByJsonPath(
      List<String> pathExpressionList, String jsonData, Map<String, String> map) {
    if (CollectionUtils.isEmpty(pathExpressionList) || StringUtils.isBlank(jsonData)) {
      return jsonData;
    }
    DocumentContext doc = JsonPath.parse(jsonData);
    pathExpressionList.forEach(
        pathExpression -> {
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
        });
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
    // Remove the leading "@" and double curly braces
    return customizedExpress.substring(
        REPLACEMENT_KEY_PREFIX.length(),
        customizedExpress.length() - REPLACEMENT_KEY_SUFFIX.length());
  }

  default Pair<String, String> convertSource(
      String source, String defaultValue, String targetType, Boolean replaceStar) {
    if (null == source || !source.startsWith(REPLACEMENT_KEY_PREFIX)) {
      return Pair.of(null, StringUtils.isBlank(source) ? defaultValue : source);
    }
    // Remove the leading "@" and double curly braces
    String strippedValue =
        source.substring(
            REPLACEMENT_KEY_PREFIX.length(), source.length() - REPLACEMENT_KEY_SUFFIX.length());
    LogHolder.log.info("source strippedValue:{}, targetType:{}", strippedValue, targetType);
    int loc = strippedValue.lastIndexOf(ARRAY_WILD_MASK);
    if (loc > 0) {
      if (Objects.equals(targetType, ENUM_KIND)) {
        return Pair.of(null, String.format("${%s}", strippedValue));
      }
      if (Boolean.TRUE.equals(replaceStar)) {
        return Pair.of(null, String.format("${%s}", replaceStar(strippedValue)));
      }
    }
    return Pair.of(null, "${" + strippedValue + "}");
  }

  default String search(String inputString, Map<String, String> target2Source) {
    // Define the target marker pattern
    Pattern pattern = Pattern.compile(TARGET_PATTERN);
    Matcher matcher = pattern.matcher(inputString);
    // Use StringBuilder for efficient string modification
    StringBuilder processedString = new StringBuilder();

    int lastEndIndex = 0;
    while (matcher.find()) {
      processedString.append(inputString, lastEndIndex, matcher.start());
      String target = matcher.group(1);
      String sourceValue = target2Source.get(target);
      if (sourceValue != null) {
        processedString.append(sourceValue);
      } else {
        // Handle missing source value, and array response
        // Removed processedString.append(target)
        LogHolder.log.info("Missing source value for target:{}", target);
      }
      lastEndIndex = matcher.end();
    }
    // Append the remaining string after the last match
    processedString.append(inputString.substring(lastEndIndex));

    return processedString.toString();
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
