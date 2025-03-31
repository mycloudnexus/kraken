package com.consoleconnect.kraken.operator.core.toolkit;

import static com.consoleconnect.kraken.operator.core.toolkit.Constants.ESCAPED_DOUBLE_QUOTE;
import static org.apache.commons.lang3.StringUtils.*;

import com.consoleconnect.kraken.operator.core.exception.ErrorResponse;
import com.consoleconnect.kraken.operator.core.exception.KrakenException;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import lombok.extern.slf4j.Slf4j;

@Slf4j
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

  public static String shortenUUID(String uuidString) {
    if (uuidString.length() < 8) {
      throw KrakenException.internalError("uuid string length should be greater than 8");
    }
    // fetch the first 8 characters of the UUID, which is enough to uniquely identify it
    return uuidString.substring(0, 8);
  }

  public static Object readWithJsonPath(Object json, String path) {
    try {
      return JsonPath.read(json, path);
    } catch (Exception e) {
      return EMPTY;
    }
  }

  public static void writeWithJsonPath(Object json, String path, String node, Object value) {
    try {
      DocumentContext doc = JsonPath.parse(json);
      doc.put(path, node, value);
    } catch (Exception e) {
      log.error(String.format("failed to set json path %s, error: %s", path, e.getMessage()));
    }
  }
}
