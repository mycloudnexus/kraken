package com.consoleconnect.kraken.operator.core.toolkit;

import static com.consoleconnect.kraken.operator.core.toolkit.Constants.ESCAPED_DOUBLE_QUOTE;
import static org.apache.commons.lang3.StringUtils.*;

import com.consoleconnect.kraken.operator.core.exception.ErrorResponse;

public class StringUtils {

  private StringUtils() {}

  public static String maskString(String data, int plainTextLength) {
    if (data == null || data.trim().isEmpty()) {
      return data;
    }
    String regex = String.format("(?<=.{%d}).", plainTextLength);
    return data.replaceAll(regex, "*");
  }

  public static String generateInitial(String name) {
    if (name == null || name.trim().isEmpty()) {
      return name;
    }
    String[] names = name.split(" ");
    StringBuilder sb = new StringBuilder();
    for (String n : names) {
      sb.append(n.charAt(0));
    }
    return sb.toString().toUpperCase();
  }

  public static String compact(String str) {
    return str.replace(SPACE, EMPTY).replace(LF, EMPTY);
  }

  public static String truncate(String raw, int upperLength) {
    if (null == raw || raw.trim().isEmpty() || upperLength <= 0) {
      return raw;
    }
    String trimmed = raw.trim();
    if (trimmed.length() <= upperLength) {
      return removeEscapedCharacter(trimmed, ESCAPED_DOUBLE_QUOTE);
    }
    return removeEscapedCharacter(trimmed.substring(0, upperLength), ESCAPED_DOUBLE_QUOTE);
  }

  public static String removeEscapedCharacter(String raw, String escapedCharacters) {
    if (isBlank(raw) || isBlank(escapedCharacters)) {
      return raw;
    }
    return raw.contains(escapedCharacters) ? raw.replace(escapedCharacters, EMPTY) : raw;
  }

  public static void processRawMessage(ErrorResponse errorResponse, String rawMsg) {
    if (org.apache.commons.lang3.StringUtils.isBlank(rawMsg)) {
      errorResponse.setMessage("");
    } else {
      String cleanedMessage = rawMsg;
      if (rawMsg.contains("@{{")) {
        cleanedMessage = rawMsg.replace("@{{", "").replace("}}", "");
      }
      errorResponse.setMessage(removeEscapedCharacter(cleanedMessage, ESCAPED_DOUBLE_QUOTE));
    }
  }
}
