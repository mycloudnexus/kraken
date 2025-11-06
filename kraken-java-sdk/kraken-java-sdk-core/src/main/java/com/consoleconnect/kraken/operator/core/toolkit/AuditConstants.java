package com.consoleconnect.kraken.operator.core.toolkit;

import java.util.regex.Pattern;

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

  public static final Pattern BANNED_TOKENS = Pattern.compile(
          "(?i)\\bT\\s*\\(|\\.class\\b|\\bnew\\b|getRuntime\\b|\\.exec\\b|ProcessBuilder\\b|Class\\.forName\\b|reflect\\.|javax\\.script\\b|java\\.lang\\b|java\\.io\\b",
          Pattern.CASE_INSENSITIVE);
}
