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
  public static final String INT_VAL = "integer";
  public static final String DOUBLE_VAL = "double";
  public static final String FAIL_ORDER_TASK = "fail_order_task";
  public static final String EVALUATE_PAYLOAD_TASK = "evaluate_payload_task";
  public static final String REJECT_ORDER_TASK = "reject_order_task";
  public static final String EMPTY_TASK = "empty_task";
  public static final String LOG_PAYLOAD_TASK = "log_payload_task";
  public static final String PROCESS_ORDER_TASK = "process_order_task";
  public static final String PERSIST_RESPONSE_TASK = "persist_response_task";
  public static final String NOTIFY_TASK = "notify_task";
  public static final String WORKFLOW_PARAM_PREFIX = "${workflow.input.%s}";

  public static String formatVersion(String version) {
    return version.replaceFirst("[V|v]", "");
  }

  public static String formatVersionUsingV(String version) {
    return "V" + formatVersion(version);
  }
}
