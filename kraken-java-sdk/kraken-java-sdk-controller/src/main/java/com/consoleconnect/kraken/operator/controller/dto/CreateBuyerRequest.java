package com.consoleconnect.kraken.operator.controller.dto;

import lombok.Data;

@Data
public class CreateBuyerRequest {
  private String buyerId;
  private String companyName;
  private String envId;

  private long tokenExpiredInSeconds;
}
