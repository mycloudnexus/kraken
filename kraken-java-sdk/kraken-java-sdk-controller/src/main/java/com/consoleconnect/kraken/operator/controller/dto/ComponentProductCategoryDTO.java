package com.consoleconnect.kraken.operator.controller.dto;

import com.consoleconnect.kraken.operator.core.enums.ProductCategoryEnum;
import java.util.List;
import lombok.Data;

@Data
public class ComponentProductCategoryDTO {
  private List<String> componentKeys;
  private List<ProductCategoryEnum> productCategories;
}
