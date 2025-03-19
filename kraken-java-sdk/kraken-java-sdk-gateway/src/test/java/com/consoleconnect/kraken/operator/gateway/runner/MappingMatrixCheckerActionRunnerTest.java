package com.consoleconnect.kraken.operator.gateway.runner;

import static com.consoleconnect.kraken.operator.gateway.runner.ResponseCodeTransform.TARGET_KEY_NOT_FOUND;

import com.consoleconnect.kraken.operator.core.enums.ExpectTypeEnum;
import com.consoleconnect.kraken.operator.core.enums.MappingTypeEnum;
import com.consoleconnect.kraken.operator.core.exception.KrakenException;
import com.consoleconnect.kraken.operator.core.model.FilterRule;
import com.consoleconnect.kraken.operator.core.model.UnifiedAsset;
import com.consoleconnect.kraken.operator.core.model.facet.ComponentAPITargetFacets;
import com.consoleconnect.kraken.operator.core.service.UnifiedAssetService;
import com.consoleconnect.kraken.operator.core.toolkit.Constants;
import com.consoleconnect.kraken.operator.core.toolkit.JsonToolkit;
import com.consoleconnect.kraken.operator.core.toolkit.YamlToolkit;
import com.consoleconnect.kraken.operator.gateway.CustomConfig;
import com.consoleconnect.kraken.operator.gateway.FilterRulesCreator;
import com.consoleconnect.kraken.operator.gateway.dto.PathCheck;
import com.consoleconnect.kraken.operator.gateway.entity.HttpRequestEntity;
import com.consoleconnect.kraken.operator.gateway.repo.HttpRequestRepository;
import com.consoleconnect.kraken.operator.test.AbstractIntegrationTest;
import com.consoleconnect.kraken.operator.test.MockIntegrationTest;
import com.fasterxml.jackson.core.type.TypeReference;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import java.io.IOException;
import java.util.*;
import lombok.SneakyThrows;
import org.apache.commons.lang3.tuple.Pair;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;

@MockIntegrationTest
@ContextConfiguration(classes = CustomConfig.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class MappingMatrixCheckerActionRunnerTest extends AbstractIntegrationTest
    implements FilterRulesCreator {
  @Autowired MappingMatrixCheckerActionRunner mappingMatrixCheckerActionRunner;
  @Autowired HttpRequestRepository httpRequestRepository;
  @Autowired UnifiedAssetService unifiedAssetService;

  @Test
  @Order(1)
  void givenTargetKeyNotFound_whenOnCheck_thenReturnException() {
    Map<String, Object> inputs = new HashMap<>();
    inputs.put("targetKey", TARGET_KEY_NOT_FOUND);
    inputs.put("mappingMatrixKey", "kraken.product.mapping.matrix");
    KrakenException krakenException =
        Assertions.assertThrowsExactly(
            KrakenException.class, () -> mappingMatrixCheckerActionRunner.onCheck(inputs));
    MatcherAssert.assertThat(
        krakenException.getMessage(),
        Matchers.containsString("api use case is not supported: not deployed"));
  }

  @Test
  @Order(2)
  void givenTargetKeyNotDeployed_whenOnCheck_thenReturnException() {
    Map<String, Object> inputs = new HashMap<>();
    inputs.put("targetKey", "mef.sonata.api-target.order.eline.read2");
    inputs.put("mappingMatrixKey", "mef.sonata.api.matrix.address.validation");
    KrakenException krakenException =
        Assertions.assertThrowsExactly(
            KrakenException.class, () -> mappingMatrixCheckerActionRunner.onCheck(inputs));
    MatcherAssert.assertThat(
        krakenException.getMessage(),
        Matchers.containsString("""
            api use case is not supported: not deployed"""));
  }

  @Test
  @Order(3)
  void givenTargetKeyNoRules_whenOnCheck_thenReturnException() {
    Map<String, Object> inputs = new HashMap<>();
    inputs.put("targetKey", "mef.sonata.api-target.order.eline.read");
    inputs.put("mappingMatrixKey", "mef.sonata.api.matrix.address.validation");
    KrakenException krakenException =
        Assertions.assertThrowsExactly(
            KrakenException.class, () -> mappingMatrixCheckerActionRunner.onCheck(inputs));
    MatcherAssert.assertThat(
        krakenException.getMessage(),
        Matchers.containsString("lack in check rules for target key"));
  }

  @Test
  @Order(3)
  void givenTargetKeyDisabled_whenOnCheck_thenReturnException() {
    Map<String, Object> inputs = new HashMap<>();
    inputs.put("targetKey", "mef.sonata.api-target.address.validate");
    inputs.put("mappingMatrixKey", "mef.sonata.api.matrix.address.validation");
    KrakenException krakenException =
        Assertions.assertThrowsExactly(
            KrakenException.class, () -> mappingMatrixCheckerActionRunner.onCheck(inputs));
    MatcherAssert.assertThat(krakenException.getMessage(), Matchers.containsString("disabled"));
  }

  @Test
  @Order(4)
  @SneakyThrows
  void givenPayload_whenOnCheck_thenReturnOK() {
    Map<String, Object> inputs = new HashMap<>();
    inputs.put("query", Map.of("buyerId", "test-company"));
    inputs.put(
        "body",
        JsonToolkit.fromJson(readFileToString("/mockData/productOrderRequest.json"), Object.class));
    inputs.put("targetKey", "mef.sonata.api-target.order.eline.add");
    inputs.put("mappingMatrixKey", "mef.sonata.api.matrix.order");
    Assertions.assertDoesNotThrow(() -> mappingMatrixCheckerActionRunner.onCheck(inputs));
  }

  @Test
  @Order(4)
  void givenPayloadWithNoRoute_whenOnCheck_thenThrow422() {
    Map<String, Object> inputs = new HashMap<>();
    inputs.put("targetKey", "targetKey:notFound");
    inputs.put("mappingMatrixKey", "productType:notFound");
    Assertions.assertThrows(
        KrakenException.class, () -> mappingMatrixCheckerActionRunner.onCheck(inputs));
  }

  @Test
  @Order(4)
  @SneakyThrows
  void givenPayloadMissEnum_whenOnCheck_thenReturnError() {
    validateOrderRequest("/mockData/productOrderRequestMissEnum.json", "MBPS");
  }

  @Test
  @Order(4)
  @SneakyThrows
  void givenPayloadWrongConstant_whenOnCheck_thenReturnError() {
    validateOrderRequest("/mockData/productOrderRequestMissConstant.json", "productOffering");
  }

  @Test
  @Order(4)
  @SneakyThrows
  void givenPayloadMissMappingParam_whenOnCheck_thenReturnError() {
    validateOrderRequest("/mockData/productOrderRequestMissMappingParam.json", "productOffering");
  }

  @Test
  @Order(4)
  @SneakyThrows
  void givenOutOfRangeBandwidth_whenChecking_thenThrowsExceptionMessageAsExpected() {
    String requestJson = "/mockData/productOrderRequestOutOfRangeBandwidth.json";
    String expected = "interval";
    String targetKey = "mef.sonata.api-target.order.uni.add";
    String mappingMatrixKey = "mef.sonata.api.matrix.order";
    validateOrderRequest(requestJson, expected, targetKey, mappingMatrixKey);
  }

  @Test
  @Order(4)
  @SneakyThrows
  void givenErrorMsgInMatrixItem_whenChecking_thenThrowsExceptionMessageAsExpected() {
    String expected = "api use case is not supported : disabled : instantSyncQuote==false";
    validateQuoteRequest("/mockData/quoteWithInstantSyncFalse.json", expected);
  }

  private void validateOrderRequest(String request, String matchedMsg) throws IOException {
    String targetKey = "mef.sonata.api-target.order.eline.add";
    String mappingMatrixKey = "mef.sonata.api.matrix.order";
    validateOrderRequest(request, matchedMsg, targetKey, mappingMatrixKey);
  }

  private void validateOrderRequest(
      String request, String matchedMsg, String targetKey, String mappingMatrixKey)
      throws IOException {
    Map<String, Object> inputs = new HashMap<>();
    inputs.put("query", Map.of("buyerId", "test-company"));
    inputs.put("body", JsonToolkit.fromJson(readFileToString(request), Object.class));
    inputs.put("targetKey", targetKey);
    inputs.put("mappingMatrixKey", mappingMatrixKey);
    KrakenException krakenException =
        Assertions.assertThrowsExactly(
            KrakenException.class, () -> mappingMatrixCheckerActionRunner.onCheck(inputs));
    if (Objects.nonNull(krakenException.getCause())) {
      MatcherAssert.assertThat(
          krakenException.getCause().getMessage(), Matchers.containsString(matchedMsg));
    }
  }

  private void validateQuoteRequest(String request, String matchedMsg) throws IOException {
    Map<String, Object> inputs = new HashMap<>();
    inputs.put("query", Map.of("buyerId", "test-company"));
    inputs.put("body", JsonToolkit.fromJson(readFileToString(request), Object.class));
    inputs.put("targetKey", "mef.sonata.api-target.quote.uni.add");
    inputs.put("mappingMatrixKey", "mef.sonata.api.matrix.quote.uni.add");
    KrakenException krakenException =
        Assertions.assertThrowsExactly(
            KrakenException.class, () -> mappingMatrixCheckerActionRunner.onCheck(inputs));
    MatcherAssert.assertThat(krakenException.getMessage(), Matchers.containsString(matchedMsg));
  }

  public static List<Pair<PathCheck, Object>> buildIllegalPathCheckList() {
    PathCheck pathCheck1 =
        new PathCheck(
            "expect1", "user", ExpectTypeEnum.EXPECTED_EXIST, "user", "error", null, null);
    PathCheck pathCheck2 =
        new PathCheck(
            "expect2", "user", ExpectTypeEnum.EXPECTED_TRUE, "${param.id}", "error", 400, null);
    PathCheck pathCheck3 =
        new PathCheck("expect3", "user", ExpectTypeEnum.EXPECTED_STR, null, "error", 422, null);
    PathCheck pathCheck4 =
        new PathCheck("expect4", "user", ExpectTypeEnum.EXPECTED_INT, null, "error", 422, null);
    PathCheck pathCheck5 =
        new PathCheck("expect5", "user", ExpectTypeEnum.EXPECTED_NUMERIC, null, "error", 422, null);
    PathCheck pathCheck6 =
        new PathCheck(
            "expect6", "user", ExpectTypeEnum.EXPECTED_NOT_BLANK, null, "error", 422, null);

    PathCheck pathCheck7 =
        new PathCheck(
            "expect7",
            "$.body.relatedContactInformation[*]",
            ExpectTypeEnum.EXPECTED_TRUE,
            "${param.emailAddress}",
            "error",
            400,
            "String");

    PathCheck pathCheck8 =
        new PathCheck(
            "expect7",
            "$.body.relatedContactInformation[*]",
            ExpectTypeEnum.EXPECTED_TRUE,
            "${param.emailAddress}",
            "error",
            400,
            "String");

    Pair<PathCheck, Object> pair1 = Pair.of(pathCheck1, "user1");
    Pair<PathCheck, Object> pair2 = Pair.of(pathCheck2, "user1");
    Pair<PathCheck, Object> pair3A = Pair.of(pathCheck3, null);
    Pair<PathCheck, Object> pair3B = Pair.of(pathCheck3, 123);
    Pair<PathCheck, Object> pair4 = Pair.of(pathCheck4, "123");
    Pair<PathCheck, Object> pair5 = Pair.of(pathCheck5, "123");
    Pair<PathCheck, Object> pair6 = Pair.of(pathCheck6, "");
    Pair<PathCheck, Object> pair7 = Pair.of(pathCheck7, Map.of("emailAddress", 123));
    Pair<PathCheck, Object> pair8 = Pair.of(pathCheck8, Map.of("emailAddress1", ""));

    return List.of(pair1, pair2, pair3A, pair3B, pair4, pair5, pair6, pair7, pair8);
  }

  @ParameterizedTest
  @MethodSource(value = "buildIllegalPathCheckList")
  void givenCheckPath_whenCheckExpect_thenReturnException(Pair<PathCheck, Object> pair) {
    Assertions.assertThrowsExactly(
        KrakenException.class,
        () -> mappingMatrixCheckerActionRunner.checkExpect(pair.getLeft(), pair.getRight()));
  }

  public static List<Pair<PathCheck, Object>> buildLegalPathCheckList() {
    PathCheck pathCheck1 =
        new PathCheck(
            ExpectTypeEnum.EXPECTED.name(),
            "user",
            ExpectTypeEnum.EXPECTED,
            "true",
            "",
            null,
            null);
    PathCheck pathCheck2 =
        new PathCheck(
            ExpectTypeEnum.EXPECTED_EXIST.name(),
            "user",
            ExpectTypeEnum.EXPECTED_EXIST,
            "true",
            "",
            null,
            null);
    PathCheck pathCheck3 =
        new PathCheck(
            ExpectTypeEnum.EXPECTED_TRUE.name(),
            "$.body.submittedGeographicAddress.['country']",
            ExpectTypeEnum.EXPECTED_TRUE,
            "${param}",
            "",
            422,
            null);
    PathCheck pathCheck4 =
        new PathCheck(
            ExpectTypeEnum.EXPECTED_STR.name(), "", ExpectTypeEnum.EXPECTED_STR, "", "", 422, null);
    PathCheck pathCheck5 =
        new PathCheck(
            ExpectTypeEnum.EXPECTED_INT.name(), "", ExpectTypeEnum.EXPECTED_INT, "", "", 422, null);
    PathCheck pathCheck6 =
        new PathCheck(
            ExpectTypeEnum.EXPECTED_NUMERIC.name(),
            "",
            ExpectTypeEnum.EXPECTED_NUMERIC,
            "",
            "",
            422,
            null);
    PathCheck pathCheck7 =
        new PathCheck(
            ExpectTypeEnum.EXPECTED_NOT_BLANK.name(),
            "",
            ExpectTypeEnum.EXPECTED_NOT_BLANK,
            "",
            "",
            422,
            null);

    PathCheck pathCheck8 =
        new PathCheck(
            ExpectTypeEnum.EXPECTED_START_WITH.name(),
            "",
            ExpectTypeEnum.EXPECTED_START_WITH,
            "UNI",
            "",
            422,
            null);

    Pair<PathCheck, Object> pair1 = Pair.of(pathCheck1, "true");
    Pair<PathCheck, Object> pair2 = Pair.of(pathCheck2, null);
    Pair<PathCheck, Object> pair3 = Pair.of(pathCheck3, "AU");
    Pair<PathCheck, Object> pair4 = Pair.of(pathCheck4, "string here");
    Pair<PathCheck, Object> pair5 = Pair.of(pathCheck5, 123);
    Pair<PathCheck, Object> pair6 = Pair.of(pathCheck6, 123.4);
    Pair<PathCheck, Object> pair7 = Pair.of(pathCheck7, "not blank");
    Pair<PathCheck, Object> pair8 = Pair.of(pathCheck8, "UNI-123");

    return List.of(pair1, pair2, pair3, pair4, pair5, pair6, pair7, pair8);
  }

  @ParameterizedTest
  @MethodSource(value = "buildLegalPathCheckList")
  void givenCheckPath_whenCheckExpect_thenReturnTrue(Pair<PathCheck, Object> pair) {
    Assertions.assertTrue(
        mappingMatrixCheckerActionRunner.checkExpect(pair.getLeft(), pair.getRight()));
  }

  @Test
  @Order(5)
  void givenCheckPath_whenCheckExpect_thenReturnFalse() {
    PathCheck pathCheck =
        new PathCheck("expect", "user", ExpectTypeEnum.EXPECTED, "user", "error", null, null);
    Assertions.assertFalse(mappingMatrixCheckerActionRunner.checkExpect(pathCheck, "user1"));
  }

  @Test
  void givenNonDiscreteString_whenValidating_thenThrowsException() {
    Assertions.assertThrowsExactly(
        KrakenException.class,
        () ->
            mappingMatrixCheckerActionRunner.validateDiscreteString(
                123, "x", MappingTypeEnum.STRING.getKind()));
  }

  @Test
  void givenStringValueList_whenValueNotInDiscreteStr_thenThrowsException() {
    Assertions.assertThrowsExactly(
        KrakenException.class,
        () ->
            mappingMatrixCheckerActionRunner.validateEnumOrDiscreteString(
                "4", "x", List.of("1", "2", "3"), MappingTypeEnum.STRING.getKind()));
  }

  @Test
  void givenNotInteger_whenValidatingDiscreteInt_thenThrowsException() {
    Assertions.assertThrowsExactly(
        KrakenException.class,
        () ->
            mappingMatrixCheckerActionRunner.validateDiscreteInteger(
                "4", "x", List.of(), MappingTypeEnum.DISCRETE_INT.getKind(), true));
  }

  @Test
  void givenIntegerNotIn_whenValidatingDiscreteInt_thenThrowsException() {
    Assertions.assertThrowsExactly(
        KrakenException.class,
        () ->
            mappingMatrixCheckerActionRunner.validateDiscreteInteger(
                4, "x", List.of("1", "2", "3"), MappingTypeEnum.DISCRETE_INT.getKind(), true));
  }

  @Test
  void givenNotInteger_whenValidatingContinuousInt_thenThrowsException() {
    Assertions.assertThrowsExactly(
        KrakenException.class,
        () ->
            mappingMatrixCheckerActionRunner.validateContinuousNumber(
                "4", "x", List.of(), MappingTypeEnum.CONTINUOUS_INT.getKind(), false));
  }

  @Test
  void givenIntegerNotIn_whenValidatingContinuousInt_thenThrowsException() {
    Assertions.assertThrowsExactly(
        KrakenException.class,
        () ->
            mappingMatrixCheckerActionRunner.validateContinuousNumber(
                4, "x", List.of("1", "3"), MappingTypeEnum.CONTINUOUS_INT.getKind(), false));
  }

  @Test
  void givenNotDouble_whenValidatingContinuousDouble_thenThrowsException() {
    Assertions.assertThrowsExactly(
        KrakenException.class,
        () ->
            mappingMatrixCheckerActionRunner.validateContinuousNumber(
                "4", "x", List.of(), MappingTypeEnum.CONTINUOUS_DOUBLE.getKind(), false));
  }

  @Test
  void givenDoubleNotIn_whenValidatingContinuousDouble_thenThrowsException() {
    Assertions.assertThrowsExactly(
        KrakenException.class,
        () ->
            mappingMatrixCheckerActionRunner.validateContinuousNumber(
                4.0,
                "x",
                List.of("1.0", "3.9"),
                MappingTypeEnum.CONTINUOUS_DOUBLE.getKind(),
                false));
  }

  @Test
  void givenDoubleInRange_whenValidatingContinuousDouble_thenNoException() {
    Assertions.assertDoesNotThrow(
        () ->
            mappingMatrixCheckerActionRunner.validateContinuousNumber(
                3.2,
                "x",
                List.of("1.0", "3.9"),
                MappingTypeEnum.CONTINUOUS_DOUBLE.getKind(),
                false));
  }

  @Test
  void givenIntegerInRange_whenValidatingContinuousInteger_thenNoException() {
    Assertions.assertDoesNotThrow(
        () ->
            mappingMatrixCheckerActionRunner.validateContinuousNumber(
                3, "x", List.of("1", "4"), MappingTypeEnum.CONTINUOUS_INT.getKind(), false));
  }

  @Test
  void givenExpected422Paths_whenDetermineHttpCode_thenReturnOK() {
    Assertions.assertEquals(
        HttpStatus.BAD_REQUEST.value(),
        mappingMatrixCheckerActionRunner.determineHttpCode(List.of(), ""));
    String bandwidth = "$.body.productOrderItem[0].product.productConfiguration.bandwidth";
    List<String> pathsExpected422 =
        List.of("$.body.productOrderItem[0].product.productConfiguration");
    Assertions.assertEquals(
        HttpStatus.UNPROCESSABLE_ENTITY.value(),
        mappingMatrixCheckerActionRunner.determineHttpCode(pathsExpected422, bandwidth));
    Assertions.assertEquals(
        HttpStatus.BAD_REQUEST.value(),
        mappingMatrixCheckerActionRunner.determineHttpCode(
            pathsExpected422, "$.body.productOrderItem[0].product.x"));
  }

  @ParameterizedTest
  @MethodSource(value = "buildNormalSourceTypeAndTarget")
  void givenNumberSourceTypeAndTarget_whenConvert_thenReturnOK(Pair<String, String> pair) {
    Object result =
        mappingMatrixCheckerActionRunner.convertBySourceType(pair.getLeft(), pair.getRight());
    Assertions.assertNotNull(result);
  }

  public static List<Pair<String, String>> buildNormalSourceTypeAndTarget() {
    Pair<String, String> pair1 = Pair.of("123", Constants.INT_VAL);
    Pair<String, String> pair2 = Pair.of("123.1", Constants.DOUBLE_VAL);
    return List.of(pair1, pair2);
  }

  @Test
  void givenNumberSourceTypeAndStringTarget_whenConvert_thenReturnNothing() {
    Object result = mappingMatrixCheckerActionRunner.convertBySourceType("abc", Constants.INT_VAL);
    Assertions.assertNull(result);
  }

  @ParameterizedTest
  @MethodSource(value = "buildPathCheckList")
  void givenIndex_whenRewritePath_thenReturnOK(Pair<String, PathCheck> pair) {
    PathCheck updatedPathCheck = mappingMatrixCheckerActionRunner.rewritePath(pair.getRight(), 0);
    Assertions.assertEquals(pair.getLeft(), updatedPathCheck.path());
  }

  public static List<Pair<String, PathCheck>> buildPathCheckList() {
    PathCheck pathCheck1 =
        new PathCheck(
            "EXPECTED_STR",
            "$.body.quoteItem[0].product.place[*].@type",
            ExpectTypeEnum.EXPECTED_STR,
            "",
            "",
            422,
            null);
    String expected1 = "$.body.quoteItem[0].product.place[0].@type";
    Pair<String, PathCheck> pair1 = Pair.of(expected1, pathCheck1);

    PathCheck pathCheck2 =
        new PathCheck("EXPECTED_STR", null, ExpectTypeEnum.EXPECTED_STR, "", "", 422, null);
    Pair<String, PathCheck> pair2 = Pair.of(null, pathCheck2);

    PathCheck pathCheck3 =
        new PathCheck("EXPECTED_STR", "", ExpectTypeEnum.EXPECTED_STR, "", "", 422, null);
    Pair<String, PathCheck> pair3 = Pair.of("", pathCheck3);
    return List.of(pair1, pair2, pair3);
  }

  @Test
  void givenNotNumerical_whenValidatingConstantNumber_thenThrowsException() {
    ComponentAPITargetFacets.Mapper mapper = new ComponentAPITargetFacets.Mapper();
    mapper.setSourceType(MappingTypeEnum.DISCRETE_INT.getKind());
    mapper.setTarget("1");
    Assertions.assertThrowsExactly(
        KrakenException.class,
        () ->
            mappingMatrixCheckerActionRunner.validateConstantNumber(
                "4", mapper, MappingTypeEnum.DISCRETE_INT.getKind()));
  }

  @SneakyThrows
  @Test
  void givenPathCheck_whenCheckMatrixConstraintsFailed_thenThrowsException() {
    String targetKey = "mef.sonata.api-target.address.validate";
    String filePath =
        "deployment-config/components/mapping-matrix/mapping.matrix.address.validation.yaml";
    Optional<UnifiedAsset> unifiedAsset =
        YamlToolkit.parseYaml(readFileToString(filePath), UnifiedAsset.class);
    UnifiedAsset targetAsset = unifiedAsset.get();
    Map<String, List<PathCheck>> facets =
        JsonToolkit.fromJson(
            JsonToolkit.toJson(targetAsset.getFacets().get("matrix")),
            new TypeReference<Map<String, List<PathCheck>>>() {});
    String requestDataPath = "mockData/addressValidationRequest.json";
    Map<String, Object> requestData =
        JsonToolkit.fromJson(
            readFileToString(requestDataPath), new TypeReference<Map<String, Object>>() {});
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("body", requestData);
    Assertions.assertThrowsExactly(
        KrakenException.class,
        () ->
            mappingMatrixCheckerActionRunner.checkMatrixConstraints(
                facets, targetKey, requestBody, List.of()));
  }

  @SneakyThrows
  @Test
  void givenPathCheck_whenCheckMatrixConstraintsPassed_thenReturnOK() {
    String targetKey = "mef.sonata.api-target.address.validate";
    String filePath =
        "deployment-config/components/mapping-matrix/mapping.matrix.address.validation.enable.yaml";
    String requestDataPath = "mockData/addressValidationRequest.json";
    Map<String, Object> requestData =
        JsonToolkit.fromJson(
            readFileToString(requestDataPath), new TypeReference<Map<String, Object>>() {});
    Optional<UnifiedAsset> unifiedAsset =
        YamlToolkit.parseYaml(readFileToString(filePath), UnifiedAsset.class);
    unifiedAsset.ifPresent(
        targetAsset -> {
          Map<String, List<PathCheck>> facets =
              JsonToolkit.fromJson(
                  JsonToolkit.toJson(targetAsset.getFacets().get("matrix")),
                  new TypeReference<Map<String, List<PathCheck>>>() {});
          Map<String, Object> requestBody = new HashMap<>();
          requestBody.put("body", requestData);
          Assertions.assertDoesNotThrow(
              () ->
                  mappingMatrixCheckerActionRunner.checkMatrixConstraints(
                      facets, targetKey, requestBody, List.of()));
        });
  }

  @Test
  void givenNotMatchedSourceConditions_whenChecking_thenNoException() {
    Map<String, Object> inputs = new HashMap<>();
    Map<String, Object> body = new HashMap<>();
    inputs.put("body", body);
    body.put("a1", "roll3");
    body.put("a2", "roll1");
    body.put("a3", "roll2");
    List<String> pathsExpected422 = List.of();
    DocumentContext documentContext = JsonPath.parse(inputs);
    ComponentAPITargetFacets.Mapper mapper = new ComponentAPITargetFacets.Mapper();
    mapper.setSource("@{{a1}}");
    mapper.setSourceConditions(getSourceConditions());
    Assertions.assertDoesNotThrow(
        () ->
            mappingMatrixCheckerActionRunner.checkEnumValue(
                documentContext, mapper, inputs, pathsExpected422));

    Assertions.assertDoesNotThrow(
        () ->
            mappingMatrixCheckerActionRunner.checkConstantValue(
                documentContext, mapper, inputs, pathsExpected422));

    mapper.setSourceLocation("BODY");
    Assertions.assertDoesNotThrow(
        () ->
            mappingMatrixCheckerActionRunner.checkMappingValue(
                documentContext, mapper, inputs, pathsExpected422));
  }

  @Test
  void givenSourceDependOnExpression_whenEvaluate_thenReturnTrue() {
    Map<String, Object> inputs = new HashMap<>();
    Map<String, Object> body = new HashMap<>();
    body.put("a1", "roll1");
    body.put("a2", "roll2");
    body.put("a3", "roll3");
    inputs.put("body", body);

    List<ComponentAPITargetFacets.SourceCondition> sourceConditions = getSourceConditions();
    boolean dependOn =
        mappingMatrixCheckerActionRunner.checkConditionsMatched(inputs, sourceConditions);
    Assertions.assertTrue(dependOn);

    body.put("a1", "roll");
    inputs.put("body", body);
    dependOn = mappingMatrixCheckerActionRunner.checkConditionsMatched(inputs, sourceConditions);
    Assertions.assertFalse(dependOn);
  }

  @Test
  @SneakyThrows
  void givenWorkflowMapper_thenValidate_thenThrowException() {
    Map<String, Object> inputs = new HashMap<>();
    inputs.put(
        "body",
        JsonToolkit.fromJson(readFileToString("mockData/delete.order.eline.json"), Object.class));
    List<String> emptyList = Collections.emptyList();
    Assertions.assertThrows(
        KrakenException.class,
        () ->
            mappingMatrixCheckerActionRunner.checkMapperConstraints(
                "mef.sonata.api-target.order.eline.delete", inputs, emptyList));
  }

  private static List<ComponentAPITargetFacets.SourceCondition> getSourceConditions() {
    ComponentAPITargetFacets.SourceCondition sourceCondition1 =
        new ComponentAPITargetFacets.SourceCondition();
    sourceCondition1.setKey("@{{a1}}");
    sourceCondition1.setOperator("eq");
    sourceCondition1.setVal("roll1");

    ComponentAPITargetFacets.SourceCondition sourceCondition2 =
        new ComponentAPITargetFacets.SourceCondition();
    sourceCondition2.setKey("@{{a2}}");
    sourceCondition2.setOperator("eq");
    sourceCondition2.setVal("roll2");

    ComponentAPITargetFacets.SourceCondition sourceCondition3 =
        new ComponentAPITargetFacets.SourceCondition();
    sourceCondition3.setKey("@{{a3}}");
    sourceCondition3.setOperator("eq");
    sourceCondition3.setVal("roll3");

    return List.of(sourceCondition1, sourceCondition2, sourceCondition3);
  }

  @Test
  @SneakyThrows
  void givenMatrixKey_whenChecking_thenReturnOK() {
    Map<String, Object> inputs = new HashMap<>();
    inputs.put("targetKey", "mef.sonata.api-target.quote.eline.modify.sync");
    inputs.put("mappingMatrixKey", "mef.sonata.api.matrix.quote.eline.modify.sync");
    inputs.put(
        "body",
        JsonToolkit.fromJson(
            readFileToString("mockData/quote.eline.modify.request.json"), Object.class));
    addValidHttpRequest();
    Assertions.assertDoesNotThrow(() -> mappingMatrixCheckerActionRunner.onCheck(inputs));
  }

  @Test
  @SneakyThrows
  void givenEmptyRules_whenCheckModification_thenNoException() {
    Map<String, Object> inputs = new HashMap<>();
    inputs.put(
        "body",
        JsonToolkit.fromJson(readFileToString("mockData/productOrderRequest.json"), Object.class));
    List<FilterRule> filterRules = new ArrayList<>();
    String targetKey = "mef.sonata.api-target.quote.eline.modify.sync";
    Assertions.assertDoesNotThrow(
        () ->
            mappingMatrixCheckerActionRunner.checkModifyConstraints(
                filterRules, targetKey, inputs));
  }

  @Test
  @SneakyThrows
  void givenEmptyInstanceId_whenCheckModification_thenThrowsException() {
    Map<String, Object> inputs = new HashMap<>();
    inputs.put(
        "body",
        JsonToolkit.fromJson(
            readFileToString("mockData/quote.eline.modify.request.no.instance.id.json"),
            Object.class));
    List<FilterRule> filterRules = new ArrayList<>();
    FilterRule filterRule = new FilterRule();
    filterRule.setQueryPath("$.body.quoteItem[0].product.id");
    filterRules.add(filterRule);
    String targetKey = "mef.sonata.api-target.quote.eline.modify.sync";
    Assertions.assertThrows(
        KrakenException.class,
        () ->
            mappingMatrixCheckerActionRunner.checkModifyConstraints(
                filterRules, targetKey, inputs));
  }

  @Test
  @SneakyThrows
  void givenNotExistedInstanceId_whenCheckModification_thenThrowsException() {
    Map<String, Object> inputs = new HashMap<>();
    inputs.put(
        "body",
        JsonToolkit.fromJson(
            readFileToString("mockData/quote.eline.modify.request.json"), Object.class));
    List<FilterRule> filterRules = new ArrayList<>();
    FilterRule filterRule = new FilterRule();
    filterRule.setQueryPath("$.body.quoteItem[0].product.place[0].id");
    filterRules.add(filterRule);
    String targetKey = "mef.sonata.api-target.quote.eline.modify.sync";
    Assertions.assertThrows(
        KrakenException.class,
        () ->
            mappingMatrixCheckerActionRunner.checkModifyConstraints(
                filterRules, targetKey, inputs));
  }

  @Test
  @SneakyThrows
  void givenValidBody_whenCheckModification_thenNoException() {
    Map<String, Object> inputs = new HashMap<>();
    inputs.put(
        "body",
        JsonToolkit.fromJson(
            readFileToString("mockData/quote.eline.modify.request.json"), Object.class));
    List<FilterRule> filterRules = buildValidFilterRules();
    String targetKey = "mef.sonata.api-target.quote.eline.modify.sync";
    addValidHttpRequest();
    Assertions.assertDoesNotThrow(
        () ->
            mappingMatrixCheckerActionRunner.checkModifyConstraints(
                filterRules, targetKey, inputs));
  }

  @SneakyThrows
  private void addValidHttpRequest() {
    HttpRequestEntity entity = new HttpRequestEntity();
    entity.setId(UUID.randomUUID());
    String s = readFileToString("mockData/productOrderRequest.json");
    entity.setRequest(s);
    entity.setMethod("POST");
    entity.setRequestId(UUID.randomUUID().toString());
    entity.setPath("/mefApi/sonata/productOrderingManagement/v10/productOrder");
    entity.setUri("localhost");
    entity.setBizType("ACCESS_E_LINE");
    entity.setProductInstanceId("67aeadfa8a05cce9385c1497");
    httpRequestRepository.save(entity);
  }

  @Test
  @SneakyThrows
  void givenInvalidBody_whenCheckModification_thenThrowsException() {
    Map<String, Object> inputs = new HashMap<>();
    inputs.put(
        "body",
        JsonToolkit.fromJson(
            readFileToString("mockData/quote.eline.modify.invalid.request.json"), Object.class));
    List<FilterRule> filterRules = buildValidFilterRules();
    String targetKey = "mef.sonata.api-target.quote.eline.modify.sync";
    addValidHttpRequest();
    Assertions.assertThrows(
        KrakenException.class,
        () ->
            mappingMatrixCheckerActionRunner.checkModifyConstraints(
                filterRules, targetKey, inputs));
  }
}
