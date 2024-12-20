package com.consoleconnect.kraken.operator.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ProductCategoryEnum {
  ACCESS_ELINE("access.eline", "Access Eline"),
  INTERNET_ACCESS("internet.access", "Internet Access");
  private final String kind;
  private final String name;
}
