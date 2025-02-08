package com.consoleconnect.kraken.operator.controller.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class CreateSellerContactRequest extends SellerContractDto {
  private String parentProductType;
  private String componentKey;
}
