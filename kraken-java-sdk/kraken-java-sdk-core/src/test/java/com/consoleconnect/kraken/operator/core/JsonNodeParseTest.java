package com.consoleconnect.kraken.operator.core;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.consoleconnect.kraken.operator.core.toolkit.JsonToolkit;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class JsonNodeParseTest {

  @Test
  void testJsonNodeParse() {
    String input =
        "{\"productOrderItem\":[{\"id\":\"123\",\"product\":{\"id\":\"456\",\"productConfiguration\":{\"@type\":\"UNI\"}}}]}";
    testParse(input);
  }

  private void testParse(String source) {
    String convertedPath = "$['productOrderItem'][0]['product']['productConfiguration']['@type']";
    String result = JsonToolkit.parseValue(convertedPath, source);
    System.out.println("result:" + result);
    Assertions.assertNotNull(result);
  }

  @Test
  void test_parse() {
    String json =
        "{\"data\":[{\"id\":\"ba3a84e5-03d3-477f-a98c-c92885c28e4f\",\"title\":\"Product Ordering Management\",\"content\":\"openapi: 3.0.1\\ninfo:\\n  description:\\n    \\\"**This charset=utf-8:\\n              schema:\\n                type: array\\n                 string\\n          description:\\n            Unique identifier for the Cancel Product Order that is generated\\n            by the Seller when the Cancel Product Order request `state` is .net/example/token\\n      type: oauth2\\n\"}],\"total\":1,\"page\":0,\"size\":20}";
    assertThat(json, hasJsonPath("$.data[0].title", is("Product Ordering Management")));
  }
}
