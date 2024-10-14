package com.consoleconnect.kraken.operator.gateway.filter;

import static com.consoleconnect.kraken.operator.core.toolkit.DataBufferUtil.convert2String;
import static org.springframework.core.io.buffer.DataBufferUtils.join;

import com.consoleconnect.kraken.operator.core.entity.ApiActivityLogEntity;
import com.consoleconnect.kraken.operator.core.enums.SyncStatusEnum;
import com.consoleconnect.kraken.operator.core.model.AppProperty;
import com.consoleconnect.kraken.operator.core.repo.ApiActivityLogRepository;
import com.consoleconnect.kraken.operator.gateway.service.FilterHeaderService;
import java.util.Objects;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.reactivestreams.Publisher;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class ApiLogOutputGlobalFilter extends AbstractGlobalFilter {

  public ApiLogOutputGlobalFilter(
      AppProperty appProperty,
      ApiActivityLogRepository apiActivityLogRepository,
      FilterHeaderService filterHeaderService) {
    super(appProperty, apiActivityLogRepository, filterHeaderService);
  }

  @Override
  public Mono<Void> filterInternal(ServerWebExchange exchange, GatewayFilterChain chain) {

    ServerHttpResponseDecorator responseMutated =
        new ServerHttpResponseDecorator(exchange.getResponse()) {
          @Override
          public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
            return join(body)
                .flatMap(
                    db -> {
                      try {
                        Optional<ApiActivityLogEntity> apiActivityLogEntityOptional =
                            find(
                                KrakenFilterConstants.X_LOG_ENTITY_ID,
                                exchange,
                                apiActivityLogRepository);
                        if (apiActivityLogEntityOptional.isEmpty()) {
                          log.info("ApiActivityLogEntity is null");
                          return this.getDelegate().writeWith(Mono.just(db));
                        }
                        ApiActivityLogEntity apiActivityLogEntity =
                            apiActivityLogEntityOptional.get();
                        String finalResponse = convert2String(db, exchange);
                        if (StringUtils.isNoneBlank(finalResponse)) {
                          apiActivityLogEntity.setResponse(finalResponse);
                        }
                        apiActivityLogEntity.setHttpStatusCode(
                            Objects.requireNonNull(exchange.getResponse().getStatusCode()).value());
                        apiActivityLogEntity.setSyncStatus(SyncStatusEnum.UNDEFINED);
                        apiActivityLogRepository.save(apiActivityLogEntity);
                        return this.getDelegate().writeWith(Mono.just(db));
                      } catch (Exception e) {
                        log.error("tracing final response error", e);
                        return this.getDelegate().writeWith(Mono.just(db));
                      }
                    });
          }
        };
    return chain
        .filter(exchange.mutate().response(responseMutated).build())
        .doOnError(t -> log.error("error occurs", t));
  }

  @Override
  public int getOrder() {
    return Ordered.HIGHEST_PRECEDENCE;
  }
}
