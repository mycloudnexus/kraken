package com.consoleconnect.kraken.operator.controller.dto;

import lombok.Data;

@Data
public class VerifyMapperRequest {
  private String tagId;
  private boolean verified;
}