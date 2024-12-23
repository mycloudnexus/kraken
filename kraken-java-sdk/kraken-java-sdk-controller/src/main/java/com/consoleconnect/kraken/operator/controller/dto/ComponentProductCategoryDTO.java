package com.consoleconnect.kraken.operator.controller.dto;

import com.consoleconnect.kraken.operator.core.enums.ProductCategoryEnum;
import java.util.List;
import java.util.Map;
import lombok.Data;

@Data
public class ComponentProductCategoryDTO {
  private Map<String, String> componentKeys;
  private List<ProductCategoryEnum> productCategories;
}
