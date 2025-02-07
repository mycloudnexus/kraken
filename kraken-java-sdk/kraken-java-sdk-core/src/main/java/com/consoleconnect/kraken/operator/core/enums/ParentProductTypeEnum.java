package com.consoleconnect.kraken.operator.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ParentProductTypeEnum {
  ACCESS_ELINE(
      "access.eline", "Access Eline", "including two sub-product-types: access_e_line and uni"),
  INTERNET_ACCESS("internet.access", "Internet Access", "not supported yet");

  private final String kind;
  private final String name;
  private final String desc;
}
