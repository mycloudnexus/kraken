package com.consoleconnect.kraken.operator.controller.dto;

import java.util.List;
import lombok.Data;

@Data
public class ComponentProductCategoryDTO {
  private List<ComponentProductMetadata> componentProducts;
  private List<ProductCategoryMetaData> productCategories;

  @Data
  public static class ComponentProductMetadata {
    private String key;
    private String id;
  }

  @Data
  public static class ProductCategoryMetaData {
    private String kind;
    private String name;
  }
}
