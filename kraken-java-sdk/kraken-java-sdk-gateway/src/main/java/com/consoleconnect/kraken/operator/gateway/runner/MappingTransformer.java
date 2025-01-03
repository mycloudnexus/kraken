package com.consoleconnect.kraken.operator.gateway.runner;

import static com.consoleconnect.kraken.operator.core.toolkit.Constants.DOT;
import static com.consoleconnect.kraken.operator.core.toolkit.ConstructExpressionUtil.*;

import com.consoleconnect.kraken.operator.core.dto.StateValueMappingDto;
import com.consoleconnect.kraken.operator.core.model.facet.ComponentAPIFacets;
import com.consoleconnect.kraken.operator.core.model.facet.ComponentAPITargetFacets;
import com.consoleconnect.kraken.operator.core.toolkit.JsonToolkit;
import com.consoleconnect.kraken.operator.gateway.template.SpELEngine;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import io.jsonwebtoken.lang.Strings;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONArray;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

public interface MappingTransformer {
  String REPLACEMENT_KEY_PREFIX = "@{{";
  String REPLACEMENT_KEY_SUFFIX = "}}";
  String ARRAY_WILD_MASK = "[*]";
  String ARRAY_FIRST_ELE = "[0]";
  String ARRAY_FIRST_REGEX = "\\[0\\]";
  String TARGET_VALUE_MAPPER_KEY = "targetValueMapping";
  String JSON_PATH_EXPRESSION_PREFIX = "$.";
  String ENUM_KIND = "enum";
  String LENGTH_FUNC = "length()";
  String LEFT_SQUARE_BRACKET = "[";
  String RIGHT_SQUARE_BRACKET = "]";
  String FORWARD_DOWNSTREAM = "forwardDownstream";
  String REQUEST_BODY = "requestBody.";
  String RESPONSE_BODY = "responseBody";
  String MAPPING_TYPE = "array";
  String SLASH = "/";
  String MEF_REQ_BODY_JSON_ROOT = "$.mefRequestBody.";

  @Slf4j
  final class LogHolder {}

  private boolean isMissingMappers(ComponentAPITargetFacets.Endpoint endpoint) {
    return Objects.isNull(endpoint.getMappers())
        || CollectionUtils.isEmpty(endpoint.getMappers().getResponse());
  }

  private boolean isSkippableMapper(ComponentAPITargetFacets.Mapper mapper) {
    return StringUtils.isBlank(mapper.getSource()) && StringUtils.isBlank(mapper.getDefaultValue());
  }

  private boolean isDeletableMapper(ComponentAPITargetFacets.Mapper mapper) {
    return StringUtils.isNotBlank(mapper.getCheckPath())
        && StringUtils.isNotBlank(mapper.getDeletePath());
  }

  /**
   * Transform mappers into response body
   *
   * @param endpoint the endpoint definition
   * @return the response body replaced by mappers
   */
  default String transform(
      ComponentAPITargetFacets.Endpoint endpoint,
      StateValueMappingDto responseTargetMapperDto,
      Map<String, Object> inputs) {
    String responseBody = endpoint.getResponseBody();
    if (isMissingMappers(endpoint)) {
      return com.consoleconnect.kraken.operator.core.toolkit.StringUtils.compact(responseBody);
    }
    String compactedResponseBody =
        com.consoleconnect.kraken.operator.core.toolkit.StringUtils.compact(responseBody);
    LogHolder.log.info("compactedResponseBody:{}", compactedResponseBody);
    List<ComponentAPITargetFacets.Mapper> responseMappers = endpoint.getMappers().getResponse();
    for (ComponentAPITargetFacets.Mapper mapper : responseMappers) {
      compactedResponseBody =
          processMapper(mapper, responseTargetMapperDto, inputs, compactedResponseBody);
    }
    LogHolder.log.info("response mapper transform result:{}", compactedResponseBody);
    return com.consoleconnect.kraken.operator.core.toolkit.StringUtils.compact(
        compactedResponseBody);
  }

  private String processMapper(
      ComponentAPITargetFacets.Mapper mapper,
      StateValueMappingDto responseTargetMapperDto,
      Map<String, Object> inputs,
      String compactedResponseBody) {
    // Preparing check and delete path for final result
    if (isDeletableMapper(mapper)) {
      responseTargetMapperDto
          .getTargetCheckPathMapper()
          .put(mapper.getCheckPath(), mapper.getDeletePath());
    }
    // Reading response string, and find the target node
    // Using the source value to replace the value of target node
    if (isSkippableMapper(mapper)) {
      return compactedResponseBody;
    }
    if (MAPPING_TYPE.equals(mapper.getMappingType())) {
      String jsonPath = constructJsonPath(MEF_REQ_BODY_JSON_ROOT, mapper.getTarget());
      int length = lengthOfArrayNode(jsonPath, inputs);
      LogHolder.log.info(
          "Transforming responseBody array length:{}, json path:{}", length, jsonPath);
      if (length < 0) {
        compactedResponseBody =
            processMappingBody(mapper, responseTargetMapperDto, compactedResponseBody, 0);
      } else {
        int count = 0;
        while (count < length) {
          compactedResponseBody =
              processMappingBody(mapper, responseTargetMapperDto, compactedResponseBody, count);
          count++;
        }
      }
    } else {
      compactedResponseBody =
          processMappingBody(mapper, responseTargetMapperDto, compactedResponseBody, 0);
    }
    return compactedResponseBody;
  }

  private String processMappingBody(
      ComponentAPITargetFacets.Mapper mapper,
      StateValueMappingDto responseTargetMapperDto,
      String compactedResponseBody,
      int replaceIndex) {
    String convertedSource =
        convertSource(
            mapper.getSource(), mapper.getDefaultValue(), mapper.getReplaceStar(), replaceIndex);
    String convertedTarget =
        convertTarget(mapper.getTarget(), mapper.getReplaceStar(), replaceIndex);
    addTargetValueMapping(mapper, responseTargetMapperDto, convertedTarget);
    String jsonPointer =
        convertPathToJsonPointer(
            mapper.getTarget().replace(RESPONSE_BODY, StringUtils.EMPTY),
            buildSquareBracketIndex(replaceIndex));
    LogHolder.log.info(
        "jsonPointer:{}, converted source:{}, converted target:{},",
        jsonPointer,
        convertedSource,
        convertedTarget);
    compactedResponseBody =
        JsonToolkit.generateJson(jsonPointer, convertedSource, compactedResponseBody);
    // Expanding array items
    if (replaceIndex > 0) {
      return expandArrayItems(compactedResponseBody, mapper.getTarget(), replaceIndex, jsonPointer);
    }
    return compactedResponseBody;
  }

  default String buildSquareBracketIndex(int index) {
    return LEFT_SQUARE_BRACKET + index + RIGHT_SQUARE_BRACKET;
  }

  private String expandArrayItems(
      String compactedResponseBody, String target, int count, String jsonPointer) {
    DocumentContext doc = JsonPath.parse(compactedResponseBody);
    String arrayPath = constructJsonPath(Strings.EMPTY, target);
    int idx = arrayPath.indexOf(ARRAY_WILD_MASK);
    if (idx > 0) {
      String arrayRoot = arrayPath.substring(0, idx);
      Map<String, Object> map =
          doc.read(JSON_PATH_EXPRESSION_PREFIX + arrayRoot + ARRAY_FIRST_ELE, Map.class);
      for (Map.Entry<String, Object> entry : map.entrySet()) {
        String pointer = SLASH + arrayRoot + SLASH + count + SLASH + entry.getKey();
        if (jsonPointer.startsWith(pointer)) {
          continue;
        }
        String entryValue = replaceALL(entry.getValue().toString(), arrayRoot, count);
        compactedResponseBody =
            JsonToolkit.generateJson(pointer, entryValue, compactedResponseBody);
      }
    }
    return compactedResponseBody;
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
            String err =
                String.format(
                    "Json Path read key error, key:%s, value:%s will be deleted", key, value);
            LogHolder.log.error(err, e);
            deleteByPath(value, doc);
            return;
          }
          if (null == obj || (obj instanceof String str && (StringUtils.isBlank(str)))) {
            deleteByPath(value, doc);
          } else if (obj instanceof Integer i && i <= 0) {
            deleteByPath(value, doc);
          } else if (obj instanceof Boolean b && !b) {
            deleteByPath(value, doc);
          } else if (obj instanceof JSONArray array && array.isEmpty()) {
            deleteByPath(value, doc);
          } else {
            LogHolder.log.warn("Reserved key:{}, value:{}", key, value);
          }
        });
    return doc.jsonString();
  }

  default void deleteByPath(String path, DocumentContext doc) {
    try {
      doc.delete(path);
    } catch (Exception e) {
      LogHolder.log.warn("Delete path {} error: {}", path, e.getMessage());
    }
  }

  default String calculateBasedOnResponseBody(String responseBody, Map<String, Object> context) {
    String replace = responseBody.replace("((", "${").replace("))", "}");
    Object obj = JsonToolkit.fromJson(replace, Object.class);
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

  default int lengthOfArrayNode(String pathExpression, Object jsonData) {
    if (StringUtils.isEmpty(pathExpression) || Objects.isNull(jsonData)) {
      return -1;
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
      try {
        return doc.read(arrayRoot);
      } catch (Exception e) {
        String errMsg = "Failed to read json data:" + arrayRoot;
        LogHolder.log.error(errMsg, e);
      }
    }
    return -1;
  }

  default void overwritePathValue(
      DocumentContext doc, String pathExpression, Map<String, String> map) {
    String origin = doc.read(pathExpression);
    if (StringUtils.isNotBlank(origin)) {
      doc.set(pathExpression, map.get(origin) == null ? origin : map.get(origin));
    }
  }

  default String convertTarget(String target) {
    return convertTarget(target, false, 0);
  }

  default String convertTarget(String target, Boolean replaceStar, int replaceIndex) {
    if (null == target || !target.startsWith(REPLACEMENT_KEY_PREFIX)) {
      return target;
    }
    String strippedValue = target.replace(REQUEST_BODY, StringUtils.EMPTY);
    // Remove the leading "@" and double curly braces
    strippedValue =
        strippedValue.substring(
            REPLACEMENT_KEY_PREFIX.length(),
            strippedValue.length() - REPLACEMENT_KEY_SUFFIX.length());
    LogHolder.log.info("target strippedValue:{}", strippedValue);
    int loc = strippedValue.lastIndexOf(ARRAY_WILD_MASK);
    if (loc > 0 && Boolean.TRUE.equals(replaceStar)) {
      return replaceStar(strippedValue, replaceIndex);
    }
    return strippedValue;
  }

  default String convertSource(
      String source, String defaultValue, Boolean replaceStar, int replaceIndex) {
    if (null == source || !source.startsWith(REPLACEMENT_KEY_PREFIX)) {
      return (StringUtils.isBlank(source) ? defaultValue : source);
    }
    // Remove the leading "@" and double curly braces
    String strippedValue =
        source.substring(
            REPLACEMENT_KEY_PREFIX.length(), source.length() - REPLACEMENT_KEY_SUFFIX.length());
    LogHolder.log.info("source strippedValue:{}", strippedValue);
    int loc = strippedValue.lastIndexOf(ARRAY_WILD_MASK);
    if (!strippedValue.startsWith(RESPONSE_BODY)) {
      if (strippedValue.startsWith(ARRAY_FIRST_ELE) || strippedValue.startsWith(ARRAY_WILD_MASK)) {
        strippedValue = RESPONSE_BODY + strippedValue;
      } else {
        strippedValue = RESPONSE_BODY + "." + strippedValue;
      }
    }
    if (loc > 0) {
      if (Boolean.TRUE.equals(replaceStar)) {
        return String.format("${%s}", replaceStar(strippedValue, replaceIndex));
      }
      return String.format("${%s}", strippedValue);
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
    return replaceStar(str, 0);
  }

  default String replaceALL(String str, String arrayRootPrefix, int index) {
    if (StringUtils.isBlank(str)) {
      return str;
    }
    return str.replaceAll(
        arrayRootPrefix + ARRAY_FIRST_REGEX,
        arrayRootPrefix + LEFT_SQUARE_BRACKET + index + RIGHT_SQUARE_BRACKET);
  }

  default String replaceStar(String str, int index) {
    if (StringUtils.isBlank(str)) {
      return str;
    }
    String replacement =
        (index < 0 ? ARRAY_FIRST_ELE : LEFT_SQUARE_BRACKET + index + RIGHT_SQUARE_BRACKET);
    return str.replace(ARRAY_WILD_MASK, replacement);
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
