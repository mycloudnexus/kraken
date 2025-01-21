package com.consoleconnect.kraken.operator.gateway.service;

import com.consoleconnect.kraken.operator.test.AbstractIntegrationTest;
import com.consoleconnect.kraken.operator.test.MockIntegrationTest;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@MockIntegrationTest
class FilterHeaderServiceTest extends AbstractIntegrationTest {

  @Autowired FilterHeaderService filterHeaderService;

  @Test
  void testFilterHeaders() {
    Map<String, String> result =
        filterHeaderService.filterHeaders(
            Map.of(
                "k",
                "v",
                "token",
                "token",
                "Authorization",
                "token",
                "authorization",
                "token",
                "x-sonata-buyer-key",
                "key"));
    Assertions.assertTrue(result.containsKey("k"));
    Assertions.assertFalse(result.containsKey("token"));
    Assertions.assertFalse(result.containsKey("Authorization"));
    Assertions.assertFalse(result.containsKey("authorization"));
    Assertions.assertFalse(result.containsKey("x-sonata-buyer-key"));

    result = filterHeaderService.filterHeaders(null);
    Assertions.assertEquals(0, result.size());
  }
}
