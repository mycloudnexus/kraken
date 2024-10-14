package com.consoleconnect.kraken.operator.gateway.filter;

import com.consoleconnect.kraken.operator.core.entity.ApiActivityLogEntity;
import com.consoleconnect.kraken.operator.core.enums.SyncStatusEnum;
import com.consoleconnect.kraken.operator.core.model.AppProperty;
import com.consoleconnect.kraken.operator.core.repo.ApiActivityLogRepository;
import com.consoleconnect.kraken.operator.gateway.service.FilterHeaderService;
import java.util.Optional;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@AllArgsConstructor
public abstract class AbstractGlobalFilter implements GlobalFilter, Ordered {

  protected final AppProperty appProperty;
  protected final ApiActivityLogRepository apiActivityLogRepository;
  protected final FilterHeaderService filterHeaderService;

  public ApiActivityLogEntity generateHttpRequestEntity(ServerWebExchange exchange) {
    String seq =
        (String)
            exchange
                .getAttributes()
                .getOrDefault(KrakenFilterConstants.X_KRAKEN_LOG_CALL_SEQ, "-1");
    ApiActivityLogEntity entity = new ApiActivityLogEntity();
    entity.setRequestId((String) exchange.getAttribute(KrakenFilterConstants.X_LOG_REQUEST_ID));
    entity.setPath(exchange.getRequest().getURI().getPath());
    entity.setMethod(exchange.getRequest().getMethod().name());
    entity.setUri(exchange.getRequest().getURI().getHost());
    entity.setCallSeq(Integer.parseInt(seq) + 1);
    entity.setSyncStatus(SyncStatusEnum.UNDEFINED);
    exchange
        .getAttributes()
        .put(KrakenFilterConstants.X_KRAKEN_LOG_CALL_SEQ, String.valueOf(entity.getCallSeq()));
    return entity;
  }

  public Optional<ApiActivityLogEntity> find(
      String key, ServerWebExchange exchange, ApiActivityLogRepository apiActivityLogRepository) {
    String entityId = exchange.getAttribute(key);
    if (entityId == null) {
      log.error("AbstractGlobalFilter: entityId is null");
      return Optional.empty();
    }
    log.info("AbstractGlobalFilter update entity:{}", entityId);
    return apiActivityLogRepository.findById(UUID.fromString(entityId));
  }

  boolean canFilter(ServerWebExchange exchange) {
    return appProperty.getFilterPaths().stream()
        .map(path -> exchange.getRequest().getPath().toString().startsWith(path))
        .findAny()
        .isPresent();
  }

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
    if (!canFilter(exchange)) {
      return chain.filter(exchange);
    } else {
      return filterInternal(exchange, chain);
    }
  }

  abstract Mono<Void> filterInternal(ServerWebExchange exchange, GatewayFilterChain chain);
}
