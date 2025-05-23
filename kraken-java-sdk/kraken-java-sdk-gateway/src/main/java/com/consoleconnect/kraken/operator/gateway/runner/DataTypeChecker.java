package com.consoleconnect.kraken.operator.gateway.runner;

import static com.jayway.jsonpath.Criteria.where;
import static com.jayway.jsonpath.Filter.filter;

import com.consoleconnect.kraken.operator.core.enums.MappingTypeEnum;
import com.consoleconnect.kraken.operator.core.exception.ErrorResponse;
import com.consoleconnect.kraken.operator.core.exception.KrakenException;
import com.consoleconnect.kraken.operator.core.model.FilterRule;
import com.consoleconnect.kraken.operator.core.model.facet.ComponentAPITargetFacets;
import com.consoleconnect.kraken.operator.core.model.facet.ComponentValidationFacets;
import com.consoleconnect.kraken.operator.core.toolkit.Constants;
import com.consoleconnect.kraken.operator.core.toolkit.ConstructExpressionUtil;
import com.consoleconnect.kraken.operator.gateway.dto.PathCheck;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.http.HttpStatus;

public interface DataTypeChecker {

  String API_CASE_NOT_SUPPORTED = "api use case is not supported: %s";
  String ACTION_NOT_SUPPORTED = "this action is not supported due to business constraints: %s";
  String EXPECT_INT_MSG = "invalidValue, can not process @{{%s}} = %s, %s found, %s expected";
  String EXPECT_INF_MSG = "invalidFormat, can not process @{{%s}} = %s, %s found, %s expected";
  String PARAM_NOT_EXIST_MSG =
      "missingProperty, the parameter @{{%s}} does not exist in the request";
  String MISSING_PROPERTY_MSG = "missingProperty, the parameter %s does not exist in the request";
  String SHOULD_BE_MSG = "invalidValue, can not process @{{%s}} = %s, value should be %s";
  String SHOULD_BE_IN_MSG = "invalidValue, can not process @{{%s}} = %s, value should be in %s";
  String SHOULD_NOT_BE_BLANK =
      "invalidValue, can not process @{{%s}} = %s, value should not be blank";
  String SHOULD_BE_EXIST =
      "invalidValue, can not process @{{%s}} = %s, value should exist in request";
  String SHOULD_BE_INTERVAL =
      "invalidValue, can not process @{{%s}} = %s, value should be in closed interval[%s, %s]";
  String JSON_PATH_READ_ERR = "read json path error!";
  String ARRAY_PARAM_PATTERN = "\\$\\{param\\.([^}]+)\\}";

  @Slf4j
  final class LogHolder {}

  default boolean checkExpectInteger(PathCheck pathCheck, Object variable) {
    if (isNotInteger(variable)) {
      throwException(pathCheck, null);
    }
    return true;
  }

  default boolean checkExpectNumeric(PathCheck pathCheck, Object variable) {
    if (isNotNumeric(variable)) {
      throwException(pathCheck, null);
    }
    return true;
  }

  default boolean checkExpectNotBlank(PathCheck pathCheck, Object variable) {
    if (Objects.isNull(variable) || StringUtils.isBlank(String.valueOf(variable))) {
      throwException(
          pathCheck,
          String.format(SHOULD_NOT_BE_BLANK, extractCheckingPath(pathCheck.path()), variable));
    }
    return true;
  }

  default boolean checkExpectString(PathCheck pathCheck, Object variable) {
    Class<?> dataType = whichDataTypeClass(variable);
    if (Objects.isNull(variable) || !String.class.equals(dataType)) {
      throwException(
          pathCheck,
          String.format(
              EXPECT_INT_MSG,
              extractCheckingPath(pathCheck.path()),
              variable,
              (dataType == null ? null : dataType.getSimpleName()),
              "String"));
    } else if (StringUtils.isBlank((String) variable)) {
      throwException(
          pathCheck,
          String.format(
              EXPECT_INT_MSG,
              extractCheckingPath(pathCheck.path()),
              variable,
              "empty string",
              "Non Empty String"));
    }
    return true;
  }

  default boolean isNotNumeric(Object variable) {
    return !(variable instanceof Number);
  }

  default boolean isNotInteger(Object variable) {
    return !(variable instanceof Integer);
  }

  default boolean isNotDouble(Object variable) {
    return !(variable instanceof Double);
  }

  default Object readByPathWithException(
      DocumentContext documentContext, String checkPath, Integer code, String errorMsg) {
    Object realValue = null;
    try {
      realValue = documentContext.read(checkPath);
    } catch (Exception e) {
      printJsonPathReadError();
      String defaultMsg = getDefaultMessage(code);
      throwException(code, errorMsg, String.format(defaultMsg, extractCheckingPath(checkPath)));
    }
    return realValue;
  }

  default boolean filterRequest(String request, FilterRule filterRule) {
    com.jayway.jsonpath.Predicate actionFilter =
        filter(where(filterRule.getFilterKey().trim()).eq(filterRule.getFilterVal().trim()));
    List<Object> list = JsonPath.read(request, filterRule.getFilterPath().trim(), actionFilter);
    return CollectionUtils.isNotEmpty(list);
  }

  default void checkModificationItems(
      String source, String target, ComponentValidationFacets.ModificationRule rule) {
    JsonNode sourceJson = null;
    JsonNode targetJson = null;
    try {
      ObjectMapper objectMapper = new ObjectMapper();
      sourceJson = objectMapper.readTree(source);
      targetJson = objectMapper.readTree(target);
    } catch (Exception e) {
      String error =
          String.format("Failed to read input json, source:%s, target:%s", source, target);
      throw KrakenException.badRequest(error);
    }

    // Check restrictedChanges (must be the same)
    for (ComponentValidationFacets.CompareItem item : rule.getRestrictedChanges()) {
      Object sourceValue = JsonPath.read(sourceJson.toString(), item.getSourceItem());
      Object targetValue = JsonPath.read(targetJson.toString(), item.getTargetItem());
      if (sourceValue == null || !sourceValue.equals(targetValue)) {
        String error =
            String.format(
                "Mismatch in restrictedChanges:@{{%s}}, source: %s does not match %s target: %s",
                item.getSourceItem(), sourceValue, item.getTargetItem(), targetValue);
        throw KrakenException.unProcessableEntityInvalidValue(error);
      }
    }
  }

  default String getDefaultMessage(Integer code) {
    return Integer.valueOf(HttpStatus.BAD_REQUEST.value()).equals(code)
        ? MISSING_PROPERTY_MSG
        : PARAM_NOT_EXIST_MSG;
  }

  default PathCheck rewritePath(PathCheck pathCheck, int index) {
    return StringUtils.isBlank(pathCheck.path())
        ? pathCheck
        : pathCheck.withUpdatedPath(replaceWildcard(pathCheck.path(), index));
  }

  default String replaceWildcard(String path, int index) {
    if (StringUtils.isBlank(path)) {
      return path;
    }
    return path.replace("[*]", "[" + index + "]");
  }

  default void throwException(Integer code, String errorMsg, String defaultMsg) {
    String msg = errorMsg == null ? defaultMsg : errorMsg;
    if (code != null && code != HttpStatus.UNPROCESSABLE_ENTITY.value()) {
      throw new KrakenException(code, msg, new IllegalArgumentException(msg));
    }
    throw route422Exception(msg);
  }

  default KrakenException route422Exception(String detailMessage) {
    return route422Exception(API_CASE_NOT_SUPPORTED, detailMessage);
  }

  default KrakenException route422Exception(String template, String detailMessage) {
    if (Objects.isNull(template)) {
      return KrakenException.unProcessableEntityInvalidFormat(detailMessage);
    }
    if (template.contains(ErrorResponse.ErrorMapping.ERROR_422_MISSING_PROPERTY.getMsg())
        || (StringUtils.isNotBlank(detailMessage)
            && detailMessage.contains(
                ErrorResponse.ErrorMapping.ERROR_422_MISSING_PROPERTY.getMsg()))) {
      return KrakenException.unProcessableEntityMissingProperty(template.formatted(detailMessage));
    }
    if (template.contains(ErrorResponse.ErrorMapping.ERROR_422_INVALID_VALUE.getMsg())
        || (StringUtils.isNotBlank(detailMessage)
            && detailMessage.contains(
                ErrorResponse.ErrorMapping.ERROR_422_INVALID_VALUE.getMsg()))) {
      return KrakenException.unProcessableEntityInvalidValue(template.formatted(detailMessage));
    }
    return KrakenException.unProcessableEntityInvalidFormat(template.formatted(detailMessage));
  }

  default String wrapAsString(Object realValue) {
    return (realValue == null || StringUtils.isBlank(String.valueOf(realValue))
        ? StringUtils.EMPTY
        : String.valueOf(realValue));
  }

  default boolean isNumberKind(Boolean allowValueLimit, String sourceType) {
    return Boolean.TRUE.equals(allowValueLimit)
        && (Constants.INT_VAL.equals(sourceType) || Constants.DOUBLE_VAL.equals(sourceType));
  }

  default boolean isConstantType(String target) {
    return StringUtils.isNotBlank(target) && !target.contains("@{{");
  }

  default void validateConstantValue(
      String target, Object evaluateValue, String paramName, String sourceType) {
    if (isConstantType(target)) {
      validateDiscreteString(evaluateValue, paramName, sourceType);
      Object targetObj = convertBySourceType(target, sourceType);
      if (Objects.isNull(targetObj) || !targetObj.equals(evaluateValue)) {
        throw KrakenException.unProcessableEntityInvalidValue(
            String.format(SHOULD_BE_MSG, paramName, evaluateValue, target));
      }
    }
  }

  default Object convertBySourceType(String target, String sourceType) {
    Object targetObj = null;
    try {
      if (Constants.INT_VAL.equals(sourceType)) {
        targetObj = Integer.valueOf(target);
      } else if (Constants.DOUBLE_VAL.equals(sourceType)) {
        targetObj = Double.valueOf(target);
      } else {
        targetObj = target;
      }
    } catch (Exception e) {
      LogHolder.log.error("Failed to convert by sourceType", e);
    }
    return targetObj;
  }

  default void validateDiscreteString(Object evaluateValue, String paramName, String sourceType) {
    if (MappingTypeEnum.STRING.getKind().equalsIgnoreCase(sourceType)) {
      Class<?> dataType = whichDataTypeClass(evaluateValue);
      if (!String.class.equals(dataType)) {
        throw KrakenException.unProcessableEntityInvalidFormat(
            String.format(
                EXPECT_INF_MSG,
                paramName,
                evaluateValue,
                (dataType == null ? null : dataType.getSimpleName()),
                "String"));
      }
    }
  }

  default String rewriteCheckingPath(PathCheck pathCheck) {
    String checkingPath = extractCheckingPath(pathCheck.path());
    if (checkingPath.endsWith("]")) {
      List<String> params =
          ConstructExpressionUtil.extractParam(pathCheck.value(), ARRAY_PARAM_PATTERN);
      if (CollectionUtils.isNotEmpty(params)) {
        checkingPath = checkingPath + "." + params.get(0);
      }
    }
    return checkingPath;
  }

  default boolean checkExpectDataType(PathCheck pathCheck, Object variable) {
    String dataType = whichDataType(variable);
    if (Objects.isNull(variable) || !pathCheck.expectedValueType().equalsIgnoreCase(dataType)) {
      String checkingPath = rewriteCheckingPath(pathCheck);
      throwException(
          pathCheck,
          String.format(
              EXPECT_INF_MSG, checkingPath, variable, dataType, pathCheck.expectedValueType()));
    }
    return true;
  }

  default void validateEnumOrDiscreteString(
      Object evaluateValue, String paramName, List<String> valueList, String sourceType) {
    validateDiscreteString(evaluateValue, paramName, sourceType);
    if ((MappingTypeEnum.STRING.getKind().equalsIgnoreCase(sourceType)
            || (MappingTypeEnum.ENUM.getKind().equalsIgnoreCase(sourceType)))
        && Objects.nonNull(evaluateValue)
        && CollectionUtils.isNotEmpty(valueList)
        && !valueList.contains(evaluateValue.toString())) {
      throw KrakenException.unProcessableEntityInvalidValue(
          String.format(SHOULD_BE_IN_MSG, paramName, evaluateValue, valueList));
    }
  }

  default String whichDataType(Object evaluateValue) {
    return (evaluateValue == null ? null : evaluateValue.getClass().getSimpleName());
  }

  default Class<?> whichDataTypeClass(Object evaluateValue) {
    return (evaluateValue == null ? null : evaluateValue.getClass());
  }

  default void validateDiscreteInteger(
      Object evaluateValue,
      String paramName,
      List<String> valueList,
      String sourceType,
      Boolean discrete) {
    if (MappingTypeEnum.DISCRETE_INT.getKind().equalsIgnoreCase(sourceType)
        && MappingTypeEnum.DISCRETE_INT.getDiscrete().equals(discrete)) {
      if (Objects.isNull(evaluateValue) || isNotInteger(evaluateValue)) {
        throw KrakenException.unProcessableEntityInvalidValue(
            String.format(
                EXPECT_INT_MSG, paramName, evaluateValue, whichDataType(evaluateValue), "Integer"));
      }
      if (CollectionUtils.isNotEmpty(valueList)
          && evaluateValue instanceof Integer evaluateIntVal) {
        Set<Integer> sets = valueList.stream().map(Integer::valueOf).collect(Collectors.toSet());
        if (!sets.contains(evaluateIntVal)) {
          throw KrakenException.unProcessableEntityInvalidValue(
              String.format(SHOULD_BE_IN_MSG, paramName, evaluateValue, valueList));
        }
      }
    }
  }

  default void validateContinuousNumber(
      Object evaluateValue,
      String paramName,
      List<String> valueList,
      String sourceType,
      Boolean discrete) {
    if (isContinuousInt(sourceType, discrete)) {
      validateNumber(
          evaluateValue, paramName, valueList, Integer::parseInt, "Integer", this::isNotInteger);
    } else if (isContinuousDouble(sourceType, discrete)) {
      validateNumber(
          evaluateValue, paramName, valueList, Double::parseDouble, "Double", this::isNotDouble);
    }
  }

  private <T extends Number & Comparable<T>> void validateNumber(
      Object evaluateValue,
      String paramName,
      List<String> valueList,
      Function<String, T> parser,
      String expectedType,
      Predicate<Object> invalidCheck) {
    if (Objects.isNull(evaluateValue) || invalidCheck.test(evaluateValue)) {
      throw KrakenException.unProcessableEntityInvalidValue(
          String.format(
              EXPECT_INT_MSG,
              paramName,
              evaluateValue,
              whichDataType(evaluateValue),
              expectedType));
    }
    validateContinuousValue(evaluateValue, paramName, valueList, parser);
  }

  private boolean isContinuousInt(String sourceType, Boolean discrete) {
    return MappingTypeEnum.CONTINUOUS_INT.getKind().equalsIgnoreCase(sourceType)
        && MappingTypeEnum.CONTINUOUS_INT.getDiscrete().equals(discrete);
  }

  private boolean isContinuousDouble(String sourceType, Boolean discrete) {
    return MappingTypeEnum.CONTINUOUS_DOUBLE.getKind().equalsIgnoreCase(sourceType)
        && MappingTypeEnum.CONTINUOUS_DOUBLE.getDiscrete().equals(discrete);
  }

  default <T extends Number & Comparable<T>> void validateContinuousValue(
      Object evaluateValue, String paramName, List<String> valueList, Function<String, T> parser) {
    List<T> values = valueList.stream().map(parser).toList();
    T min = Collections.min(values);
    T max = Collections.max(values);
    String valueStr = String.valueOf(evaluateValue);

    if (StringUtils.isBlank(valueStr)
        || !NumberUtils.isCreatable(valueStr)
        || isOutsideRange(parser.apply(valueStr), min, max)) {
      throw KrakenException.unProcessableEntityInvalidValue(
          String.format(SHOULD_BE_INTERVAL, paramName, evaluateValue, min, max));
    }
  }

  private <T extends Comparable<T>> boolean isOutsideRange(T value, T min, T max) {
    return value.compareTo(min) < 0 || value.compareTo(max) > 0;
  }

  default void validateConstantNumber(
      Object evaluateValue, ComponentAPITargetFacets.Mapper mapper, String paramName) {
    if (MappingTypeEnum.DISCRETE_INT.getKind().equalsIgnoreCase(mapper.getSourceType())
        && NumberUtils.isCreatable(mapper.getTarget())) {
      Class<?> dataType = whichDataTypeClass(evaluateValue);
      if (Objects.isNull(evaluateValue) || isNotNumeric(evaluateValue)) {
        throw KrakenException.unProcessableEntityInvalidValue(
            String.format(
                EXPECT_INT_MSG,
                paramName,
                evaluateValue,
                (dataType == null ? null : dataType.getSimpleName()),
                "Number"));
      }
    }
  }

  default void printJsonPathReadError() {
    LogHolder.log.error(JSON_PATH_READ_ERR);
  }

  default String extractCheckingPath(String path) {
    if (StringUtils.isBlank(path)) {
      return path;
    }
    return path.replace("$.body.", "").replace("$.query.", "");
  }

  default int determineHttpCode(List<String> pathsExpected422, String actualPath) {
    if (CollectionUtils.isNotEmpty(pathsExpected422) && StringUtils.isNotBlank(actualPath)) {
      return pathsExpected422.stream().anyMatch(actualPath::startsWith)
          ? HttpStatus.UNPROCESSABLE_ENTITY.value()
          : HttpStatus.BAD_REQUEST.value();
    }
    return HttpStatus.BAD_REQUEST.value();
  }

  void throwException(PathCheck pathCheck, String defaultMsg);
}
