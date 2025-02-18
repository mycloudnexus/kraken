package com.consoleconnect.kraken.operator.gateway.filter;

import static org.springframework.core.io.buffer.DataBufferUtils.join;

import com.consoleconnect.kraken.operator.core.model.AppProperty;
import com.consoleconnect.kraken.operator.core.repo.ApiActivityLogRepository;
import com.consoleconnect.kraken.operator.core.service.ApiActivityLogService;
import com.consoleconnect.kraken.operator.gateway.service.BackendServerLogService;
import com.consoleconnect.kraken.operator.gateway.service.FilterHeaderService;
import java.nio.charset.Charset;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
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

  private final BackendServerLogService backendServerLogService;

  public BackendServerRequestLogFilter(
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
    backendServerLogService.recordRequestParameter(exchange);
    ServerHttpRequestDecorator requestMutated =
        new ServerHttpRequestDecorator(exchange.getRequest()) {

          @Override
          public Flux<DataBuffer> getBody() {
            return Flux.from(
                join(super.getBody())
                    .doOnNext(
                        db ->
                            backendServerLogService.recordRequestBody(
                                exchange,
                                () -> db != null ? db.toString(Charset.defaultCharset()) : null)));
          }
        };
    return chain.filter(exchange.mutate().request(requestMutated).build());
  }

  @Override
  public int getOrder() {
    return Ordered.LOWEST_PRECEDENCE - 100;
  }
}
