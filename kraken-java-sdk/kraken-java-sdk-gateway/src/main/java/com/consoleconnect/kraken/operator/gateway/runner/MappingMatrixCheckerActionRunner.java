package com.consoleconnect.kraken.operator.gateway.runner;

import static com.consoleconnect.kraken.operator.core.enums.AssetKindEnum.PRODUCT_MAPPING_MATRIX;
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
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.web.server.ServerWebExchange;

@Service
@Slf4j
public class MappingMatrixCheckerActionRunner extends AbstractActionRunner {
  public static final String MAPPING_MATRIX_KEY = "mappingMatrixKey";
  public static final String TARGET_KEY = "targetKey";
  public static final String MATRIX = "matrix";
  public static final String MESSAGE_ALERT = "api use case is not supported %s";
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
      throw KrakenException.badRequest(MESSAGE_ALERT.formatted(":not deployed"));
    }
    if (StringUtils.isNotBlank(componentKey)
        && componentKey.endsWith(NOT_FOUND)
        && componentKey.contains(COLON)) {
      throw KrakenException.unProcessableEntity(
          String.format("%s should exist in request", componentKey.split(COLON)[0]));
    }
    if (targetKey.contains(ResponseCodeTransform.TARGET_KEY_NOT_FOUND)) {
      throw KrakenException.badRequest(MESSAGE_ALERT.formatted(":possibly product not supported"));
    }
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
    Map<String, List<PathCheck>> facets =
        JsonToolkit.fromJson(
            JsonToolkit.toJson(assetDtoPaging.getData().get(0).getFacets().get(MATRIX)),
            new TypeReference<>() {});
    if (Objects.isNull(facets) || !facets.containsKey(targetKey)) {
      throw KrakenException.badRequest(
          MESSAGE_ALERT.formatted(":lack in check rules for target key: " + targetKey));
    }
    // check enable/disable
    enableChecking(facets, targetKey);

    checkRequestConstraints(targetKey, inputs);
    DocumentContext documentContext = JsonPath.parse(inputs);
    StringBuilder builder = new StringBuilder();
    boolean allMatch =
        facets.get(targetKey).stream()
            .allMatch(
                pathCheckEntry -> {
                  boolean check = check(documentContext, pathCheckEntry);
                  log.info("Evaluate {} : {}", pathCheckEntry.name, check);
                  if (!check) {
                    builder.append(
                        pathCheckEntry.errorMsg != null
                            ? String.format(": %s", pathCheckEntry.errorMsg)
                            : String.format(
                                "item:%s,expected:%s; ",
                                pathCheckEntry.name, pathCheckEntry.value));
                  }
                  return check;
                });
    if (!allMatch) {
      throw KrakenException.badRequest(MESSAGE_ALERT.formatted(builder));
    }
  }

  public void enableChecking(Map<String, List<PathCheck>> facets, String targetKey) {
    Optional<PathCheck> enabledOpt =
        facets.get(targetKey).stream()
            .filter(check -> CHECK_NAME_ENABLED.equalsIgnoreCase(check.name))
            .findFirst();
    if (enabledOpt.isPresent() && "false".equals(enabledOpt.get().value)) {
      if (Objects.isNull(enabledOpt.get().errorMsg)) {
        throw KrakenException.badRequest(MESSAGE_ALERT.formatted(":disabled"));
      } else {
        throw KrakenException.badRequest(enabledOpt.get().errorMsg);
      }
    }
  }

  public boolean check(DocumentContext documentContext, PathCheck pathCheck) {
    Object realValue = null;
    try {
      realValue = documentContext.read(pathCheck.path());
    } catch (Exception e) {
      log.error("read json path error!");
      throwException(
          pathCheck,
          String.format("The parameter %s does not exist in the request.", pathCheck.name));
    }
    if (realValue instanceof JSONArray array) {
      return array.stream().allMatch(value -> checkExpect(pathCheck, value));
    }
    String s =
        realValue == null || StringUtils.isBlank(String.valueOf(realValue))
            ? StringUtils.EMPTY
            : String.valueOf(realValue);
    return checkExpect(pathCheck, s);
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
      case EXPECTED_TRUE -> {
        try {
          SpELEngine.evaluateWithoutSuppressException(
              pathCheck.value, Map.of(PARAM_NAME, value), Object.class);
        } catch (Exception e) {
          throwException(pathCheck, null);
        }
        return true;
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

  public void checkRequestConstraints(String targetKey, Map<String, Object> inputs) {
    UnifiedAssetDto assetDto = unifiedAssetService.findOne(targetKey);
    UnifiedAssetDto mapperAsset =
        unifiedAssetService.findOne(assetDto.getMetadata().getMapperKey());
    ComponentAPITargetFacets.Mappers mappers =
        UnifiedAsset.getFacets(mapperAsset, ComponentAPITargetFacets.class)
            .getEndpoints()
            .get(0)
            .getMappers();
    List<ComponentAPITargetFacets.Mapper> request = mappers.getRequest();
    for (ComponentAPITargetFacets.Mapper mapper : request) {
      if (StringUtils.isBlank(mapper.getTarget())
          || ParamLocationEnum.HYBRID.name().equals(mapper.getTargetLocation())) {
        continue;
      }
      if (MappingTypeEnum.ENUM.getKind().equals(mapper.getSourceType())
          || MappingTypeEnum.DISCRETE_VAR.getKind().equals(mapper.getSourceType())
          || MappingTypeEnum.CONTINUOUS_VAR.getKind().equals(mapper.getSourceType())) {
        checkEnumValue(
            mapper.getSource(),
            mapper.getTarget(),
            inputs,
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
      throw KrakenException.unProcessableEntity(
          String.format(
              "can not process %s = %s, value should be %s",
              expression.replace("@{{", "").replace("}}", ""), evaluateValue, expectedValue));
    }
  }

  private void checkEnumValue(
      String source,
      String target,
      Map<String, Object> inputs,
      List<String> valueList,
      String sourceType) {
    String constructedBody = constructBody(replaceStarToZero(source));
    List<String> params = extractMapperParam(source);
    if (CollectionUtils.isEmpty(params)) {
      return;
    }
    String evaluateValue = SpELEngine.evaluate(constructedBody, inputs, String.class);
    if (StringUtils.isNotBlank(target)
        && !target.contains("@{{")
        && !target.equals(evaluateValue)) {
      throw KrakenException.unProcessableEntity(
          String.format(
              "can not process %s = %s, value should be %s", params.get(0), evaluateValue, target));
    }
    if ((MappingTypeEnum.DISCRETE_VAR.getKind().equals(sourceType)
            || (MappingTypeEnum.ENUM.getKind().equals(sourceType)))
        && !valueList.contains(evaluateValue)) {
      throw KrakenException.unProcessableEntity(
          String.format(
              "can not process %s = %s, value should be in %s",
              params.get(0), evaluateValue, valueList));
    }
    if ((MappingTypeEnum.CONTINUOUS_VAR.getKind().equals(sourceType))) {
      List<Double> values = valueList.stream().map(Double::parseDouble).toList();
      double min = Collections.min(values);
      double max = Collections.max(values);
      if (StringUtils.isBlank(evaluateValue)
          || !NumberUtils.isCreatable(evaluateValue)
          || Double.parseDouble(evaluateValue) < min
          || Double.parseDouble(evaluateValue) > max) {
        throw KrakenException.unProcessableEntity(
            String.format(
                "can not process %s = %s, value should be in closed interval[%s, %s]",
                params.get(0), evaluateValue, min, max));
      }
    }
  }

  private void checkMappingValue(
      String expression, ParamLocationEnum location, Map<String, Object> inputs) {
    String target = null;
    switch (location) {
      case BODY -> target = constructBody(replaceStarToZero(expression));
      case QUERY -> target = constructQuery(replaceStarToZero(expression));
      case PATH -> target = constructPath(replaceStarToZero(expression));
      default -> throw KrakenException.unProcessableEntity("can not process request.");
    }
    List<String> params = extractMapperParam(expression);
    if (CollectionUtils.isEmpty(params)) {
      return;
    }
    try {
      // check param exist
      SpELEngine.evaluateWithoutSuppressException(target, inputs, String.class);
    } catch (Exception e) {
      throw KrakenException.unProcessableEntity(
          String.format(
              "can not process %s = %s, value should exist in request", params.get(0), null));
    }
  }

  private void throwException(PathCheck pathCheck, String defaultMsg) {
    String msg = pathCheck.errorMsg == null ? defaultMsg : pathCheck.errorMsg;
    if (pathCheck.code != null) {
      throw new KrakenException(pathCheck.code, msg);
    }
    throw KrakenException.unProcessableEntity(msg);
  }
}
