package com.consoleconnect.kraken.operator.auth.security;

import java.util.Map;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

public class JwtTokenVerifier implements OAuth2TokenValidator<Jwt> {

  private final Map<String, Object> claims;

  public JwtTokenVerifier(Map<String, Object> claims) {
    this.claims = claims;
  }

  @Override
  public OAuth2TokenValidatorResult validate(Jwt token) {
    if (claims == null || claims.isEmpty()) {
      return OAuth2TokenValidatorResult.success();
    }

    for (Map.Entry<String, Object> entry : claims.entrySet()) {
      if (!token.hasClaim(entry.getKey())
          || !token.getClaim(entry.getKey()).equals(entry.getValue())) {
        String description = String.format("The %s claim is not valid", entry.getKey());
        OAuth2Error error = new OAuth2Error(OAuth2ErrorCodes.INVALID_TOKEN, description, null);
        return OAuth2TokenValidatorResult.failure(error);
      }
    }
    return null;
  }
}
