package com.consoleconnect.kraken.operator.controller.dto;

import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class CreateSellerContactRequest extends SellerContractDto {
  private List<String> productCategories;
}
