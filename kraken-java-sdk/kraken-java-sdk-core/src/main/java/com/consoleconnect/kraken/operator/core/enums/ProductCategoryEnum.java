package com.consoleconnect.kraken.operator.core.enums;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.Getter;

@Getter
public enum ProductCategoryEnum {
  ACCESS_ELINE("access.eline", "Access Eline"),
  INTERNET_ACCESS("internet.access", "Internet Access");

  ProductCategoryEnum(String kind, String name) {
    this.kind = kind;
    this.name = name;
    Holder.holderMap.put(kind, this);
  }

  private final String kind;
  private final String name;

  private static class Holder {
    private static final Map<String, ProductCategoryEnum> holderMap = new HashMap<>();
  }

  public static ProductCategoryEnum kindOf(String kind) {
    return Objects.isNull(kind) ? null : Holder.holderMap.getOrDefault(kind, null);
  }

  public static Map<String, String> all() {
    return Holder.holderMap.entrySet().stream()
        .collect(Collectors.toMap(Map.Entry::getKey, v -> v.getValue().getName()));
  }
}
