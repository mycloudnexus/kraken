package com.consoleconnect.kraken.operator.auth.dto;

import com.consoleconnect.kraken.operator.auth.security.UserContext;
import lombok.Data;

@Data
public class AuthResponse {
  private String id;
  private String accessToken;
  private Long expiresIn;
  private String tokenType = UserContext.AUTHORIZATION_HEADER_PREFIX;

  private String refreshToken;
  private Long refreshTokenExpiresIn;
}
