package com.consoleconnect.kraken.operator.core.toolkit;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConstructExpressionUtil {

  private ConstructExpressionUtil() {}

  public static List<String> extractMapperParam(String param) {
    String patternStr = "\\@\\{\\{(.*?)\\}\\}";
    return extractParam(param, patternStr);
  }

  public static List<String> extractParam(String param, String patternStr) {
    List<String> contents = new ArrayList<>();
    Pattern pattern = Pattern.compile(patternStr);
    Matcher matcher = pattern.matcher(param);
    while (matcher.find()) {
      contents.add(matcher.group(1));
    }
    return contents;
  }

  public static String convertToJsonPointer(String path) {
    List<String> params = extractMapperParam(path);
    return "/"
        + params
            .get(0)
            .replaceAll("\\[(\\*)\\]", "[0]")
            .replaceAll("(?)\\[", "\\/")
            .replaceAll("(?)\\].", "\\/")
            .replaceAll("(\\.)", "\\/");
  }

  public static String replaceStarToZero(String param) {
    return param.replaceAll("\\[(\\*)\\]", "[0]");
  }

  public static String constructMefQuery(String s) {
    return String.format("${mefQuery.%s}", s);
  }

  public static String constructMeRequestBody(String s) {
    return String.format("${body.%s}", s);
  }

  public static String constructParam(String s) {
    return String.format("${%s}", s);
  }

  public static String constructBody(String source) {
    return source.replace("@{{", "${body.").replace("}}", "}");
  }

  public static String constructQuery(String source) {
    return source.replace("@{{", "${query.").replace("}}", "}");
  }

  public static String constructPath(String source) {
    return source.replace("@{{", "${").replace("}}", "}");
  }

  public static String constructDBParam(String s) {
    return String.format("${entity.response.%s}", s.replace("responseBody.", ""));
  }

  public static String constructOriginalDBParam(String s) {
    return String.format("@{{entity.response.%s}}", s.replace("responseBody.", ""));
  }

  public static List<String> extractOriginalPathParam(String path) {
    String patternStr = "\\{(.*?)\\}";
    return extractParam(path, patternStr);
  }
}
