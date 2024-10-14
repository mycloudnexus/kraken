package com.consoleconnect.kraken.operator.gateway.filter;

import static com.consoleconnect.kraken.operator.gateway.filter.KrakenFilterConstants.GATEWAY_SERVICE;
import static com.consoleconnect.kraken.operator.gateway.filter.KrakenFilterConstants.X_KRAKEN_URL;
import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR;
import static org.springframework.core.io.buffer.DataBufferUtils.join;

import com.consoleconnect.kraken.operator.core.entity.ApiActivityLogEntity;
import com.consoleconnect.kraken.operator.core.model.AppProperty;
import com.consoleconnect.kraken.operator.core.repo.ApiActivityLogRepository;
import com.consoleconnect.kraken.operator.gateway.service.FilterHeaderService;
import java.nio.charset.Charset;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class BackendServerRequestLogFilter extends AbstractGlobalFilter {
  public BackendServerRequestLogFilter(
      AppProperty appProperty,
      ApiActivityLogRepository apiActivityLogRepository,
      FilterHeaderService filterHeaderService) {
    super(appProperty, apiActivityLogRepository, filterHeaderService);
  }

  @Override
  public Mono<Void> filterInternal(ServerWebExchange exchange, GatewayFilterChain chain) {
    recordRequestParameter(exchange);
    ServerHttpRequestDecorator requestMutated =
        new ServerHttpRequestDecorator(exchange.getRequest()) {

          @Override
          public Flux<DataBuffer> getBody() {
            return Flux.from(join(super.getBody()).doOnNext(db -> recordRequestBody(exchange, db)));
          }
        };
    return chain.filter(exchange.mutate().request(requestMutated).build());
  }

  private void recordRequestParameter(ServerWebExchange exchange) {
    try {
      final ApiActivityLogEntity entity = generateHttpRequestEntity(exchange);
      entity.setQueryParameters(exchange.getRequest().getQueryParams().toSingleValueMap());
      entity.setHeaders(
          filterHeaderService.filterHeaders(exchange.getRequest().getHeaders().toSingleValueMap()));
      entity.setRequestIp(GATEWAY_SERVICE);
      entity.setResponseIp(exchange.getRequest().getRemoteAddress().getHostName());
      Route route = exchange.getRequiredAttribute(GATEWAY_ROUTE_ATTR);
      Object url = exchange.getAttributes().get(X_KRAKEN_URL);
      if (url != null) {
        entity.setUri(url.toString());
      } else {
        if (route != null) {
          entity.setUri(route.getUri().getHost());
        }
      }

      apiActivityLogRepository.save(entity);
      log.info("createdEntity:{}", entity.getId());
      exchange
          .getAttributes()
          .put(KrakenFilterConstants.X_LOG_TRANSFORMED_ENTITY_ID, entity.getId().toString());
    } catch (Exception e) {
      log.error("tracing backend request error", e);
    }
  }

  private void recordRequestBody(ServerWebExchange exchange, DataBuffer db) {
    try {
      String entityId =
          (String) exchange.getAttributes().get(KrakenFilterConstants.X_LOG_TRANSFORMED_ENTITY_ID);
      ApiActivityLogEntity entity =
          apiActivityLogRepository.findById(UUID.fromString(entityId)).orElse(null);

      if (db != null) {
        String requestPayload = db.toString(Charset.defaultCharset());
        if (StringUtils.isNotBlank(requestPayload)) {
          entity.setRequest(requestPayload);
        }
      }
      Route route = exchange.getRequiredAttribute(GATEWAY_ROUTE_ATTR);
      if (route != null) {
        entity.setUri(route.getUri().getHost());
      }
      apiActivityLogRepository.save(entity);
      log.info("updateEntity:{}", entity.getId());
      exchange
          .getAttributes()
          .put(KrakenFilterConstants.X_LOG_TRANSFORMED_ENTITY_ID, entity.getId().toString());
    } catch (Exception e) {
      log.error("tracing backend request error", e);
    }
  }

  @Override
  public int getOrder() {
    return Ordered.LOWEST_PRECEDENCE - 100;
  }
}
