package com.consoleconnect.kraken.operator.core;

import com.consoleconnect.kraken.operator.core.exception.ErrorResponse;
import java.util.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ErrorMappingTest {

  @Test
  void test400Mapping() {
    int code = 400;
    String message = "API mapping is incomplete";
    String result = ErrorResponse.ErrorMapping.defaultMsg(code, message);
    Assertions.assertEquals("incompleteMapping", result);
  }

  @Test
  void testDefaultMsg() {
    List<Integer> list = List.of(400, 404, 401, 403, 422, 500);
    List<String> result =
        list.stream().map(item -> ErrorResponse.ErrorMapping.defaultMsg(item, "")).toList();
    Assertions.assertEquals(6, result.size());
  }
}
