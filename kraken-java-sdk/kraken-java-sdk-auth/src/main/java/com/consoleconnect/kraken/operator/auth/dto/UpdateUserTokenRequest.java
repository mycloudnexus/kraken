package com.consoleconnect.kraken.operator.auth.dto;

import java.util.Map;
import lombok.Data;

@Data
public class UpdateUserTokenRequest {
  private Map<String, Object> claims;
  private Long expiresInSeconds;
  private String token;
}
