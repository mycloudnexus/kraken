package com.consoleconnect.kraken.operator.core.enums;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Getter;

@Getter
public enum ProductCategoryEnum {
  ACCESS_ELINE("access.eline", "Access Eline"),
  INTERNET_ACCESS("internet.access", "Internet Access");

  ProductCategoryEnum(String kind, String name) {
    this.kind = kind;
    this.name = name;
    Holder.categoryMap.put(kind, this);
  }

  private final String kind;
  private final String name;

  private static class Holder {
    private static final Map<String, ProductCategoryEnum> categoryMap = new HashMap<>();
  }

  public static ProductCategoryEnum kindOf(String kind) {
    return Holder.categoryMap.getOrDefault(kind, null);
  }

  public static Map<String, String> all() {
    return Holder.categoryMap.entrySet().stream()
        .collect(Collectors.toMap(Map.Entry::getKey, v -> v.getValue().getName()));
  }
}
