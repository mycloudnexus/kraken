package com.consoleconnect.kraken.operator.gateway;

import com.consoleconnect.kraken.operator.gateway.runner.MappingTransformer;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class MappingTransformerTest implements MappingTransformer {

  @Test
  void givenJsonInput_whenDeleteNode_thenReturnOK() {
    Map<String, String> checkPathMap = new HashMap<>();
    checkPathMap.put("$.key1", "$.key1");
    checkPathMap.put("$.key2", "$.key2");
    checkPathMap.put("$.key3", "$.key3");
    String input = "{\"key1\":\"\",\"key2\":0,\"key3\":false,\"key\":\"hello kraken\"}";
    String result = deleteNodeByPath(checkPathMap, input);
    Assertions.assertEquals("{\"key\":\"hello kraken\"}", result);
  }

  @Test
  void givenJsonArray_whenCounting_thenReturnOK() {
    String pathExpression = "$.body.quoteItem[*].requestedQuoteItemTerm.duration.units";
    String jsonData =
        "{\"body\":{\"quoteItem\":[{\"requestedQuoteItemTerm\":{\"duration\":{\"amount\":1,\"units\":\"calendarMonths\"}}}]}}";
    int result = lengthOfArrayNode(pathExpression, jsonData);
    Assertions.assertTrue(result > -1);
  }
}
