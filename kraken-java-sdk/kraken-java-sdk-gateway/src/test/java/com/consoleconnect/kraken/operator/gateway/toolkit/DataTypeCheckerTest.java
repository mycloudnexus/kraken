package com.consoleconnect.kraken.operator.gateway.toolkit;

import com.consoleconnect.kraken.operator.core.model.FilterRule;
import com.consoleconnect.kraken.operator.gateway.CustomConfig;
import com.consoleconnect.kraken.operator.gateway.FilterRulesCreator;
import com.consoleconnect.kraken.operator.gateway.dto.PathCheck;
import com.consoleconnect.kraken.operator.gateway.runner.DataTypeChecker;
import com.consoleconnect.kraken.operator.test.AbstractIntegrationTest;
import com.consoleconnect.kraken.operator.test.MockIntegrationTest;
import java.util.List;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.test.context.ContextConfiguration;

@MockIntegrationTest
@ContextConfiguration(classes = CustomConfig.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DataTypeCheckerTest extends AbstractIntegrationTest implements FilterRulesCreator {

  @SneakyThrows
  @Test
  void givenRequest_whenFilter_thenReturnTrue() {
    DataTypeChecker checker =
        new DataTypeChecker() {
          @Override
          public void throwException(PathCheck pathCheck, String defaultMsg) {}
        };

    List<FilterRule> filterRules = buildValidFilterRules();
    FilterRule filterRule = filterRules.get(0);
    String request = readFileToString("mockData/productOrderRequest.json");
    boolean result = checker.filterRequest(request, filterRule);
    Assertions.assertTrue(result);
  }
}
