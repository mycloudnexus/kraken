package com.consoleconnect.kraken.operator.core.toolkit;

import static org.assertj.core.api.Assertions.assertThat;

import com.consoleconnect.kraken.operator.core.enums.ExpectTypeEnum;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@Slf4j
class ConstructExpressionUtilTest {

  @Test
  void givenExpression_whenConvert_thenResponseOK() {
    String expression = "@{{param}}";
    List<String> params = ConstructExpressionUtil.extractMapperParam(expression);
    assertThat(params).hasSize(1);
    String s =
        ConstructExpressionUtil.constructBody(
            ConstructExpressionUtil.replaceStarToZero("@{{param[*]}}"));
    assertThat(s).isEqualTo("${body.param[0]}");
    String s1 = ConstructExpressionUtil.constructDBParam(expression);
    assertThat(s1).contains("response");
    String s2 = ConstructExpressionUtil.constructQuery(expression);
    assertThat(s2).contains("query");
    String s3 = ConstructExpressionUtil.constructMeRequestBody(expression);
    assertThat(s3).contains("body");
    List<String> pathParam = ConstructExpressionUtil.extractOriginalPathParam("/{path}/a/b/c");
    assertThat(pathParam).contains("path");
    String s4 = ConstructExpressionUtil.convertToJsonPointer("@{{a[*].b.c[1]}}");
    String s5 = ConstructExpressionUtil.constructOriginalDBParam("abc");
    assertThat(s5).contains("entity");
    assertThat(s4).doesNotContain("*");
    ExpectTypeEnum expected = ExpectTypeEnum.EXPECTED;
    ExpectTypeEnum expectedExist = ExpectTypeEnum.EXPECTED_EXIST;
    ExpectTypeEnum expectedTrue = ExpectTypeEnum.EXPECTED_TRUE;
    log.info(
        "expected: {}, expectedExist: {}, expectedTrue: {}", expected, expectedExist, expectedTrue);
  }

  @Test
  void givenStatusInArray_whenConvert_thenOK() {
    String target = "@{{[*].status}}";
    String result = ConstructExpressionUtil.convertToJsonPointer(target);
    String expected = "/status";
    Assertions.assertEquals(expected, result);
  }
}
