package com.consoleconnect.kraken.operator.core.toolkit;

import static org.apache.commons.lang3.StringUtils.*;

public class StringUtils {

  public static final String ESCAPED_DOUBLE_QUOTE = "\"";

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

    if (raw.trim().length() < upperLength) {
      return raw.trim().replace(ESCAPED_DOUBLE_QUOTE, EMPTY);
    }
    return raw.trim().substring(0, upperLength).replace(ESCAPED_DOUBLE_QUOTE, EMPTY);
  }
}
