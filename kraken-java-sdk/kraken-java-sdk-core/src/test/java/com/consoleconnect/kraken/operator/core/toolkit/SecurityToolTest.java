package com.consoleconnect.kraken.operator.core.toolkit;

import com.consoleconnect.kraken.operator.core.exception.KrakenException;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

@Slf4j
class SecurityToolTest {

  @ParameterizedTest
  @MethodSource(value = "buildIllegalExpressions")
  void testNegativeExpression(String expression) {
    Assertions.assertThrows(KrakenException.class, () -> SecurityTool.evaluate(expression));
  }

  public static List<String> buildIllegalExpressions() {
    return List.of(
        "@{{buyerId==null?0:T(java.lang.Runtime).getRuntime().exec(new java.lang.String[]{body.p[0].body.p[1].body.p[2]})}}",
        "{T(java.lang.System).getenv('JAVA_HOME')}",
        "{T(java.lang.System).getProperty('user.home')}");
  }
}
