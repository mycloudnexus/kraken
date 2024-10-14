package com.consoleconnect.kraken.operator.auth.model;

import lombok.Data;

@Data
public class JwtToken {
  private String token;
  private long expiresIn;
  private String tokenType;
}
