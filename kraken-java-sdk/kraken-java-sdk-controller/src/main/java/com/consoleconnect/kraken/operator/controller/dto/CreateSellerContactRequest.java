package com.consoleconnect.kraken.operator.controller.dto;

import java.util.List;
import lombok.Data;

@Data
public class CreateSellerContactRequest {
  private List<String> productCategories;
  private String contactName;
  private String contactEmail;
  private String contactPhone;
}
