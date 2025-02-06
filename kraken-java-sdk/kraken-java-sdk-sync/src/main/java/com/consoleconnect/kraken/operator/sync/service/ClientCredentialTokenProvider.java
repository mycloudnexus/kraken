package com.consoleconnect.kraken.operator.sync.service;

import com.consoleconnect.kraken.operator.auth.dto.AuthResponse;
import com.consoleconnect.kraken.operator.core.exception.KrakenException;
import com.consoleconnect.kraken.operator.sync.model.SyncProperty;
import com.consoleconnect.kraken.operator.sync.service.security.ExternalSystemTokenProvider;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTParser;
import java.text.ParseException;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@Slf4j
@ConditionalOnProperty(
    value = "app.control-plane.auth.client-credentials.enabled",
    havingValue = "true")
public class ClientCredentialTokenProvider implements ExternalSystemTokenProvider {

  private static final long EXPIRATION_BUFFER_IN_SECONDS = 30;

  private static final String ENDPOINT_AUTH_TOKEN = "/auth/token";

  private String cachedToken = null;

  private WebClient webClient = null;

  private final SyncProperty syncProperty;

  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class ClientAuthRequest {
    @JsonAlias({"username"})
    private String clientId;

    @JsonAlias({"password"})
    private String clientSecret;
  }

  public ClientCredentialTokenProvider(SyncProperty syncProperty) {
    this.syncProperty = syncProperty;
  }

  @Override
  public String getToken() {
    if (cachedToken == null || isTokenExpired(cachedToken)) {
      cachedToken = generateAccessToken().getAccessToken();
    }
    return ExternalSystemTokenProvider.BEARER_TOKEN_PREFIX + cachedToken;
  }

  private boolean isTokenExpired(String token) {
    try {
      JWT jwt = JWTParser.parse(token);
      long now = System.currentTimeMillis();
      long expiration = jwt.getJWTClaimsSet().getExpirationTime().getTime();
      return (expiration - now) < EXPIRATION_BUFFER_IN_SECONDS * 1000;
    } catch (ParseException e) {
      throw KrakenException.internalError(e.getMessage());
    }
  }

  private AuthResponse generateAccessToken() {
    SyncProperty.ClientCredentials clientCredentials =
        syncProperty.getControlPlane().getAuth().getClientCredentials();
    ClientAuthRequest request =
        ClientAuthRequest.builder()
            .clientId(clientCredentials.getClientId())
            .clientSecret(clientCredentials.getClientSecret())
            .build();
    return getWebClient()
        .method(HttpMethod.POST)
        .uri(uriBuilder -> uriBuilder.path(ENDPOINT_AUTH_TOKEN).build())
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .body(BodyInserters.fromValue(request))
        .retrieve()
        .bodyToMono(new ParameterizedTypeReference<AuthResponse>() {})
        .block();
  }

  private WebClient getWebClient() {
    synchronized (this) {
      if (this.webClient == null) {
        this.webClient =
            WebClient.builder()
                .baseUrl(
                    syncProperty
                        .getControlPlane()
                        .getAuth()
                        .getClientCredentials()
                        .getAuthServerUrl())
                .build();
      }
      return this.webClient;
    }
  }
}
