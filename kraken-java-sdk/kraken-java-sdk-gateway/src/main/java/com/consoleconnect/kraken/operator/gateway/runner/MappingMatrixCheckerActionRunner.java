package com.consoleconnect.kraken.operator.gateway.runner;

import static com.consoleconnect.kraken.operator.core.enums.AssetKindEnum.PRODUCT_MAPPING_MATRIX;
import static com.consoleconnect.kraken.operator.core.enums.ExpectTypeEnum.EXPECTED_INT;
import static com.consoleconnect.kraken.operator.core.toolkit.ConstructExpressionUtil.*;

import com.consoleconnect.kraken.operator.core.dto.Tuple2;
import com.consoleconnect.kraken.operator.core.dto.UnifiedAssetDto;
import com.consoleconnect.kraken.operator.core.enums.ActionTypeEnum;
import com.consoleconnect.kraken.operator.core.enums.ExpectTypeEnum;
import com.consoleconnect.kraken.operator.core.enums.MappingTypeEnum;
import com.consoleconnect.kraken.operator.core.enums.ParamLocationEnum;
import com.consoleconnect.kraken.operator.core.exception.KrakenException;
import com.consoleconnect.kraken.operator.core.model.AppProperty;
import com.consoleconnect.kraken.operator.core.model.UnifiedAsset;
import com.consoleconnect.kraken.operator.core.model.facet.ComponentAPIFacets;
import com.consoleconnect.kraken.operator.core.model.facet.ComponentAPITargetFacets;
import com.consoleconnect.kraken.operator.core.repo.UnifiedAssetRepository;
import com.consoleconnect.kraken.operator.core.service.UnifiedAssetService;
import com.consoleconnect.kraken.operator.core.toolkit.AssetsConstants;
import com.consoleconnect.kraken.operator.core.toolkit.JsonToolkit;
import com.consoleconnect.kraken.operator.core.toolkit.Paging;
import com.consoleconnect.kraken.operator.gateway.template.SpELEngine;
import com.fasterxml.jackson.core.type.TypeReference;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONArray;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.web.server.ServerWebExchange;

@Service
@Slf4j
public class MappingMatrixCheckerActionRunner extends AbstractActionRunner
    implements DataTypeChecker {
  public static final String MAPPING_MATRIX_KEY = "mappingMatrixKey";
  public static final String TARGET_KEY = "targetKey";
  public static final String MATRIX = "matrix";
  public static final String CHECK_NAME_ENABLED = "enabled";
  public static final String PARAM_NAME = "param";
  public static final String NOT_FOUND = "notFound";
  public static final String COLON = ":";
  private final UnifiedAssetService unifiedAssetService;
  private final UnifiedAssetRepository unifiedAssetRepository;

  public MappingMatrixCheckerActionRunner(
      AppProperty appProperty,
      UnifiedAssetService unifiedAssetService,
      UnifiedAssetRepository unifiedAssetRepository) {
    super(appProperty);
    this.unifiedAssetService = unifiedAssetService;
    this.unifiedAssetRepository = unifiedAssetRepository;
  }

  @Override
  public boolean canHandle(ComponentAPIFacets.Action action) {
    return ActionTypeEnum.MAPPING_MATRIX_CHECKER
        == ActionTypeEnum.fromString(action.getActionType());
  }

  @Override
  protected Optional<ServerWebExchange> runIt(
      ServerWebExchange exchange,
      ComponentAPIFacets.Action action,
      Map<String, Object> inputs,
      Map<String, Object> outputs) {
    onCheck(inputs);
    return Optional.empty();
  }

  protected void onCheck(Map<String, Object> inputs) {
    Assert.notNull(inputs.get(MAPPING_MATRIX_KEY), "mappingMatrixKey must not be null");
    Assert.notNull(inputs.get(TARGET_KEY), "targetKey must not be null");
    String componentKey = inputs.get(MAPPING_MATRIX_KEY).toString();
    String targetKey = inputs.get(TARGET_KEY).toString();
    if (unifiedAssetRepository.findOneByKey(targetKey).isEmpty()) {
      throw KrakenException.badRequest(API_CASE_NOT_SUPPORTED.formatted(":not deployed"));
    }
    if (StringUtils.isNotBlank(componentKey)
        && componentKey.endsWith(NOT_FOUND)
        && componentKey.contains(COLON)) {
      throw KrakenException.unProcessableEntityMissingProperty(
          String.format("%s should exist in request", componentKey.split(COLON)[0]));
    }
    if (targetKey.contains(ResponseCodeTransform.TARGET_KEY_NOT_FOUND)) {
      throw KrakenException.badRequest(
          API_CASE_NOT_SUPPORTED.formatted(":possibly product not supported"));
    }
    // <mapper-key,<checkName,(path, expected)
    Map<String, List<PathCheck>> facets = queryMatrixFacets(componentKey);
    if (Objects.isNull(facets) || !facets.containsKey(targetKey)) {
      throw KrakenException.badRequest(
          API_CASE_NOT_SUPPORTED.formatted(":lack in check rules for target key: " + targetKey));
    }
    if (unifiedAssetRepository.findOneByKey(targetKey).isEmpty()) {
      throw KrakenException.badRequest(API_CASE_NOT_SUPPORTED.formatted(":not deployed"));
    }
    // disable checking, 400
    checkDisabled(facets, targetKey);
    // matrix checking, body-422, query-400
    checkMatrixConstraints(facets, targetKey, inputs);
    // mapper checking, body-422, query-400
    checkMapperConstraints(targetKey, inputs);
  }

  public Map<String, List<PathCheck>> queryMatrixFacets(String componentKey) {
    Paging<UnifiedAssetDto> assetDtoPaging =
        unifiedAssetService.findBySpecification(
            Tuple2.ofList(
                AssetsConstants.FIELD_KIND,
                PRODUCT_MAPPING_MATRIX.getKind(),
                AssetsConstants.FIELD_KEY,
                componentKey),
            null,
            null,
            PageRequest.of(0, 1),
            null);
    // <mapper-key,<checkName,(path, expected)
    return JsonToolkit.fromJson(
        JsonToolkit.toJson(assetDtoPaging.getData().get(0).getFacets().get(MATRIX)),
        new TypeReference<>() {});
  }

  public void checkDisabled(Map<String, List<PathCheck>> facets, String targetKey) {
    facets.get(targetKey).stream()
        .filter(
            check -> CHECK_NAME_ENABLED.equalsIgnoreCase(check.name) && "false".equals(check.value))
        .findFirst()
        .ifPresent(
            check -> {
              String errorMessage =
                  (check.errorMsg != null
                      ? check.errorMsg
                      : API_CASE_NOT_SUPPORTED.formatted(":disabled"));
              throw KrakenException.badRequest(errorMessage);
            });
  }

  public void checkMatrixConstraints(
      Map<String, List<PathCheck>> facets, String targetKey, Map<String, Object> inputs) {
    DocumentContext documentContext = JsonPath.parse(inputs);
    StringBuilder builder = new StringBuilder();
    boolean allMatch =
        facets.get(targetKey).stream()
            .allMatch(
                pathCheckEntry -> {
                  boolean check = check(documentContext, pathCheckEntry);
                  log.info("Evaluate {} : {}", pathCheckEntry.path, check);
                  if (!check) {
                    builder.append(
                        pathCheckEntry.errorMsg != null
                            ? String.format(": %s", pathCheckEntry.errorMsg)
                            : String.format(
                                "item:@{{%s}},expected:%s; ",
                                extractCheckingPath(pathCheckEntry.path), pathCheckEntry.value));
                  }
                  return check;
                });
    if (!allMatch) {
      throw KrakenException.unProcessableEntityInvalidValue(
          API_CASE_NOT_SUPPORTED.formatted(builder.toString()));
    }
  }

  public boolean check(DocumentContext documentContext, PathCheck pathCheck) {
    Object realValue = readByPathCheckWithException(documentContext, pathCheck);
    if (realValue instanceof JSONArray array) {
      return array.stream().allMatch(value -> checkExpect(pathCheck, value));
    }
    if (EXPECTED_INT == pathCheck.expectType) {
      return checkExpectInteger(pathCheck, realValue);
    }
    String str = wrapAsString(realValue);
    return checkExpect(pathCheck, str);
  }

  public boolean checkExpect(PathCheck pathCheck, Object value) {
    switch (pathCheck.expectType) {
      case EXPECTED -> {
        return pathCheck.value.equalsIgnoreCase(value.toString());
      }
      case EXPECTED_EXIST -> {
        if (!Objects.equals(pathCheck.value, String.valueOf(Boolean.TRUE))) {
          throwException(pathCheck, null);
        }
        return true;
      }
      case EXPECTED_NON_NUMERIC_STR -> {
        return checkExpectNonNumericStr(pathCheck, value);
      }
      case EXPECTED_TRUE -> {
        try {
          SpELEngine.evaluateWithoutSuppressException(
              pathCheck.value, Map.of(PARAM_NAME, value), Object.class);
          return true;
        } catch (Exception e) {
          throwException(pathCheck, null);
        }
      }
      default -> {
        return false;
      }
    }
    return false;
  }

  public record PathCheck(
      String name,
      String path,
      ExpectTypeEnum expectType,
      String value,
      String errorMsg,
      Integer code) {}

  public void checkMapperConstraints(String targetKey, Map<String, Object> inputs) {
    UnifiedAssetDto assetDto = unifiedAssetService.findOne(targetKey);
    UnifiedAssetDto mapperAsset =
        unifiedAssetService.findOne(assetDto.getMetadata().getMapperKey());
    ComponentAPITargetFacets.Mappers mappers =
        UnifiedAsset.getFacets(mapperAsset, ComponentAPITargetFacets.class)
            .getEndpoints()
            .get(0)
            .getMappers();
    List<ComponentAPITargetFacets.Mapper> request = mappers.getRequest();
    DocumentContext documentContext = JsonPath.parse(inputs);
    for (ComponentAPITargetFacets.Mapper mapper : request) {
      if (StringUtils.isBlank(mapper.getTarget())
          || ParamLocationEnum.HYBRID.name().equals(mapper.getTargetLocation())) {
        continue;
      }
      if (MappingTypeEnum.ENUM.getKind().equals(mapper.getSourceType())
          || MappingTypeEnum.DISCRETE_STR.getKind().equals(mapper.getSourceType())
          || MappingTypeEnum.DISCRETE_INT.getKind().equals(mapper.getSourceType())
          || MappingTypeEnum.CONTINUOUS_DOUBLE.getKind().equals(mapper.getSourceType())
          || MappingTypeEnum.CONTINUOUS_INT.getKind().equals(mapper.getSourceType())) {
        checkEnumValue(
            mapper.getSource(),
            mapper.getTarget(),
            documentContext,
            mapper.getSourceValues(),
            mapper.getSourceType());
      } else if (mapper.getTarget() != null && !mapper.getTarget().contains("@{{")) {
        checkConstantValue(mapper.getSource(), mapper.getTarget(), inputs);
      } else {
        checkMappingValue(
            mapper.getSource(), ParamLocationEnum.valueOf(mapper.getSourceLocation()), inputs);
      }
    }
  }

  private void checkConstantValue(
      String expression, String expectedValue, Map<String, Object> inputs) {
    if (String.valueOf(expectedValue).contains("{{")) {
      return;
    }
    String evaluateValue = SpELEngine.evaluate(constructBody(expression), inputs, String.class);
    if (!Objects.equals(evaluateValue, expectedValue)) {
      throw KrakenException.unProcessableEntityInvalidFormat(
          String.format(SHOULD_BE_MSG, expression, evaluateValue, expectedValue));
    }
  }

  private void checkEnumValue(
      String source,
      String target,
      DocumentContext documentContext,
      List<String> valueList,
      String sourceType) {
    String constructedBody = constructJsonPathBody(replaceStarToZero(source));
    List<String> params = extractMapperParam(source);
    if (CollectionUtils.isEmpty(params)) {
      return;
    }
    Object evaluateValue =
        readByPathWithException(documentContext, constructedBody /*,params.get(0)*/, 422, null);
    validateSourceValue(sourceType, evaluateValue, params.get(0), valueList, target);
  }

  private void validateSourceValue(
      String sourceType,
      Object evaluateValue,
      String paramName,
      List<String> valueList,
      String target) {
    // Constants checking
    validateConstantValue(target, evaluateValue, paramName);

    // Enumeration and discrete string variables checking
    validateEnumOrDiscreteString(evaluateValue, paramName, valueList, sourceType);

    // Discrete integer checking
    validateDiscreteInteger(evaluateValue, paramName, valueList, sourceType);

    // Continuous integer variables checking
    validateContinuousInteger(evaluateValue, paramName, valueList, sourceType);

    // Continuous double variables checking
    validateContinuousDouble(evaluateValue, paramName, valueList, sourceType);
  }

  private void checkMappingValue(
      String expression, ParamLocationEnum location, Map<String, Object> inputs) {
    String target = null;
    switch (location) {
      case BODY -> target = constructBody(replaceStarToZero(expression));
      case QUERY -> target = constructQuery(replaceStarToZero(expression));
      case PATH -> target = constructPath(replaceStarToZero(expression));
      default -> throw KrakenException.unProcessableEntityInvalidFormat("can not process request.");
    }
    List<String> params = extractMapperParam(expression);
    if (CollectionUtils.isEmpty(params)) {
      return;
    }
    try {
      // check param exist
      SpELEngine.evaluateWithoutSuppressException(target, inputs, String.class);
    } catch (Exception e) {
      throw KrakenException.unProcessableEntityInvalidFormat(
          String.format(SHOULD_BE_EXIST, params.get(0), null));
    }
  }

  @Override
  public void throwException(PathCheck pathCheck, String defaultMsg) {
    String msg = pathCheck.errorMsg == null ? defaultMsg : pathCheck.errorMsg;
    if (pathCheck.code != null && pathCheck.code != HttpStatus.UNPROCESSABLE_ENTITY.value()) {
      throw new KrakenException(pathCheck.code, msg);
    }
    throw KrakenException.unProcessableEntityInvalidFormat(msg);
  }

  public Object readByPathCheckWithException(DocumentContext documentContext, PathCheck pathCheck) {
    return readByPathWithException(
        documentContext, pathCheck.path, pathCheck.code, pathCheck.errorMsg);
  }
}
