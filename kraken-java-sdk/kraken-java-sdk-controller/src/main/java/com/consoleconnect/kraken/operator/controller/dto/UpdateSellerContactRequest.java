package com.consoleconnect.kraken.operator.controller.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class UpdateSellerContactRequest extends SellerContractDto {
  private String key;
}
