package com.consoleconnect.kraken.operator.gateway.runner;

import com.consoleconnect.kraken.operator.core.enums.MappingTypeEnum;
import com.consoleconnect.kraken.operator.core.exception.ErrorResponse;
import com.consoleconnect.kraken.operator.core.exception.KrakenException;
import com.jayway.jsonpath.DocumentContext;
import java.util.*;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.http.HttpStatus;

public interface DataTypeChecker {

  String API_CASE_NOT_SUPPORTED = "invalidValue, api use case is not supported %s";
  String EXPECT_INT_MSG = "invalidValue, can not process @{{%s}} = %s, %s found, %s expected";
  String PARAM_NOT_EXIST_MSG =
      "missingProperty, the parameter @{{%s}} does not exist in the request";
  String SHOULD_BE_MSG = "invalidValue, can not process %s = %s, value should be %s";
  String SHOULD_BE_IN_MSG = "invalidValue, can not process @{{%s}} = %s, value should be in %s";
  String SHOULD_BE_EXIST =
      "invalidValue, can not process @{{%s}} = %s, value should exist in request";
  String SHOULD_BE_INTERVAL =
      "invalidValue, can not process @{{%s}} = %s, value should be in closed interval[%s, %s]";
  String JSON_PATH_READ_ERR = "read json path error!";

  @Slf4j
  final class LogHolder {}

  default boolean checkExpectInteger(
      MappingMatrixCheckerActionRunner.PathCheck pathCheck, Object variable) {
    if (isNotInteger(variable)) {
      throwException(pathCheck, null);
    }
    return true;
  }

  default boolean checkExpectNonNumericStr(
      MappingMatrixCheckerActionRunner.PathCheck pathCheck, Object variable) {
    if (Objects.nonNull(variable) && NumberUtils.isDigits(variable.toString())) {
      throwException(pathCheck, null);
    }
    return true;
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
      throwException(
          code, errorMsg, String.format(PARAM_NOT_EXIST_MSG, extractCheckingPath(checkPath)));
    }
    return realValue;
  }

  default void throwException(Integer code, String errorMsg, String defaultMsg) {
    String msg = errorMsg == null ? defaultMsg : errorMsg;
    if (code != null && code != HttpStatus.UNPROCESSABLE_ENTITY.value()) {
      throw new KrakenException(code, msg);
    }
    throw routeException(msg);
  }

  default KrakenException routeException(String detailMessage) {
    if (StringUtils.isNotBlank(detailMessage)
        && detailMessage.contains(ErrorResponse.ErrorMapping.ERROR_422_MISSING_PROPERTY.getMsg())) {
      return KrakenException.unProcessableEntityMissingProperty(
          API_CASE_NOT_SUPPORTED.formatted(detailMessage));
    }
    if (StringUtils.isNotBlank(detailMessage)
        && detailMessage.contains(ErrorResponse.ErrorMapping.ERROR_422_INVALID_VALUE.getMsg())) {
      return KrakenException.unProcessableEntityInvalidValue(
          API_CASE_NOT_SUPPORTED.formatted(detailMessage));
    }
    return KrakenException.unProcessableEntityInvalidFormat(
        API_CASE_NOT_SUPPORTED.formatted(detailMessage));
  }

  default String wrapAsString(Object realValue) {
    return (realValue == null || StringUtils.isBlank(String.valueOf(realValue))
        ? StringUtils.EMPTY
        : String.valueOf(realValue));
  }

  default boolean isConstantType(String target) {
    return StringUtils.isNotBlank(target) && !target.contains("@{{");
  }

  default void validateConstantValue(String target, Object evaluateValue, String paramName) {
    if (isConstantType(target) && !target.equals(evaluateValue)) {
      throw KrakenException.unProcessableEntityInvalidValue(
          String.format(SHOULD_BE_IN_MSG, paramName, evaluateValue, target));
    }
  }

  default void validateEnumOrDiscreteString(
      Object evaluateValue, String paramName, List<String> valueList, String sourceType) {
    if (MappingTypeEnum.DISCRETE_STR.getKind().equals(sourceType)
        && Objects.nonNull(evaluateValue)) {
      Class<?> dataType = whichDataTypeClass(evaluateValue);
      if (!String.class.equals(dataType)) {
        throw KrakenException.unProcessableEntityInvalidValue(
            String.format(
                EXPECT_INT_MSG,
                paramName,
                evaluateValue,
                (dataType == null ? null : dataType.getSimpleName()),
                "String"));
      }
    }
    if ((MappingTypeEnum.DISCRETE_STR.getKind().equals(sourceType)
            || (MappingTypeEnum.ENUM.getKind().equals(sourceType)))
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
      Object evaluateValue, String paramName, List<String> valueList, String sourceType) {
    if (MappingTypeEnum.DISCRETE_INT.getKind().equals(sourceType)
        && Objects.nonNull(evaluateValue)) {
      if (isNotInteger(evaluateValue)) {
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

  default void validateContinuousInteger(
      Object evaluateValue, String paramName, List<String> valueList, String sourceType) {
    if (MappingTypeEnum.CONTINUOUS_INT.getKind().equals(sourceType)
        && Objects.nonNull(evaluateValue)) {
      if (isNotInteger(evaluateValue)) {
        throw KrakenException.unProcessableEntityInvalidValue(
            String.format(
                EXPECT_INT_MSG, paramName, evaluateValue, whichDataType(evaluateValue), "Integer"));
      }
      validateContinuousDouble(evaluateValue, paramName, valueList);
    }
  }

  default void validateContinuousDouble(
      Object evaluateValue, String paramName, List<String> valueList, String sourceType) {
    if (MappingTypeEnum.CONTINUOUS_DOUBLE.getKind().equals(sourceType)
        && Objects.nonNull(evaluateValue)) {
      if (isNotDouble(evaluateValue)) {
        throw KrakenException.unProcessableEntityInvalidValue(
            String.format(
                EXPECT_INT_MSG, paramName, evaluateValue, whichDataType(evaluateValue), "Integer"));
      }
      validateContinuousDouble(evaluateValue, paramName, valueList);
    }
  }

  default void validateContinuousDouble(
      Object evaluateValue, String paramName, List<String> valueList) {
    List<Double> values = valueList.stream().map(Double::parseDouble).toList();
    double min = Collections.min(values);
    double max = Collections.max(values);
    String valueStr = String.valueOf(evaluateValue);
    if (StringUtils.isBlank(valueStr)
        || !NumberUtils.isCreatable(valueStr)
        || Double.parseDouble(valueStr) < min
        || Double.parseDouble(valueStr) > max) {
      throw KrakenException.unProcessableEntityInvalidValue(
          String.format(SHOULD_BE_INTERVAL, paramName, evaluateValue, min, max));
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

  void throwException(MappingMatrixCheckerActionRunner.PathCheck pathCheck, String defaultMsg);
}