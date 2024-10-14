package com.consoleconnect.kraken.operator.gateway.runner;

import static com.consoleconnect.kraken.operator.gateway.runner.ResponseCodeTransform.TARGET_KEY_NOT_FOUND;

import com.consoleconnect.kraken.operator.core.exception.KrakenException;
import com.consoleconnect.kraken.operator.core.toolkit.JsonToolkit;
import com.consoleconnect.kraken.operator.gateway.CustomConfig;
import com.consoleconnect.kraken.operator.test.AbstractIntegrationTest;
import com.consoleconnect.kraken.operator.test.MockIntegrationTest;
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
    MatcherAssert.assertThat(
        krakenException.getMessage(), Matchers.containsString("can not be null"));
  }
}
