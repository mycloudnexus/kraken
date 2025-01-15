package com.consoleconnect.kraken.operator.gateway.filter;

import static com.consoleconnect.kraken.operator.gateway.filter.KrakenFilterConstants.GATEWAY_SERVICE;
import static com.consoleconnect.kraken.operator.gateway.filter.KrakenFilterConstants.X_KRAKEN_URL;
import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR;
import static org.springframework.core.io.buffer.DataBufferUtils.join;

import com.consoleconnect.kraken.operator.core.entity.ApiActivityLogEntity;
import com.consoleconnect.kraken.operator.core.model.ApiActivityRequestLog;
import com.consoleconnect.kraken.operator.core.model.AppProperty;
import com.consoleconnect.kraken.operator.core.repo.ApiActivityLogRepository;
import com.consoleconnect.kraken.operator.core.service.ApiActivityLogService;
import com.consoleconnect.kraken.operator.gateway.service.BackendApiActivityLogService;
import com.consoleconnect.kraken.operator.gateway.service.FilterHeaderService;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Optional;
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

  private final BackendApiActivityLogService backendApiActivityLogService;

  public BackendServerRequestLogFilter(
      AppProperty appProperty,
      ApiActivityLogRepository apiActivityLogRepository,
      FilterHeaderService filterHeaderService,
      ApiActivityLogService apiActivityLogService,
      BackendApiActivityLogService backendApiActivityLogService) {
    super(appProperty, apiActivityLogRepository, filterHeaderService, apiActivityLogService);
    this.backendApiActivityLogService = backendApiActivityLogService;
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
      Map<String, String> headers =
          filterHeaderService.filterHeaders(exchange.getRequest().getHeaders().toSingleValueMap());
      String uri = exchange.getRequest().getURI().getHost();
      Object url = exchange.getAttributes().get(X_KRAKEN_URL);
      if (url != null) {
        uri = (String) url;
      } else {
        Route route = exchange.getRequiredAttribute(GATEWAY_ROUTE_ATTR);
        if (route != null) {
          uri = route.getUri().getHost();
        }
      }

      ApiActivityRequestLog requestLog =
          ApiActivityRequestLog.builder()
              .requestId((String) exchange.getAttribute(KrakenFilterConstants.X_LOG_REQUEST_ID))
              .callSeq(getAndUpdateCallSeq(exchange))
              .uri(uri)
              .path(exchange.getRequest().getURI().getPath())
              .method(exchange.getRequest().getMethod().name())
              .queryParameters(exchange.getRequest().getQueryParams().toSingleValueMap())
              .headers(headers)
              .requestIp(GATEWAY_SERVICE)
              .responseIp(exchange.getRequest().getRemoteAddress().getHostName())
              .build();
      ApiActivityLogEntity entity = backendApiActivityLogService.logApiActivityRequest(requestLog);

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

      String request = null;
      if (db != null) {
        String payload = db.toString(Charset.defaultCharset());
        if (StringUtils.isNotBlank(payload)) {
          request = payload;
        }
      }

      String uri = null;
      Route route = exchange.getRequiredAttribute(GATEWAY_ROUTE_ATTR);
      if (route != null) {
        uri = route.getUri().getHost();
      }

      ApiActivityRequestLog requestLog =
          ApiActivityRequestLog.builder()
              .activityRequestLogId(entityId)
              .uri(uri)
              .request(request)
              .build();
      Optional<ApiActivityLogEntity> entity =
          backendApiActivityLogService.logApiActivityRequestPayload(requestLog);
      entity.ifPresent(
          e -> {
            log.info("updateEntity:{}", e.getId());
            exchange
                .getAttributes()
                .put(KrakenFilterConstants.X_LOG_TRANSFORMED_ENTITY_ID, e.getId().toString());
          });
    } catch (Exception e) {
      log.error("tracing backend request error", e);
    }
  }

  @Override
  public int getOrder() {
    return Ordered.LOWEST_PRECEDENCE - 100;
  }
}
