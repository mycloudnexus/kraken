package com.consoleconnect.kraken.operator.controller.tools;

import com.consoleconnect.kraken.operator.core.exception.KrakenException;

public class AssetKeyHelper {

  private static final String ERR_INVALID_KEY = "Invalid key";

  private AssetKeyHelper() {}

  public static String toKindKey(String sourceKey, String parentKey, String kind) {
    String prefix = parentKey + ".";
    if (!sourceKey.startsWith(prefix)) {
      throw KrakenException.internalError(ERR_INVALID_KEY);
    }
    String suffix = sourceKey.substring(prefix.length());
    return kind + suffix.substring(suffix.indexOf('.'));
  }
}
