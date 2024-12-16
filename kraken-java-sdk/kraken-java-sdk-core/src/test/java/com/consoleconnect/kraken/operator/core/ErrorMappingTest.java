package com.consoleconnect.kraken.operator.core;

import com.consoleconnect.kraken.operator.core.exception.ErrorResponse;
import com.consoleconnect.kraken.operator.core.exception.KrakenException;
import java.util.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class ErrorMappingTest {

  @Test
  void test400Mapping() {
    int code = 400;
    String message = "API mapping is incomplete";
    String result =
        ErrorResponse.ErrorMapping.defaultMsg(code, new IllegalArgumentException(message));
    Assertions.assertEquals("incompleteMapping", result);
  }

  @Test
  void testDefaultMsg() {
    List<Integer> list = List.of(400, 404, 401, 403, 422, 500);
    List<String> result =
        list.stream()
            .map(
                item ->
                    ErrorResponse.ErrorMapping.defaultMsg(item, new IllegalArgumentException("")))
            .toList();
    Assertions.assertEquals(6, result.size());
  }

  public static String[] messageArray() {
    return new String[] {"invalidValue", "missingProperty", "invalidFormat"};
  }

  @ParameterizedTest
  @MethodSource(value = "messageArray")
  void given422Exception_whenRouting_thenReturnOK(String message) {
    KrakenException exception =
        new KrakenException(
            422, "422 UnProcessable Entity," + message, new IllegalArgumentException(message));
    String result = ErrorResponse.ErrorMapping.process422(exception);
    Assertions.assertEquals(message, result);
  }
}
