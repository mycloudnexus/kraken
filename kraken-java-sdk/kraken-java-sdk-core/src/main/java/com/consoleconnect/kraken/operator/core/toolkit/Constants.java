package com.consoleconnect.kraken.operator.core.toolkit;

public class Constants {

  private Constants() {}

  public static final String IP_LOCAL_HOSTNAME = "localhost";
  public static final String COMMA = ",";
  public static final String MAPPER_SIGN = "-mapper";
  public static final String INIT_VERSION = "1.0";
  public static final String ENV = "env";

  public static String formatVersion(String version) {
    return version.replaceFirst("[V|v]", "");
  }

  public static String formatVersionUsingV(String version) {
    return "V" + formatVersion(version);
  }
}
