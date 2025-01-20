package com.consoleconnect.kraken.operator.core.toolkit;

public class Constants {

  private Constants() {}

  public static final String IP_LOCAL_HOSTNAME = "localhost";
  public static final String COMMA = ",";
  public static final String DOT = ".";
  public static final String MAPPER_SIGN = "-mapper";
  public static final String INIT_VERSION = "1.0";
  public static final String ENV = "env";
  public static final String QUOTE_KEY_WORD = "quote";
  public static final String ORDER_KEY_WORD = "order";
  public static final String WORKFLOW_PARAM_PREFIX = "${workflow.input.%s}";
  public static final String EMPTY_TASK = "empty_task";

  public static String formatVersion(String version) {
    return version.replaceFirst("[V|v]", "");
  }

  public static String formatVersionUsingV(String version) {
    return "V" + formatVersion(version);
  }
}
