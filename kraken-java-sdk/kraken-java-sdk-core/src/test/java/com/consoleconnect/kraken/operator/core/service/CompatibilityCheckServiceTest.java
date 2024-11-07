package com.consoleconnect.kraken.operator.core.service;

import com.consoleconnect.kraken.operator.core.CustomConfig;
import com.consoleconnect.kraken.operator.test.AbstractIntegrationTest;
import com.consoleconnect.kraken.operator.test.MockIntegrationTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@MockIntegrationTest
@ContextConfiguration(classes = CustomConfig.class)
class CompatibilityCheckServiceTest extends AbstractIntegrationTest {
  @Autowired CompatibilityCheckService compatibilityCheckService;

  @Test
  @Order(1)
  void givenAppVersionAndBlankProductVersion_whenCheck_thenSuccess() {
    Assertions.assertTrue(compatibilityCheckService.check("1.2.0", null));
  }

  @Test
  @Order(2)
  void givenAppVersionAndProductVersion_whenCheck_thenSuccess() {
    Assertions.assertTrue(compatibilityCheckService.check("1.2.0", "V1.4.1"));
  }

  @Test
  @Order(3)
  void givenAppVersionAndInCompatibleProductVersion_whenCheck_thenFalse() {
    Assertions.assertFalse(compatibilityCheckService.check("1.2.0", "V1.5.1"));
  }
}
