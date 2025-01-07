package com.consoleconnect.kraken.operator.core.model.facet;

import lombok.Data;

@Data
public class SellerContactFacets {
  SellerInfo sellerInfo;

  @Data
  public static class SellerInfo {
    private String contactName;
    private String contactEmail;
    private String contactPhone;
    private String role;
  }
}
