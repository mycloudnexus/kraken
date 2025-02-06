package com.consoleconnect.kraken.operator.sync.service;

import com.consoleconnect.kraken.operator.auth.dto.AuthResponse;
import com.consoleconnect.kraken.operator.core.model.HttpResponse;
import com.consoleconnect.kraken.operator.sync.ClientCredentialMockServerTest;
import com.consoleconnect.kraken.operator.sync.service.security.ExternalSystemTokenProvider;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import reactor.core.publisher.Mono;

class ClientCredentialTokenProviderTest extends ClientCredentialMockServerTest {

  @Test
  void givenClientCredentialTokenEnabled_whenGet_thenRespondInternalToken() {
    String token = generateToken();
    AuthResponse authResponse = new AuthResponse();
    authResponse.setAccessToken(token);
    mockServer
        .responseWith(HttpStatus.OK, HttpResponse.ok(authResponse), new HashMap<>())
        .call(
            () -> {
              String resp = externalSystemTokenProvider.getToken();
              return Mono.just(resp);
            })
        .expectResponse(ExternalSystemTokenProvider.BEARER_TOKEN_PREFIX + token)
        .takeRequest()
        .expectMethod("POST")
        .expectPath("/tenant/auth/token");

    // Return saved token when get token again
    String resp = externalSystemTokenProvider.getToken();
    Assertions.assertEquals(resp, ExternalSystemTokenProvider.BEARER_TOKEN_PREFIX + token);
  }

  private String generateToken() {
    long current = System.currentTimeMillis();
    return Jwts.builder()
        .header()
        .add("kid", "kid1")
        .and()
        .subject("test")
        .claims(Map.of("gty", "client-credentials"))
        .issuedAt(new Date(current))
        .expiration(new Date(current + 100 * 1000))
        .issuer("https://test.issuer")
        .signWith(
            SignatureAlgorithm.HS256,
            "ABCDEFG012345789ABCDEFG012345789ABCDEFG012345789ABCDEFG012345789")
        .compact();
  }
}
