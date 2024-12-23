package com.consoleconnect.kraken.operator.core.enums;

import java.util.HashMap;
import java.util.Map;
import lombok.Getter;

@Getter
public enum ProductCategoryEnum {
  ACCESS_ELINE("access.eline", "Access Eline"),
  INTERNET_ACCESS("internet.access", "Internet Access");

  ProductCategoryEnum(String kind, String name) {
    this.kind = kind;
    this.name = name;
    Holder.categoryMap.put(kind, name);
  }

  private final String kind;
  private final String name;

  public static class Holder {
    static Map<String, String> categoryMap = new HashMap<>();
  }
}
