package com.consoleconnect.kraken.operator.core.toolkit;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@Slf4j
class JsonPathTests {
  @SneakyThrows
  @Test
  void givenJsonPath_whenGenerate_returnJson() {
    String s = JsonToolkit.generateJson("/user/class/0/name", "John", "{}");
    String s2 = JsonToolkit.generateJson("/user/class/0/password", "password", s);
    String s3 = JsonToolkit.generateJson("/user/class/0/secret/key", "secretKey", s2);
    log.info("result: {}", s3);
    assertThat(s3, hasJsonPath("$.user.class[0].secret.key", equalTo("secretKey")));
  }

  @SneakyThrows
  @Test
  void givenJsonArray_whenGenerate_thenReturnOK() {
    String s = JsonToolkit.generateJson("/id", "${responseBody.createdPortId}", "[{\"id\":\"\"}]");
    String actual = compactJson(s);
    String expected = "[{\"id\":\"${responseBody.createdPortId}\"}]";
    Assertions.assertEquals(expected, actual);
  }

  public String compactJson(String jsonString) throws JsonProcessingException {
    ObjectMapper mapper = new ObjectMapper();
    Object jsonObject = mapper.readValue(jsonString, Object.class);
    return mapper.writeValueAsString(jsonObject);
  }
}
