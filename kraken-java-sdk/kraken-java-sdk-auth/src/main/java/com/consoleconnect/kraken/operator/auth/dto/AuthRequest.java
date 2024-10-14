package com.consoleconnect.kraken.operator.auth.dto;

import com.consoleconnect.kraken.operator.auth.enums.AuthGrantTypeEnum;
import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

@Data
public class AuthRequest {
  @JsonAlias({"username", "userName", "user-name"})
  private String email;

  private String password;

  private String refreshToken;

  private AuthGrantTypeEnum grantType = AuthGrantTypeEnum.USERNAME_PASSWORD;
}
