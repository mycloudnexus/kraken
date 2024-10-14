package com.consoleconnect.kraken.operator.gateway.filter;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.handler.predicate.AbstractRoutePredicateFactory;
import org.springframework.cloud.gateway.handler.predicate.GatewayPredicate;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.server.ServerWebExchange;

@Slf4j
public class QueryParamsRoutePredicateFactory
    extends AbstractRoutePredicateFactory<QueryParamsRoutePredicateFactory.Config> {

  public QueryParamsRoutePredicateFactory() {
    super(QueryParamsRoutePredicateFactory.Config.class);
  }

  @Override
  public Predicate<ServerWebExchange> apply(QueryParamsRoutePredicateFactory.Config config) {
    return (GatewayPredicate)
        exchange -> {
          for (String param : config.getQueryParams()) {
            log.info("QueryParamsRoutePredicateFactory: param={}", param);
            if (exchange.getRequest().getQueryParams().containsKey(param)) {
              String value = exchange.getRequest().getQueryParams().getFirst(param);
              log.info("Uri Variables {}={}", param, value);
              ServerWebExchangeUtils.putUriTemplateVariables(exchange, Map.of(param, value));
            } else {
              log.info("QueryParamsRoutePredicateFactory: param={} not found", param);
              return false;
            }
          }
          return true;
        };
  }

  @Data
  @Validated
  public static class Config {
    @NotEmpty private List<String> queryParams;
  }
}
