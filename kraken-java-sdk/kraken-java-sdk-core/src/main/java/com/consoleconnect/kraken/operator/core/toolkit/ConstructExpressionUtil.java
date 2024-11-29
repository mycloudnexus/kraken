package com.consoleconnect.kraken.operator.core.toolkit;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;

public class ConstructExpressionUtil {

  public static final String ARRAY_ROOT_PREFIX = "[*].";

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

  public static String convertPathToJsonPointer(String path) {
    return convertPathToJsonPointer(path, "[0]");
  }

  public static String convertPathToJsonPointer(String path, String arrayReplacement) {
    List<String> params = extractMapperParam(path);
    String param = params.get(0);
    if (StringUtils.isNotBlank(param) && param.startsWith(ARRAY_ROOT_PREFIX)) {
      param = param.substring(ARRAY_ROOT_PREFIX.length());
    }
    return "/"
        + param
            .replaceAll("\\[(\\*)\\]", arrayReplacement)
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

  public static String constructBodyOfArray(String source, int indexOfArray) {
    return constructBodyOfArray(source, indexOfArray, "${body.");
  }

  public static String constructBodyOfArray(String source, int indexOfArray, String arrayPrefix) {
    return source
        .replace("@{{", arrayPrefix)
        .replace("}}", "}")
        .replace("[*]", "[" + indexOfArray + "]");
  }

  public static String constructJsonPath(String prefix, String source) {
    return source.replace("@{{", prefix).replace("}}", "");
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
