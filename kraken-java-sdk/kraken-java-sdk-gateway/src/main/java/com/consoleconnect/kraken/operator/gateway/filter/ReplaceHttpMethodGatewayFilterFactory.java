package com.consoleconnect.kraken.operator.gateway.filter;

import java.util.List;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpMethod;

@Slf4j
public class ReplaceHttpMethodGatewayFilterFactory
    extends AbstractGatewayFilterFactory<ReplaceHttpMethodGatewayFilterFactory.Config> {

  private static final String REPLACEMENT = "replacement";

  public ReplaceHttpMethodGatewayFilterFactory() {
    super(Config.class);
  }

  @Override
  public String name() {
    return this.getClass().getSimpleName();
  }

  @Override
  public List<String> shortcutFieldOrder() {
    return List.of(REPLACEMENT);
  }

  @Override
  public GatewayFilter apply(Config config) {
    return (exchange, chain) -> {
      var request = exchange.getRequest();
      log.info(
          "Method transform from {} to {}",
          request.getMethod(),
          config.getReplacement().toString());
      var mutatedExchange =
          exchange
              .mutate()
              .request(request.mutate().method(config.getReplacement()).build())
              .build();
      return chain.filter(mutatedExchange);
    };
  }

  @Getter
  public static class Config {
    private HttpMethod replacement;

    public void setReplacement(String method) {
      this.replacement = HttpMethod.valueOf(method);
    }
  }
}
