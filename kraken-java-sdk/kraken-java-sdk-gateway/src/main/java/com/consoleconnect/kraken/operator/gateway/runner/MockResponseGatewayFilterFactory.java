package com.consoleconnect.kraken.operator.gateway.runner;

import com.consoleconnect.kraken.operator.core.model.facet.ComponentAPIFacets;
import com.consoleconnect.kraken.operator.core.toolkit.JsonToolkit;
import java.nio.charset.StandardCharsets;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.server.reactive.ServerHttpResponse;
import reactor.core.publisher.Mono;

@Slf4j
public class MockResponseGatewayFilterFactory
    extends AbstractGatewayFilterFactory<ComponentAPIFacets.Action> implements MappingTransformer {

  @Override
  public GatewayFilter apply(ComponentAPIFacets.Action config) {
    return (exchange, chain) -> {
      log.info("Running mock response action: {}", config.getActionType());
      Optional<Map<String, Object>> contextOptional =
          AbstractActionRunner.generateActionContext(exchange, config);
      Boolean forwardDownstream = forwardDownstream(contextOptional.get(), config);
      if (contextOptional.isPresent() && !Boolean.TRUE.equals(forwardDownstream)) {
        ServerHttpResponse httpResponse = exchange.getResponse();
        Map<String, Object> context = contextOptional.get();
        Integer statusCode = (Integer) context.getOrDefault("statusCode", 200);
        Map<String, String> headers = (Map<String, String>) context.get("headers");
        String body =
            context.get("body") instanceof String str
                ? str
                : JsonToolkit.toJson(context.get("body"));

        log.info("Mock response status code: {}", statusCode);
        log.info("Mock response headers: {}", headers);
        log.info("Mock response body: {}", body);

        httpResponse.setStatusCode(org.springframework.http.HttpStatus.valueOf(statusCode));
        if (headers != null) {
          headers.forEach((key, value) -> httpResponse.getHeaders().set(key, value));
        }
        byte[] bytes = (body == null ? "{}" : body).getBytes(StandardCharsets.UTF_8);
        DataBuffer buffer = httpResponse.bufferFactory().wrap(bytes);
        return httpResponse.writeWith(Mono.just(buffer));
      } else {
        log.info("No context found for action: {}", config.getActionType());
        return chain.filter(exchange);
      }
    };
  }
}
