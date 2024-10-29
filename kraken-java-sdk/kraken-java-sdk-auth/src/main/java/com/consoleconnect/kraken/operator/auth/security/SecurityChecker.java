package com.consoleconnect.kraken.operator.auth.security;

import org.springframework.core.Ordered;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

public interface SecurityChecker extends Ordered {
  Mono<Object> internalRun(ServerWebExchange exchange);
}
