package com.consoleconnect.kraken.operator.gateway.filter;

import com.consoleconnect.kraken.operator.core.model.facet.ComponentAPIFacets;
import com.consoleconnect.kraken.operator.gateway.runner.AbstractActionRunner;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.web.server.ServerWebExchange;

@Slf4j
public class ActionGatewayFilterFactory
    extends AbstractGatewayFilterFactory<ComponentAPIFacets.Action> {

  private final List<AbstractActionRunner> runners;

  public ActionGatewayFilterFactory(List<AbstractActionRunner> runners) {
    super();
    this.runners = runners;
  }

  @Override
  public GatewayFilter apply(ComponentAPIFacets.Action config) {
    return (exchange, chain) -> {
      Optional<ServerWebExchange> updatedExchangeOptional = Optional.empty();
      Optional<AbstractActionRunner> runnerOptional =
          runners.stream().filter(runner -> runner.canHandle(config)).findFirst();
      if (config.isPreRequest() && runnerOptional.isPresent()) {
        log.info("Running pre request action: {}", config.getActionType());
        updatedExchangeOptional = runnerOptional.get().run(exchange, config);
      }
      final ServerWebExchange updatedExchange = updatedExchangeOptional.orElse(exchange);
      return chain
          .filter(updatedExchange)
          .doFinally(
              signalType -> {
                // Post request
                if (config.isPostRequest() && runnerOptional.isPresent()) {
                  log.info("Running post request action: {}", config.getActionType());
                  runnerOptional.get().run(updatedExchange, config);
                }
              });
    };
  }
}
