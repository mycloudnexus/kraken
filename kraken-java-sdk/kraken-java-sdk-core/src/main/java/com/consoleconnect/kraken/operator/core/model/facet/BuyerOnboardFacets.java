package com.consoleconnect.kraken.operator.core.model.facet;

import lombok.Data;

@Data
public class BuyerOnboardFacets {
  BuyerInfo buyerInfo;

  @Data
  public static class BuyerInfo {
    private String buyerId;
    private String companyName;
    private String envId;
    private String role;
  }
}
