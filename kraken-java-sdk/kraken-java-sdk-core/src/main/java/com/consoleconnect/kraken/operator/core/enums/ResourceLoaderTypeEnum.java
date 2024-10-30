package com.consoleconnect.kraken.operator.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ResourceLoaderTypeEnum {
  CLASSPATH("classpath:"),
  FILE("file:"),
  GITHUB("github:"),
  RAW("raw:"),
  MEMORY("memory:"),
  UNDEFINED("undefined:");
  private final String kind;

  public static ResourceLoaderTypeEnum fromString(String kind) {
    for (ResourceLoaderTypeEnum resourceLoaderTypeEnum : ResourceLoaderTypeEnum.values()) {
      if (resourceLoaderTypeEnum.kind.equalsIgnoreCase(kind)) {
        return resourceLoaderTypeEnum;
      }
    }
    return UNDEFINED;
  }

  public static String generatePath(ResourceLoaderTypeEnum kind, String path) {
    return kind.getKind() + path;
  }
}
