package com.consoleconnect.kraken.operator.auth.dto;

import lombok.Data;

@Data
public class RefreshTokenRequest {
  private String refreshToken;
}
