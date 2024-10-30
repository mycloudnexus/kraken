package com.consoleconnect.kraken.operator.auth.security;

import java.util.List;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.Ordered;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@AllArgsConstructor
public class SecurityCheckHolderFilter implements WebFilter, Ordered {

  private final String name;
  private final List<String> pathPatterns;
  private final SecurityChecker securityChecker;
  private final AntPathMatcher antPathMatcher = new AntPathMatcher();

  @Override
  public int getOrder() {
    return securityChecker.getOrder();
  }

  @Override
  public @NotNull Mono<Void> filter(@NotNull ServerWebExchange exchange, WebFilterChain chain) {
    boolean present =
        pathPatterns.stream()
            .anyMatch(
                pattern -> antPathMatcher.match(pattern, exchange.getRequest().getPath().value()));
    if (!present) {
      return chain.filter(exchange);
    }
    return securityChecker.internalRun(exchange).then(chain.filter(exchange));
  }
}
