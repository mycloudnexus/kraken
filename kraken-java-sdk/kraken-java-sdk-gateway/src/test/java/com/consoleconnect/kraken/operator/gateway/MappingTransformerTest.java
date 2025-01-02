package com.consoleconnect.kraken.operator.gateway;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;

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
  void givenJson_whenDeleteNodeByPath_thenDeleteNodeSuccess() {
    Map<String, String> checkPathMap = new HashMap<>();
    String input =
        "{\"state\":\"completed1\",\"completionDate\":\"123\",\"productOrderItem\":[{\"state\":\"completed\",\"completionDate\":\"123\"}]}\n";
    checkPathMap.put("$[?(@.state == 'completed')]", "$.completionDate");
    checkPathMap.put(
        "$.productOrderItem[?(@.state == 'completed')]",
        "$.productOrderItem[?(@.state != 'completed')].completionDate");
    checkPathMap.put("$.notFound", "$.notFound");
    String s = deleteNodeByPath(checkPathMap, input);
    assertThat(s, hasJsonPath("$.productOrderItem[0].completionDate"), notNullValue());
  }
}
