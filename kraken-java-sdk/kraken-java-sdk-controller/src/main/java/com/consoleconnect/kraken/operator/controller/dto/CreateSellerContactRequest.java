package com.consoleconnect.kraken.operator.controller.dto;

import lombok.Data;

@Data
public class CreateSellerContactRequest {
  private String productType;
  private String contactName;
  private String contactEmail;
  private String contactPhone;
}
