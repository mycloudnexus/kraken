package com.consoleconnect.kraken.operator.gateway.filter;

import static com.consoleconnect.kraken.operator.core.toolkit.DataBufferUtil.convert2String;
import static org.springframework.core.io.buffer.DataBufferUtils.join;

import com.consoleconnect.kraken.operator.core.entity.ApiActivityLogEntity;
import com.consoleconnect.kraken.operator.core.model.ApiActivityResponseLog;
import com.consoleconnect.kraken.operator.core.model.AppProperty;
import com.consoleconnect.kraken.operator.core.repo.ApiActivityLogRepository;
import com.consoleconnect.kraken.operator.core.service.ApiActivityLogService;
import com.consoleconnect.kraken.operator.gateway.service.BackendApiActivityLogService;
import com.consoleconnect.kraken.operator.gateway.service.FilterHeaderService;
import java.util.Objects;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.reactivestreams.Publisher;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class BackendServerResponseLogFilter extends AbstractGlobalFilter {

  private final BackendApiActivityLogService backendApiActivityLogService;

  public BackendServerResponseLogFilter(
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
    ServerHttpResponseDecorator responseMutated =
        new ServerHttpResponseDecorator(exchange.getResponse()) {
          @Override
          public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
            return join(body)
                .flatMap(
                    db -> {
                      try {
                        Optional<ApiActivityLogEntity> updatedEntityOptional =
                            find(
                                KrakenFilterConstants.X_LOG_TRANSFORMED_ENTITY_ID,
                                exchange,
                                apiActivityLogRepository);
                        if (updatedEntityOptional.isEmpty()) {
                          return this.getDelegate().writeWith(Mono.just(db));
                        }
                        ApiActivityLogEntity updatedEntity = updatedEntityOptional.get();
                        String response = null;
                        String responseStr = convert2String(db, exchange);
                        log.info(" the backend response is {}", responseStr);
                        if (StringUtils.isNoneBlank(responseStr)) {
                          response = responseStr;
                        }
                        Integer statusCode =
                            Objects.requireNonNull(exchange.getResponse().getStatusCode()).value();
                        ApiActivityResponseLog responseLog =
                            ApiActivityResponseLog.builder()
                                .apiActivityLog(updatedEntity)
                                .responseIp(updatedEntity.getUri())
                                .response(response)
                                .httpStatusCode(statusCode)
                                .build();
                        backendApiActivityLogService.logApiActivityResponse(responseLog);
                        return this.getDelegate().writeWith(Mono.just(db));
                      } catch (Exception e) {
                        log.error("tracing backed response error", e);
                        return this.getDelegate().writeWith(Mono.just(db));
                      }
                    });
          }
        };
    return chain.filter(exchange.mutate().response(responseMutated).build());
  }

  @Override
  public int getOrder() {
    return -1;
  }
}
