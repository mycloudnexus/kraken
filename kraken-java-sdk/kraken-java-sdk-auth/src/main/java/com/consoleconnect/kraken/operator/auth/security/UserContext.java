package com.consoleconnect.kraken.operator.auth.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.jwt.JwtClaimAccessor;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import reactor.core.publisher.Mono;

@Slf4j
public class UserContext {

  public static final String ANONYMOUS = "anonymous";
  public static final String SYSTEM_UPGRADE = "SYSTEM_UPGRADE";
  public static final String SYSTEM_UPGRADE_NAME = "system";

  public static final String AUTHORIZATION_HEADER = "Authorization";
  public static final String AUTHORIZATION_HEADER_PREFIX = "Bearer ";

  public static final String TOKEN_CLAIM_ROLES = "roles";

  private UserContext() {}

  public static Mono<String> getUserId() {
    return getAuthentication()
        .map(JwtAuthenticationToken::getToken)
        .map(JwtClaimAccessor::getSubject)
        .switchIfEmpty(Mono.just(ANONYMOUS));
  }

  public static Mono<JwtAuthenticationToken> getAuthentication() {
    try {
      return ReactiveSecurityContextHolder.getContext()
          .map(SecurityContext::getAuthentication)
          .cast(JwtAuthenticationToken.class);
    } catch (Exception ex) {
      log.warn("Failed to get authentication", ex);
      return Mono.empty();
    }
  }
}
