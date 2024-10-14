package com.consoleconnect.kraken.operator.auth.security;

import com.consoleconnect.kraken.operator.auth.model.AuthDataProperty;
import com.consoleconnect.kraken.operator.auth.service.JwtDecoderService;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.ReactiveAuthenticationManagerResolver;
import reactor.core.publisher.Mono;

@Slf4j
@AllArgsConstructor
public class TenantAuthenticationManagerResolver
    implements ReactiveAuthenticationManagerResolver<String> {

  private final AuthDataProperty.ResourceServer resourceServer;
  private final JwtDecoderService jwtDecoderService;

  @Override
  public Mono<ReactiveAuthenticationManager> resolve(String issuer) {
    log.info("finding authentication manager via issuer:{}", issuer);
    Optional<AuthDataProperty.JwtDecoderProperty> decoderOptional =
        resourceServer.getJwt().stream()
            .filter(jwt -> jwt.getIssuer().equalsIgnoreCase(issuer))
            .findFirst();
    if (decoderOptional.isPresent()) {
      log.info("found jwt decoder for issuer:{}", issuer);
      AuthDataProperty.JwtDecoderProperty decoderProperty = decoderOptional.get();
      return Mono.just(new AuthenticationManager(decoderProperty, jwtDecoderService));
    } else {
      log.warn("no jwt decoder found for issuer:{}", issuer);
      return Mono.empty();
    }
  }
}
