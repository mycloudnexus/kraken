package com.consoleconnect.kraken.operator.auth.controller;

import com.consoleconnect.kraken.operator.auth.jwt.JWKSetGenerator;
import com.consoleconnect.kraken.operator.auth.model.AuthDataProperty;
import com.consoleconnect.kraken.operator.auth.model.AuthServerEnabled;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@ConditionalOnBean(AuthServerEnabled.class)
@ConditionalOnProperty(value = "app.security.auth-server.jwks.enabled", havingValue = "true")
@AllArgsConstructor
@RestController()
@RequestMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "User Auth ", description = "User Auth")
public class JWKSController {
  private final AuthDataProperty.AuthServer authServer;

  @Operation(summary = "JWKSet")
  @GetMapping("/.well-known/jwks.json")
  public Map<String, Object> keys() {
    AuthDataProperty.Jwks jwks = authServer.getJwks();
    return JWKSetGenerator.generate(jwks).toJSONObject();
  }
}
