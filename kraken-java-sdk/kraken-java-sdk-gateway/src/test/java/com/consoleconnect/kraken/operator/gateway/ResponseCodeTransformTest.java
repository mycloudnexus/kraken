package com.consoleconnect.kraken.operator.gateway;

import static org.junit.jupiter.api.Assertions.assertThrows;

import com.consoleconnect.kraken.operator.core.exception.KrakenException;
import com.consoleconnect.kraken.operator.gateway.runner.ResponseCodeTransform;
import org.junit.jupiter.api.Test;

class ResponseCodeTransformTest {
  @Test
  void testResponseCodeTransform() {
    ResponseCodeTransform responseCodeTransform = new ResponseCodeTransform() {};

    String res =
        """
                {
                "result":"targetKey:notFound"
                }
                """;
    assertThrows(KrakenException.class, () -> responseCodeTransform.checkOutputKey(res));
  }
}
