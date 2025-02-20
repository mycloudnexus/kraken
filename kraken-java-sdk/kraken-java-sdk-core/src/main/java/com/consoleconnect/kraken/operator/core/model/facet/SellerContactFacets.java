package com.consoleconnect.kraken.operator.core.model.facet;

import lombok.Data;

@Data
public class SellerContactFacets {
  SellerInfo sellerInfo;

  @Data
  public static class SellerInfo {
    private String name;
    private String emailAddress;
    private String number;
    private String role;
  }
}
