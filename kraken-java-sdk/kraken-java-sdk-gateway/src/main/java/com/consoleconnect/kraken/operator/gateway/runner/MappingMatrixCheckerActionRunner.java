package com.consoleconnect.kraken.operator.gateway.runner;

import static com.consoleconnect.kraken.operator.core.enums.AssetKindEnum.PRODUCT_MAPPING_MATRIX;
import static com.consoleconnect.kraken.operator.core.enums.ParamLocationEnum.*;
import static com.consoleconnect.kraken.operator.core.toolkit.Constants.COMMA;
import static com.consoleconnect.kraken.operator.core.toolkit.Constants.COMMA_SPACE_EXPRESSION;
import static com.consoleconnect.kraken.operator.core.toolkit.ConstructExpressionUtil.*;

import com.consoleconnect.kraken.operator.core.dto.Tuple2;
import com.consoleconnect.kraken.operator.core.dto.UnifiedAssetDto;
import com.consoleconnect.kraken.operator.core.enums.ActionTypeEnum;
import com.consoleconnect.kraken.operator.core.enums.MappingTypeEnum;
import com.consoleconnect.kraken.operator.core.enums.ParamLocationEnum;
import com.consoleconnect.kraken.operator.core.exception.KrakenException;
import com.consoleconnect.kraken.operator.core.ingestion.ResourceLoaderFactory;
import com.consoleconnect.kraken.operator.core.model.AppProperty;
import com.consoleconnect.kraken.operator.core.model.HttpTask;
import com.consoleconnect.kraken.operator.core.model.UnifiedAsset;
import com.consoleconnect.kraken.operator.core.model.facet.ComponentAPIFacets;
import com.consoleconnect.kraken.operator.core.model.facet.ComponentAPITargetFacets;
import com.consoleconnect.kraken.operator.core.model.facet.ComponentValidationFacets;
import com.consoleconnect.kraken.operator.core.model.facet.ComponentWorkflowFacets;
import com.consoleconnect.kraken.operator.core.repo.UnifiedAssetRepository;
import com.consoleconnect.kraken.operator.core.service.AssetReader;
import com.consoleconnect.kraken.operator.core.service.UnifiedAssetService;
import com.consoleconnect.kraken.operator.core.toolkit.AssetsConstants;
import com.consoleconnect.kraken.operator.core.toolkit.JsonToolkit;
import com.consoleconnect.kraken.operator.core.toolkit.Paging;
import com.consoleconnect.kraken.operator.gateway.dto.PathCheck;
import com.consoleconnect.kraken.operator.gateway.entity.HttpRequestEntity;
import com.consoleconnect.kraken.operator.gateway.repo.HttpRequestRepository;
import com.consoleconnect.kraken.operator.gateway.template.SpELEngine;
import com.fasterxml.jackson.core.type.TypeReference;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONArray;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.server.ServerWebExchange;

@Service
@Slf4j
public class MappingMatrixCheckerActionRunner extends AbstractActionRunner
    implements DataTypeChecker, AssetReader {
  public static final String MAPPING_MATRIX_KEY = "mappingMatrixKey";
  public static final String TARGET_KEY = "targetKey";
  public static final String MATRIX = "matrix";
  public static final String CHECK_NAME_ENABLED = "enabled";
  public static final String PARAM_NAME = "param";
  public static final String NOT_FOUND = "notFound";
  public static final String COLON = ":";
  public static final String WORKFLOW_PREFIX = "workflow.";
  public static final String EXPECTED422_PATH_KEY = "expect-http-status-422-if-missing";
  public static final String CONVERT_FIELD_KEY = "convertField";
  private final UnifiedAssetService unifiedAssetService;
  private final UnifiedAssetRepository unifiedAssetRepository;
  private final HttpRequestRepository httpRequestRepository;
  @Getter private final ResourceLoaderFactory resourceLoaderFactory;

  public MappingMatrixCheckerActionRunner(
      AppProperty appProperty,
      UnifiedAssetService unifiedAssetService,
      UnifiedAssetRepository unifiedAssetRepository,
      HttpRequestRepository httpRequestRepository,
      ResourceLoaderFactory resourceLoaderFactory) {
    super(appProperty);
    this.unifiedAssetService = unifiedAssetService;
    this.unifiedAssetRepository = unifiedAssetRepository;
    this.httpRequestRepository = httpRequestRepository;
    this.resourceLoaderFactory = resourceLoaderFactory;
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
    String matrixKey = inputs.get(MAPPING_MATRIX_KEY).toString();
    String targetKey = inputs.get(TARGET_KEY).toString();
    if (unifiedAssetRepository.findOneByKey(targetKey).isEmpty()) {
      throw KrakenException.badRequest(API_CASE_NOT_SUPPORTED.formatted("not deployed"));
    }
    if (StringUtils.isNotBlank(matrixKey)
        && matrixKey.endsWith(NOT_FOUND)
        && matrixKey.contains(COLON)) {
      throw KrakenException.unProcessableEntityMissingProperty(
          String.format("%s should exist in request", matrixKey.split(COLON)[0]));
    }
    if (targetKey.contains(ResponseCodeTransform.TARGET_KEY_NOT_FOUND)) {
      throw KrakenException.badRequest(
          API_CASE_NOT_SUPPORTED.formatted(":possibly product not supported"));
    }
    Paging<UnifiedAssetDto> matrixAssets = queryMatrixAssets(matrixKey);
    // <mapper-key,[checkName,(path, expected)]>
    Map<String, List<PathCheck>> facets = readMatrixFacets(matrixAssets);
    if (Objects.isNull(facets) || !facets.containsKey(targetKey)) {
      throw KrakenException.badRequest(
          API_CASE_NOT_SUPPORTED.formatted(":lack in check rules for target key: " + targetKey));
    }
    if (unifiedAssetRepository.findOneByKey(targetKey).isEmpty()) {
      throw KrakenException.badRequest(API_CASE_NOT_SUPPORTED.formatted(":not deployed"));
    }

    // disable checking, 400
    checkDisabled(facets, targetKey);

    List<String> pathsExpected422 = readExpected422Paths(matrixAssets);
    // matrix checking, if-missing-return-400, wrong-value-return-422, paths under
    // 'expect-http-status-422-if-missing' always return 422
    checkMatrixConstraints(facets, targetKey, inputs, pathsExpected422);
    // check modification conditions
    checkModifyConstraints(readConvertField(matrixAssets), targetKey, inputs);
    // mapper checking, if-missing-return-400, wrong-value-return-422, paths under
    // 'expect-http-status-422-if-missing' always return 422
    checkMapperConstraints(targetKey, inputs, pathsExpected422);
  }

  public Map<String, List<PathCheck>> readMatrixFacets(Paging<UnifiedAssetDto> assetDtoPaging) {
    // <mapper-key,<checkName,(path, expected)
    return JsonToolkit.fromJson(
        JsonToolkit.toJson(assetDtoPaging.getData().get(0).getFacets().get(MATRIX)),
        new TypeReference<>() {});
  }

  public List<String> readExpected422Paths(Paging<UnifiedAssetDto> assetDtoPaging) {
    Object obj =
        assetDtoPaging.getData().get(0).getFacets().getOrDefault(EXPECTED422_PATH_KEY, null);
    return Objects.isNull(obj)
        ? List.of()
        : JsonToolkit.fromJson(JsonToolkit.toJson(obj), new TypeReference<>() {});
  }

  public String readConvertField(Paging<UnifiedAssetDto> assetDtoPaging) {
    return (String)
        assetDtoPaging.getData().get(0).getFacets().getOrDefault(CONVERT_FIELD_KEY, null);
  }

  public Paging<UnifiedAssetDto> queryMatrixAssets(String matrixKey) {
    return unifiedAssetService.findBySpecification(
        Tuple2.ofList(
            AssetsConstants.FIELD_KIND,
            PRODUCT_MAPPING_MATRIX.getKind(),
            AssetsConstants.FIELD_KEY,
            matrixKey),
        null,
        null,
        PageRequest.of(0, 1),
        null);
  }

  public void checkDisabled(Map<String, List<PathCheck>> facets, String targetKey) {
    facets.get(targetKey).stream()
        .filter(
            check ->
                CHECK_NAME_ENABLED.equalsIgnoreCase(check.name()) && "false".equals(check.value()))
        .findFirst()
        .ifPresent(
            check -> {
              String errorMessage =
                  (check.errorMsg() != null
                      ? check.errorMsg()
                      : API_CASE_NOT_SUPPORTED.formatted(":disabled"));
              throw KrakenException.badRequest(errorMessage);
            });
  }

  public void checkMatrixConstraints(
      Map<String, List<PathCheck>> facets,
      String targetKey,
      Map<String, Object> inputs,
      List<String> pathsExpected422) {
    DocumentContext documentContext = JsonPath.parse(inputs);
    StringBuilder builder = new StringBuilder();
    boolean allMatch =
        facets.get(targetKey).stream()
            .allMatch(
                pathCheckEntry -> {
                  boolean check = check(documentContext, pathCheckEntry, pathsExpected422);
                  log.info("Evaluate {} : {}", pathCheckEntry.path(), check);
                  if (!check) {
                    String pathName =
                        replaceWildcard(extractCheckingPath(pathCheckEntry.path()), 0);
                    builder.append(
                        pathCheckEntry.errorMsg() != null
                            ? String.format(": %s", pathCheckEntry.errorMsg())
                            : String.format(
                                "item:@{{%s}},expected:%s", pathName, pathCheckEntry.value()));
                  }
                  return check;
                });
    if (!allMatch) {
      throw KrakenException.unProcessableEntityInvalidValue(
          API_CASE_NOT_SUPPORTED.formatted(builder.toString()));
    }
  }

  public boolean check(
      DocumentContext documentContext, PathCheck pathCheck, List<String> pathsExpected422) {
    if (StringUtils.isBlank(pathCheck.path())) {
      return false;
    }
    Object realValue = readByPathCheckWithException(documentContext, pathCheck, pathsExpected422);
    // The 'index' indicates the location of elements in an array.
    // Since we need accurate information about which element has an unexpected value,
    // the index is a reasonable choice for identification in an array.
    if (realValue instanceof JSONArray array) {
      if (array.isEmpty()) {
        PathCheck updatedPathCheck = rewritePath(pathCheck, 0);
        throwException(
            pathCheck,
            String.format(
                getDefaultMessage(updatedPathCheck.code()),
                extractCheckingPath(updatedPathCheck.path())));
      }
      return IntStream.range(0, array.size())
          .allMatch(
              index -> {
                PathCheck updatedPathCheck = rewritePath(pathCheck, index);
                return checkExpect(updatedPathCheck, array.get(index));
              });
    }
    return checkExpect(pathCheck, realValue);
  }

  public boolean checkExpect(PathCheck pathCheck, Object value) {
    switch (pathCheck.expectType()) {
      case EXPECTED -> {
        if (pathCheck.value().contains(COMMA)) {
          return Arrays.stream(pathCheck.value().split(COMMA_SPACE_EXPRESSION))
              .anyMatch(item -> item.equalsIgnoreCase(wrapAsString(value)));
        } else {
          return pathCheck.value().equalsIgnoreCase(wrapAsString(value));
        }
      }
      case EXPECTED_START_WITH -> {
        return wrapAsString(value).startsWith(pathCheck.value());
      }
      case EXPECTED_EXIST -> {
        if (!Objects.equals(pathCheck.value(), String.valueOf(Boolean.TRUE))) {
          throwException(pathCheck, null);
        }
        return true;
      }
      case EXPECTED_TRUE -> {
        Object obj = null;
        try {
          obj =
              SpELEngine.evaluateWithoutSuppressException(
                  pathCheck.value(), Map.of(PARAM_NAME, value), Object.class);
        } catch (Exception e) {
          // 400-if-not-exist
          String checkingPath = rewriteCheckingPath(pathCheck);
          throw KrakenException.badRequestInvalidBody(
              String.format(MISSING_PROPERTY_MSG, checkingPath));
        }
        if (StringUtils.isNotBlank(pathCheck.expectedValueType())) {
          // 422-if-not-matched
          return checkExpectDataType(pathCheck, obj);
        }
        return true;
      }
      case EXPECTED_STR -> {
        return checkExpectString(pathCheck, value);
      }
      case EXPECTED_INT -> {
        return checkExpectInteger(pathCheck, value);
      }
      case EXPECTED_NUMERIC -> {
        return checkExpectNumeric(pathCheck, value);
      }
      case EXPECTED_NOT_BLANK -> {
        return checkExpectNotBlank(pathCheck, value);
      }
    }
    return false;
  }

  public void checkModifyConstraints(
      String convertField, String targetKey, Map<String, Object> inputs) {
    if (StringUtils.isBlank(convertField)) {
      return;
    }
    // Reading the configurable items to compare
    Optional<UnifiedAsset> modifyAssetOpt = readFromPath(getAppProperty().getModifyUseCase());
    if (modifyAssetOpt.isEmpty()) {
      return;
    }
    UnifiedAsset modifyAsset = modifyAssetOpt.get();
    ComponentValidationFacets modifyFacets =
        UnifiedAsset.getFacets(modifyAsset, ComponentValidationFacets.class);
    Set<String> supportedUseCases =
        modifyFacets.getModificationRules().stream()
            .map(ComponentValidationFacets.ModificationRule::getUseCase)
            .collect(Collectors.toSet());
    UnifiedAssetDto targetAsset = unifiedAssetService.findOne(targetKey);
    if (!supportedUseCases.contains(targetAsset.getMetadata().getMapperKey())) {
      return;
    }
    // Read instance id
    DocumentContext documentContext = JsonPath.parse(inputs);
    // 400 if no parameter
    Object instanceObj = readByPathWithException(documentContext, convertField, List.of(), "");
    // Query Order payload by instance id
    if (Objects.isNull(instanceObj)) {
      return;
    }
    List<HttpRequestEntity> httpRequestEntities =
        httpRequestRepository.findByProductInstanceId((String) instanceObj);
    // 404 if not found
    if (CollectionUtils.isEmpty(httpRequestEntities)) {
      throw KrakenException.notFound(
          "instanceId not exist", new IllegalArgumentException("instanceId not exist"));
    }
    // Filter the add order
    Optional<HttpRequestEntity> opt =
        httpRequestEntities.stream()
            .filter(
                item -> {
                  String request = JsonToolkit.toJson(item.getRequest());
                  return filterRequest(request);
                })
            .findFirst();
    if (opt.isEmpty()) {
      throw KrakenException.unProcessableEntityInvalidValue("The instanceId has no matched order");
    }
    // Reading the payload
    HttpRequestEntity orderRequest = opt.get();
    String payload = JsonToolkit.toJson(orderRequest.getRequest());
    // Check mapping items with the order payload
  }

  public void checkMapperConstraints(
      String targetKey, Map<String, Object> inputs, List<String> pathsExpected422) {
    UnifiedAssetDto assetDto = unifiedAssetService.findOne(targetKey);
    UnifiedAssetDto mapperAsset =
        unifiedAssetService.findOne(assetDto.getMetadata().getMapperKey());
    ComponentAPITargetFacets facets =
        UnifiedAsset.getFacets(mapperAsset, ComponentAPITargetFacets.class);
    List<ComponentAPITargetFacets.Mapper> request = new ArrayList<>();
    if (facets.getWorkflow() != null && facets.getWorkflow().isEnabled()) {
      UnifiedAssetDto workflowAsset = unifiedAssetService.findOne(facets.getWorkflow().getKey());
      ComponentWorkflowFacets workflowFacets =
          UnifiedAsset.getFacets(workflowAsset, ComponentWorkflowFacets.class);
      if (workflowFacets.getValidationMapper() != null) {
        request.addAll(workflowFacets.getValidationMapper());
        request.addAll(fetchMapper(workflowFacets.getExecutionStage()));
        request.addAll(fetchMapper(workflowFacets.getValidationStage()));
        request.addAll(fetchMapper(workflowFacets.getPreparationStage()));
      }
    } else {
      ComponentAPITargetFacets.Mappers mappers =
          UnifiedAsset.getFacets(mapperAsset, ComponentAPITargetFacets.class)
              .getEndpoints()
              .get(0)
              .getMappers();
      request.addAll(mappers.getRequest());
    }
    DocumentContext documentContext = JsonPath.parse(inputs);
    for (ComponentAPITargetFacets.Mapper mapper : request) {
      if (StringUtils.isBlank(mapper.getTarget())) {
        log.info("Skipped mapper due to blank target, source:{}", mapper.getSource());
        continue;
      }
      if (MappingTypeEnum.ENUM.getKind().equals(mapper.getSourceType())
          || MappingTypeEnum.STRING.getKind().equals(mapper.getSourceType())
          || isNumberKind(mapper.getAllowValueLimit(), mapper.getSourceType())) {
        checkEnumValue(documentContext, mapper, inputs, pathsExpected422);
      } else if (isConstantType(mapper.getTarget())) {
        checkConstantValue(documentContext, mapper, inputs, pathsExpected422);
      } else {
        checkMappingValue(documentContext, mapper, inputs, pathsExpected422);
      }
    }
  }

  private static List<ComponentAPITargetFacets.Mapper> fetchMapper(List<HttpTask> workflowFacets) {
    return workflowFacets.stream()
        .flatMap(httpTask -> httpTask.getEndpoint().getMappers().getRequest().stream())
        .filter(mapper -> !mapper.getTarget().contains(WORKFLOW_PREFIX))
        .toList();
  }

  public void checkConstantValue(
      DocumentContext documentContext,
      ComponentAPITargetFacets.Mapper mapper,
      Map<String, Object> inputs,
      List<String> pathsExpected422) {
    String expectedValue = mapper.getTarget();
    if (String.valueOf(expectedValue).contains("{{")) {
      return;
    }
    if (CollectionUtils.isNotEmpty(mapper.getSourceConditions())
        && !checkConditionsMatched(inputs, mapper.getSourceConditions())) {
      // Skip the checking
      return;
    }
    // Keep normal checking process
    List<String> params = extractMapperParam(mapper.getSource());
    String constructedBody = constructJsonPathBody(replaceStarToZero(mapper.getSource()));
    // if path in the excluded400Path, then throws 422, otherwise throws 400
    Object realValue =
        readByPathWithException(documentContext, constructedBody, pathsExpected422, null);
    validateConstantNumber(
        realValue, mapper, CollectionUtils.isEmpty(params) ? mapper.getSource() : params.get(0));
    String evaluateValue =
        SpELEngine.evaluate(constructBody(mapper.getSource()), inputs, String.class);
    if (!Objects.equals(evaluateValue, expectedValue)) {
      String msg =
          String.format(
              SHOULD_BE_MSG, extractCheckingPath(constructedBody), evaluateValue, expectedValue);
      throw KrakenException.unProcessableEntityInvalidValue(msg);
    }
  }

  public void checkEnumValue(
      DocumentContext documentContext,
      ComponentAPITargetFacets.Mapper mapper,
      Map<String, Object> inputs,
      List<String> pathsExpected422) {
    List<String> params = extractMapperParam(mapper.getSource());
    if (CollectionUtils.isEmpty(params)) {
      return;
    }
    if (CollectionUtils.isNotEmpty(mapper.getSourceConditions())
        && !checkConditionsMatched(inputs, mapper.getSourceConditions())) {
      // Skip the checking
      return;
    }

    String constructedBody = constructJsonPathBody(replaceStarToZero(mapper.getSource()));
    // if path in the excluded400Path, then throws 422, otherwise throws 400
    Object realValue =
        readByPathWithException(documentContext, constructedBody, pathsExpected422, null);
    validateSourceValue(
        mapper.getSourceType(),
        mapper.getDiscrete(),
        realValue,
        params.get(0),
        mapper.getSourceValues(),
        mapper.getTarget());
  }

  private void validateSourceValue(
      String sourceType,
      Boolean discrete,
      Object evaluateValue,
      String paramName,
      List<String> valueList,
      String target) {
    // Constants checking
    validateConstantValue(target, evaluateValue, paramName, sourceType);

    // Enumeration and discrete string variables checking
    validateEnumOrDiscreteString(evaluateValue, paramName, valueList, sourceType);

    // Discrete integer checking
    validateDiscreteInteger(evaluateValue, paramName, valueList, sourceType, discrete);

    // Continuous number variables checking, include integer and double
    validateContinuousNumber(evaluateValue, paramName, valueList, sourceType, discrete);
  }

  public void checkMappingValue(
      DocumentContext documentContext,
      ComponentAPITargetFacets.Mapper mapper,
      Map<String, Object> inputs,
      List<String> pathsExpected422) {
    String target = null;
    String jsonPathExpression = null;
    ParamLocationEnum location = ParamLocationEnum.valueOf(mapper.getSourceLocation());
    switch (location) {
      case BODY -> {
        target = constructBody(replaceStarToZero(mapper.getSource()));
        jsonPathExpression = constructJsonPathBody(replaceStarToZero(mapper.getSource()));
      }
      case QUERY -> target = constructQuery(replaceStarToZero(mapper.getSource()));
      case PATH -> target = constructPath(replaceStarToZero(mapper.getSource()));
      default -> throw KrakenException.unProcessableEntityInvalidFormat("can not process request.");
    }
    List<String> params = extractMapperParam(mapper.getSource());
    if (CollectionUtils.isEmpty(params)) {
      return;
    }
    if (CollectionUtils.isNotEmpty(mapper.getSourceConditions())
        && !checkConditionsMatched(inputs, mapper.getSourceConditions())) {
      // Skip the checking
      return;
    }

    String paramName = params.get(0);
    if (BODY.equals(location)) {
      log.info("jsonPathExpression:{}", jsonPathExpression);
      // if path in the excluded400Path, then throws 422, otherwise throws 400
      Object realValue =
          readByPathWithException(documentContext, jsonPathExpression, pathsExpected422, null);
      validateSourceValue(
          mapper.getSourceType(),
          mapper.getDiscrete(),
          realValue,
          params.get(0),
          mapper.getSourceValues(),
          mapper.getTarget());
    }
    if (QUERY.equals(location) || BODY.equals(location)) {
      try {
        SpELEngine.evaluateWithoutSuppressException(target, inputs, String.class);
      } catch (Exception e) {
        throw KrakenException.unProcessableEntityInvalidFormat(
            String.format(SHOULD_BE_EXIST, paramName, null));
      }
    }
  }

  public boolean checkConditionsMatched(
      Map<String, Object> inputs, List<ComponentAPITargetFacets.SourceCondition> sourceConditions) {
    return sourceConditions.stream()
        .filter(
            sourceCondition ->
                StringUtils.isNotBlank(sourceCondition.getKey())
                    && StringUtils.isNotBlank(sourceCondition.getVal())
                    && StringUtils.isNotBlank(sourceCondition.getOperator()))
        .allMatch(
            sourceCondition -> {
              String expression = buildSourceConditionExpression(sourceCondition);
              return SpELEngine.isTrue(expression, inputs);
            });
  }

  private String buildSourceConditionExpression(
      ComponentAPITargetFacets.SourceCondition sourceCondition) {
    return "${body."
        + extractMapperParam(sourceCondition.getKey()).get(0)
        + " "
        + sourceCondition.getOperator()
        + " '"
        + sourceCondition.getVal()
        + "'}";
  }

  @Override
  public void throwException(PathCheck pathCheck, String defaultMsg) {
    String msg = pathCheck.errorMsg() == null ? defaultMsg : pathCheck.errorMsg();
    if (pathCheck.code() != null && pathCheck.code() != HttpStatus.UNPROCESSABLE_ENTITY.value()) {
      throw new KrakenException(pathCheck.code(), msg);
    }
    throw route422Exception(msg, "");
  }

  public Object readByPathWithException(
      DocumentContext documentContext,
      String jsonPathExpression,
      List<String> pathsExpected422,
      String errorMsg) {
    int code = determineHttpCode(pathsExpected422, jsonPathExpression);
    return readByPathWithException(documentContext, jsonPathExpression, code, errorMsg);
  }

  public Object readByPathCheckWithException(
      DocumentContext documentContext, PathCheck pathCheck, List<String> pathsExpected422) {
    return readByPathWithException(
        documentContext, pathCheck.path(), pathsExpected422, pathCheck.errorMsg());
  }
}
