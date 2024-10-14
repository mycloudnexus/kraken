package com.consoleconnect.kraken.operator.gateway.filter;

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.CACHED_REQUEST_BODY_ATTR;

import com.consoleconnect.kraken.operator.core.exception.KrakenException;
import com.consoleconnect.kraken.operator.core.model.facet.ComponentAPIFacets;
import com.consoleconnect.kraken.operator.core.toolkit.JsonToolkit;
import com.consoleconnect.kraken.operator.gateway.template.SpELEngine;
import java.util.*;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;

@Slf4j
public class GeneratePathFromBodyGatewayFilterFactory
    extends AbstractGatewayFilterFactory<GeneratePathFromBodyGatewayFilterFactory.Config> {

  public static final String X_FILTER_ID = "X-Filter-Id";
  public static final String X_PATH = "X-PATH";

  @Override
  public GatewayFilter apply(Config config) {
    return (exchange, chain) -> {
      String requestPayload = exchange.getAttribute(CACHED_REQUEST_BODY_ATTR);
      Map<String, Object> variables =
          getVariables(ServerWebExchangeUtils.getUriTemplateVariables(exchange), requestPayload);
      Optional<ComponentAPIFacets.Filter> filterOptional =
          selectFilter(variables, config.getFilters());
      if (filterOptional.isPresent()) {
        ComponentAPIFacets.Filter filter = filterOptional.get();
        exchange.getAttributes().put(X_FILTER_ID, filter.getId());
        log.info("filter matched: {}", filter.getId());

        // render path
        String renderedPath = SpELEngine.evaluate(filter.getPath(), variables, String.class);
        log.info("rendered path: {}", renderedPath);
        exchange.getAttributes().put(X_PATH, renderedPath);
      } else {
        log.warn("No filter matched");
        throw new KrakenException(
            500,
            "your request failed to be handled",
            new IllegalArgumentException(
                "please double check your request payload or contact us to debug"));
      }
      String filterId = Objects.requireNonNull(exchange.getAttribute(X_FILTER_ID));
      log.info("filterId: {}", filterId);
      String path = Objects.requireNonNull(exchange.getAttribute(X_PATH));
      ServerHttpRequest.Builder builder = exchange.getRequest().mutate();
      builder.header(X_FILTER_ID, filterId);
      config.getFilters().stream()
          .filter(filter -> filter.getId().equals(filterId))
          .findFirst()
          .ifPresent(
              filter -> {
                builder.path(path);
                if (filter.getMethod() != null) {
                  builder.method(HttpMethod.valueOf(filter.getMethod().toUpperCase()));
                }
              });
      ServerWebExchange build = exchange.mutate().request(builder.build()).build();
      return chain.filter(build);
    };
  }

  private Optional<ComponentAPIFacets.Filter> selectFilter(
      Map<String, Object> variables, List<ComponentAPIFacets.Filter> filters) {
    log.info(variables.toString());
    SpELEngine engine = new SpELEngine(variables);
    return filters.stream()
        .filter(
            filter -> {
              if (StringUtils.isNotBlank(filter.getCondition())) {
                return engine.isTrue(filter.getCondition());
              }
              return false;
            })
        .findFirst();
  }

  private Map<String, Object> getVariables(Map<String, String> queryParams, String requestPayload) {
    Map<String, Object> variables = new HashMap<>();
    variables.put("body", JsonToolkit.fromJson(requestPayload, Object.class));
    variables.put("query", queryParams);
    return variables;
  }

  @Data
  public static class Config {
    private List<ComponentAPIFacets.Filter> filters;
  }
}
