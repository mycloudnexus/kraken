package com.consoleconnect.kraken.operator.gateway.runner;

import static com.consoleconnect.kraken.operator.gateway.runner.ResponseCodeTransform.TARGET_KEY_NOT_FOUND;

import com.consoleconnect.kraken.operator.core.enums.ExpectTypeEnum;
import com.consoleconnect.kraken.operator.core.exception.KrakenException;
import com.consoleconnect.kraken.operator.core.toolkit.JsonToolkit;
import com.consoleconnect.kraken.operator.gateway.CustomConfig;
import com.consoleconnect.kraken.operator.test.AbstractIntegrationTest;
import com.consoleconnect.kraken.operator.test.MockIntegrationTest;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import lombok.SneakyThrows;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.*;
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
        krakenException.getMessage(), Matchers.containsString("possibly product not supported"));
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
            lack in check rules for target key"""));
  }

  @Test
  @Order(3)
  void givenTargetKeyNoRuls_whenOnCheck_thenReturnException() {
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
  void givenErrorMsgInMatrixItem_whenChecking_thenThrowsExceptionMessageAsExpected() {
    validateQuoteRequest("/mockData/quoteWithInstantSyncFalse.json", "instantSyncQuote==false");
  }

  private void validateOrderRequest(String request, String matchedMsg) throws IOException {
    Map<String, Object> inputs = new HashMap<>();
    inputs.put("query", Map.of("buyerId", "test-company"));
    inputs.put("body", JsonToolkit.fromJson(readFileToString(request), Object.class));
    inputs.put("targetKey", "mef.sonata.api-target.order.eline.add");
    inputs.put("mappingMatrixKey", "mef.sonata.api.matrix.order");
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

  @Test
  @Order(6)
  void givenCheckPath_whenCheckExpect_thenReturnException() {
    MappingMatrixCheckerActionRunner.PathCheck pathCheck =
        new MappingMatrixCheckerActionRunner.PathCheck(
            "expect", "user", ExpectTypeEnum.EXPECTED, "user", "error", null);
    Assertions.assertFalse(mappingMatrixCheckerActionRunner.checkExpect(pathCheck, "user1"));
    MappingMatrixCheckerActionRunner.PathCheck pathCheck1 =
        new MappingMatrixCheckerActionRunner.PathCheck(
            "expect", "user", ExpectTypeEnum.EXPECTED_EXIST, "user", "error", null);
    Assertions.assertThrowsExactly(
        KrakenException.class,
        () -> mappingMatrixCheckerActionRunner.checkExpect(pathCheck1, "user1"));
    MappingMatrixCheckerActionRunner.PathCheck pathCheck2 =
        new MappingMatrixCheckerActionRunner.PathCheck(
            "expect", "user", ExpectTypeEnum.EXPECTED_TRUE, "${param.id}", "error", 400);
    Assertions.assertThrowsExactly(
        KrakenException.class,
        () -> mappingMatrixCheckerActionRunner.checkExpect(pathCheck2, "user1"));
  }
}
