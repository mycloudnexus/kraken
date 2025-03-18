package com.consoleconnect.kraken.operator.gateway.filter;

import static com.consoleconnect.kraken.operator.core.toolkit.DataBufferUtil.convert2String;
import static org.springframework.core.io.buffer.DataBufferUtils.join;

import com.consoleconnect.kraken.operator.core.model.AppProperty;
import com.consoleconnect.kraken.operator.core.repo.ApiActivityLogRepository;
import com.consoleconnect.kraken.operator.core.service.ApiActivityLogService;
import com.consoleconnect.kraken.operator.gateway.service.BackendServerLogService;
import com.consoleconnect.kraken.operator.gateway.service.FilterHeaderService;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
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

  private final BackendServerLogService backendServerLogService;

  public BackendServerResponseLogFilter(
      AppProperty appProperty,
      ApiActivityLogRepository apiActivityLogRepository,
      FilterHeaderService filterHeaderService,
      ApiActivityLogService apiActivityLogService,
      BackendServerLogService backendServerLogService) {
    super(appProperty, apiActivityLogRepository, filterHeaderService, apiActivityLogService);
    this.backendServerLogService = backendServerLogService;
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
                      backendServerLogService.recordResponse(
                          exchange,
                          () -> {
                            try {
                              return convert2String(db, exchange);
                            } catch (IOException e) {
                              return "";
                            }
                          });
                      return this.getDelegate().writeWith(Mono.just(db));
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
