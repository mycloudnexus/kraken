package com.consoleconnect.kraken.operator.gateway.runner;

import static com.consoleconnect.kraken.operator.gateway.runner.ResponseCodeTransform.TARGET_KEY_NOT_FOUND;

import com.consoleconnect.kraken.operator.core.enums.ExpectTypeEnum;
import com.consoleconnect.kraken.operator.core.enums.MappingTypeEnum;
import com.consoleconnect.kraken.operator.core.exception.KrakenException;
import com.consoleconnect.kraken.operator.core.model.UnifiedAsset;
import com.consoleconnect.kraken.operator.core.model.facet.ComponentAPITargetFacets;
import com.consoleconnect.kraken.operator.core.toolkit.JsonToolkit;
import com.consoleconnect.kraken.operator.core.toolkit.YamlToolkit;
import com.consoleconnect.kraken.operator.gateway.CustomConfig;
import com.consoleconnect.kraken.operator.gateway.dto.PathCheck;
import com.consoleconnect.kraken.operator.test.AbstractIntegrationTest;
import com.consoleconnect.kraken.operator.test.MockIntegrationTest;
import com.fasterxml.jackson.core.type.TypeReference;
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
import org.springframework.test.context.ContextConfiguration;

@MockIntegrationTest
@ContextConfiguration(classes = CustomConfig.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class MappingMatrixCheckerActionRunnerTest extends AbstractIntegrationTest {
  @Autowired MappingMatrixCheckerActionRunner mappingMatrixCheckerActionRunner;

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
  @Order(3)
  @SneakyThrows
  void givenParamMissing_whenOnCheck_thenReturnException() {
    Map<String, Object> inputs = new HashMap<>();
    String s = readFileToString("/mockData/addressValidationRequest.json");
    inputs.put("body", JsonToolkit.fromJson(s, Object.class));
    inputs.put("targetKey", "mef.sonata.api-target.address.validate");
    inputs.put("mappingMatrixKey", "mef.sonata.api.matrix.address.validation.enable");
    KrakenException krakenException =
        Assertions.assertThrowsExactly(
            KrakenException.class, () -> mappingMatrixCheckerActionRunner.onCheck(inputs));
    MatcherAssert.assertThat(krakenException.getMessage(), Matchers.containsString("422"));
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
    MatcherAssert.assertThat(
        krakenException.getCause().getMessage(), Matchers.containsString(matchedMsg));
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
        new PathCheck("EXPECTED", "user", ExpectTypeEnum.EXPECTED, "true", "", null, null);
    PathCheck pathCheck2 =
        new PathCheck(
            "EXPECTED_EXIST", "user", ExpectTypeEnum.EXPECTED_EXIST, "true", "", null, null);
    PathCheck pathCheck3 =
        new PathCheck(
            "EXPECTED_TRUE",
            "$.body.submittedGeographicAddress.['country']",
            ExpectTypeEnum.EXPECTED_TRUE,
            "${param}",
            "",
            422,
            null);
    PathCheck pathCheck4 =
        new PathCheck("EXPECTED_STR", "", ExpectTypeEnum.EXPECTED_STR, "", "", 422, null);
    PathCheck pathCheck5 =
        new PathCheck("EXPECTED_INT", "", ExpectTypeEnum.EXPECTED_INT, "", "", 422, null);
    PathCheck pathCheck6 =
        new PathCheck("EXPECTED_NUMERIC", "", ExpectTypeEnum.EXPECTED_NUMERIC, "", "", 422, null);
    PathCheck pathCheck7 =
        new PathCheck(
            "EXPECTED_NOT_BLANK", "", ExpectTypeEnum.EXPECTED_NOT_BLANK, "", "", 422, null);

    Pair<PathCheck, Object> pair1 = Pair.of(pathCheck1, "true");
    Pair<PathCheck, Object> pair2 = Pair.of(pathCheck2, null);
    Pair<PathCheck, Object> pair3 = Pair.of(pathCheck3, "AU");
    Pair<PathCheck, Object> pair4 = Pair.of(pathCheck4, "string here");
    Pair<PathCheck, Object> pair5 = Pair.of(pathCheck5, 123);
    Pair<PathCheck, Object> pair6 = Pair.of(pathCheck6, 123.4);
    Pair<PathCheck, Object> pair7 = Pair.of(pathCheck7, "not blank");

    return List.of(pair1, pair2, pair3, pair4, pair5, pair6, pair7);
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
                123, "x", MappingTypeEnum.DISCRETE_STR.getKind()));
  }

  @Test
  void givenStringValueList_whenValueNotInDiscreteStr_thenThrowsException() {
    Assertions.assertThrowsExactly(
        KrakenException.class,
        () ->
            mappingMatrixCheckerActionRunner.validateEnumOrDiscreteString(
                "4", "x", List.of("1", "2", "3"), MappingTypeEnum.DISCRETE_STR.getKind()));
  }

  @Test
  void givenNotInteger_whenValidatingDiscreteInt_thenThrowsException() {
    Assertions.assertThrowsExactly(
        KrakenException.class,
        () ->
            mappingMatrixCheckerActionRunner.validateDiscreteInteger(
                "4", "x", List.of(), MappingTypeEnum.DISCRETE_INT.getKind()));
  }

  @Test
  void givenIntegerNotIn_whenValidatingDiscreteInt_thenThrowsException() {
    Assertions.assertThrowsExactly(
        KrakenException.class,
        () ->
            mappingMatrixCheckerActionRunner.validateDiscreteInteger(
                4, "x", List.of("1", "2", "3"), MappingTypeEnum.DISCRETE_INT.getKind()));
  }

  @Test
  void givenNotInteger_whenValidatingContinuousInt_thenThrowsException() {
    Assertions.assertThrowsExactly(
        KrakenException.class,
        () ->
            mappingMatrixCheckerActionRunner.validateContinuousInteger(
                "4", "x", List.of(), MappingTypeEnum.CONTINUOUS_INT.getKind()));
  }

  @Test
  void givenIntegerNotIn_whenValidatingContinuousInt_thenThrowsException() {
    Assertions.assertThrowsExactly(
        KrakenException.class,
        () ->
            mappingMatrixCheckerActionRunner.validateContinuousInteger(
                4, "x", List.of("1", "3"), MappingTypeEnum.CONTINUOUS_INT.getKind()));
  }

  @Test
  void givenNotDouble_whenValidatingContinuousDouble_thenThrowsException() {
    Assertions.assertThrowsExactly(
        KrakenException.class,
        () ->
            mappingMatrixCheckerActionRunner.validateContinuousDouble(
                "4", "x", List.of(), MappingTypeEnum.CONTINUOUS_DOUBLE.getKind()));
  }

  @Test
  void givenDoubleNotIn_whenValidatingContinuousDouble_thenThrowsException() {
    Assertions.assertThrowsExactly(
        KrakenException.class,
        () ->
            mappingMatrixCheckerActionRunner.validateContinuousDouble(
                4.0, "x", List.of("1.0", "3.9"), MappingTypeEnum.CONTINUOUS_DOUBLE.getKind()));
  }

  @Test
  void givenNotNumerical_whenValidatingConstantNumber_thenThrowsException() {
    ComponentAPITargetFacets.Mapper mapper = new ComponentAPITargetFacets.Mapper();
    mapper.setSourceType(MappingTypeEnum.CONSTANT_NUM.getKind());
    Assertions.assertThrowsExactly(
        KrakenException.class,
        () ->
            mappingMatrixCheckerActionRunner.validateConstantNumber(
                "4", mapper, MappingTypeEnum.CONSTANT_NUM.getKind()));
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
                facets, targetKey, requestBody));
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
                      facets, targetKey, requestBody));
        });
  }
}
