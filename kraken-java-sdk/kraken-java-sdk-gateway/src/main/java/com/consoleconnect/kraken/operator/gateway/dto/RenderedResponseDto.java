package com.consoleconnect.kraken.operator.gateway.dto;

import java.util.List;
import lombok.Data;

@Data
public class RenderedResponseDto {
  private String id;
  private String orderId;
  private String state;
  private List<ProductOrderItem> productOrderItem;

  @Data
  public static class ProductOrderItem {
    private String id;
    private String state;
    private String action;
  }
}
