package com.consoleconnect.kraken.operator.auth.service;

import com.consoleconnect.kraken.operator.auth.security.SecurityChecker;
import com.consoleconnect.kraken.operator.core.exception.KrakenException;
import com.consoleconnect.kraken.operator.core.toolkit.LabelConstants;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component(value = "securityTestChecker")
@AllArgsConstructor
public class TestChecker implements SecurityChecker {

  @Override
  public Mono<Object> internalRun(ServerWebExchange exchange) {
    return ReactiveSecurityContextHolder.getContext()
        .handle(
            (securityContext, sink) -> {
              Authentication authentication = securityContext.getAuthentication();
              Map<String, String> labels = Map.of();
              Instant expiredDate =
                  Optional.ofNullable(labels.get(LabelConstants.LABEL_EXPIRED_AT))
                      .map(DateTimeFormatter.ISO_INSTANT::parse)
                      .map(Instant::from)
                      .orElse(Instant.MIN);
              Jwt principal = (Jwt) authentication.getPrincipal();
              Instant issuedAt = principal.getIssuedAt();
              if (issuedAt.isBefore(expiredDate)) {
                sink.error(KrakenException.badRequest("Token expired "));
                return;
              }
              sink.next(new Object());
            });
  }

  @Override
  public int getOrder() {
    return -1;
  }
}
