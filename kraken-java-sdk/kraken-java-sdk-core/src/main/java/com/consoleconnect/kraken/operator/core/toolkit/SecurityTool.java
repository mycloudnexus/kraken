package com.consoleconnect.kraken.operator.core.toolkit;

import static com.consoleconnect.kraken.operator.core.toolkit.AuditConstants.BANNED_TOKENS;

import com.consoleconnect.kraken.operator.core.exception.KrakenException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;

public class SecurityTool {
  private SecurityTool() {}

  public static void evaluate(String expression) {
    if (StringUtils.isBlank(expression)) {
      return;
    }
    if (BANNED_TOKENS.matcher(expression).find()) {
      throw new KrakenException(
          HttpStatus.BAD_REQUEST.value(), "Expression contains banned token(s) and is not allowed");
    }
  }
}
