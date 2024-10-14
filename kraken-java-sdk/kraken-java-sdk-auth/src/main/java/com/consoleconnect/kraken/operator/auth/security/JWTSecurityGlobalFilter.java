package com.consoleconnect.kraken.operator.auth.security;

import com.consoleconnect.kraken.operator.auth.model.AuthDataProperty;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.Ordered;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@AllArgsConstructor
public class JWTSecurityGlobalFilter implements WebFilter, Ordered {

  private final AuthDataProperty.ResourceServer resourceServer;

  @Override
  public int getOrder() {
    return 0;
  }

  @Override
  public @NotNull Mono<Void> filter(@NotNull ServerWebExchange exchange, WebFilterChain chain) {
    return ReactiveSecurityContextHolder.getContext()
        .map(
            securityContext -> {
              Authentication authentication = securityContext.getAuthentication();
              exchange.getAttributes().put(resourceServer.getUserId(), authentication.getName());
              return Mono.just(authentication);
            })
        .then(chain.filter(exchange));
  }
}
