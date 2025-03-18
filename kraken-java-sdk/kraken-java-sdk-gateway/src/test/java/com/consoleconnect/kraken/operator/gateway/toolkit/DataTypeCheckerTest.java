package com.consoleconnect.kraken.operator.gateway.toolkit;

import com.consoleconnect.kraken.operator.gateway.CustomConfig;
import com.consoleconnect.kraken.operator.gateway.dto.PathCheck;
import com.consoleconnect.kraken.operator.gateway.runner.DataTypeChecker;
import com.consoleconnect.kraken.operator.test.AbstractIntegrationTest;
import com.consoleconnect.kraken.operator.test.MockIntegrationTest;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.test.context.ContextConfiguration;

@MockIntegrationTest
@ContextConfiguration(classes = CustomConfig.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DataTypeCheckerTest extends AbstractIntegrationTest {

  @SneakyThrows
  @Test
  void givenRequest_whenFilter_thenReturnTrue() {
    DataTypeChecker checker =
        new DataTypeChecker() {
          @Override
          public void throwException(PathCheck pathCheck, String defaultMsg) {}
        };
    String request = readFileToString("mockData/productOrderRequest.json");
    boolean result = checker.filterRequest(request);
    Assertions.assertTrue(result);
  }
}
