package com.consoleconnect.kraken.operator.core.toolkit;

import static com.consoleconnect.kraken.operator.core.toolkit.AssetsConstants.CUSTOMIZED_PLACE_HOLDER;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;

public class ConstructExpressionUtil {

  public static final String ARRAY_ROOT_PREFIX = "[*].";

  private ConstructExpressionUtil() {}

  public static List<String> extractMapperParam(String param) {
    return extractParam(param, CUSTOMIZED_PLACE_HOLDER);
  }

  public static List<String> extractParam(String param, String patternStr) {
    if (StringUtils.isBlank(param)) {
      return List.of();
    }
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
    String param = params.get(0);
    if (StringUtils.isNotBlank(param) && param.startsWith(ARRAY_ROOT_PREFIX)) {
      param = param.substring(ARRAY_ROOT_PREFIX.length(), param.length());
    }
    return "/"
        + param
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

  public static String constructJsonPathBody(String source) {
    return source.replace("@{{", "$.body.").replace("}}", "");
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
