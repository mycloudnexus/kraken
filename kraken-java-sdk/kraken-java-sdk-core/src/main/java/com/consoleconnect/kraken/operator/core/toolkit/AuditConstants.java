package com.consoleconnect.kraken.operator.core.toolkit;

public class AuditConstants {

  private AuditConstants() {}

  public static final String VAR_PREFIX = "#";
  public static final String VAR_RESULT_PREFIX = "$.data.";

  public static final String ANONYMOUS_USER = "anonymous";

  public static final String AUDIT_KEY = "x-audit-key";
  public static final String AUDIT_ANNOTATION_KEY = "x-audit-action";
  public static final String TARGET_SPEC = "target api server";
  public static final String API_MAPPING = "api mapping use case";
  public static final String DEVELOPMENT = "development";
}
