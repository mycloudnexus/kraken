package com.consoleconnect.kraken.operator.core.toolkit;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class StringUtilsTest {
  @Test
  void givenValidJsonString_whenConvert_returnJson() {
    Assertions.assertEquals("{}", StringUtils.convertToJsonSafeString("{}"));
    Assertions.assertEquals("[]", StringUtils.convertToJsonSafeString("[]"));
    Assertions.assertEquals(
        "{\"firstKey\": \"v\"}", StringUtils.convertToJsonSafeString("{\"firstKey\": \"v\"}"));
    Assertions.assertEquals(
        "{\"first_key\": \"v\"}", StringUtils.convertToJsonSafeString("{\"first_key\": \"v\"}"));
    Assertions.assertEquals(
        "[{\"firstKey\": \"v\"}]", StringUtils.convertToJsonSafeString("[{\"firstKey\": \"v\"}]"));
    Assertions.assertEquals("\"abc\"", StringUtils.convertToJsonSafeString("abc"));
  }

  @Test
  void givenInvalidJsonString_whenConvert_returnBlankOrRawString() {
    Assertions.assertNull(StringUtils.convertToJsonSafeString(null));
    Assertions.assertEquals("", StringUtils.convertToJsonSafeString(""));
    Assertions.assertEquals(
        "\"invalid json <<\"", StringUtils.convertToJsonSafeString("invalid json <<"));
  }
}
