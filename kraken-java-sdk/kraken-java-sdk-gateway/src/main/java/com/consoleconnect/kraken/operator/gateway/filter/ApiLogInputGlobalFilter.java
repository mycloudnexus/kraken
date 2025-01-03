package com.consoleconnect.kraken.operator.gateway.filter;

import static com.consoleconnect.kraken.operator.gateway.filter.KrakenFilterConstants.GATEWAY_SERVICE;
import static org.springframework.core.io.buffer.DataBufferUtils.join;

import com.consoleconnect.kraken.operator.auth.jwt.JwtDecoderToolkit;
import com.consoleconnect.kraken.operator.auth.model.AuthDataProperty;
import com.consoleconnect.kraken.operator.core.entity.ApiActivityLogEntity;
import com.consoleconnect.kraken.operator.core.enums.LifeStatusEnum;
import com.consoleconnect.kraken.operator.core.model.AppProperty;
import com.consoleconnect.kraken.operator.core.repo.ApiActivityLogRepository;
import com.consoleconnect.kraken.operator.core.service.ApiActivityLogService;
import com.consoleconnect.kraken.operator.core.toolkit.IpUtils;
import com.consoleconnect.kraken.operator.gateway.service.FilterHeaderService;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class ApiLogInputGlobalFilter extends AbstractGlobalFilter {

  private final AuthDataProperty.ResourceServer resourceServer;

  public ApiLogInputGlobalFilter(
      AppProperty appProperty,
      AuthDataProperty.ResourceServer resourceServer,
      ApiActivityLogRepository apiActivityLogRepository,
      FilterHeaderService filterHeaderService,
      ApiActivityLogService apiActivityLogService) {
    super(appProperty, apiActivityLogRepository, filterHeaderService, apiActivityLogService);
    this.resourceServer = resourceServer;
  }

  @Override
  public Mono<Void> filterInternal(ServerWebExchange exchange, GatewayFilterChain chain) {
    exchange
        .getAttributes()
        .put(KrakenFilterConstants.X_LOG_REQUEST_ID, UUID.randomUUID().toString());
    recordRequestParameter(exchange);
    ServerHttpRequestDecorator requestMutated =
        new ServerHttpRequestDecorator(exchange.getRequest()) {
          @Override
          public Flux<DataBuffer> getBody() {
            return Flux.from(join(super.getBody()).doOnNext(db -> recordRequestBody(exchange, db)));
          }
        };
    ServerWebExchange serverWebExchange = exchange.mutate().request(requestMutated).build();
    return chain.filter(serverWebExchange);
  }

  private void recordRequestParameter(ServerWebExchange exchange) {
    try {
      ApiActivityLogEntity entity = generateHttpRequestEntity(exchange);
      entity.setQueryParameters(exchange.getRequest().getQueryParams().toSingleValueMap());
      Map<String, String> headers = exchange.getRequest().getHeaders().toSingleValueMap();
      entity.setHeaders(filterHeaderService.filterHeaders(headers));
      entity.setQueryParameters(exchange.getRequest().getQueryParams().toSingleValueMap());
      entity.setRequestIp(IpUtils.getIP(exchange.getRequest()));
      entity.setResponseIp(GATEWAY_SERVICE);
      entity.setLifeStatus(LifeStatusEnum.LIVE);
      entity.setBuyer(readBuyer(headers));
      this.apiActivityLogService.save(entity);
      log.info("createdEntity:{}", entity.getId());
      exchange
          .getAttributes()
          .put(KrakenFilterConstants.X_LOG_ENTITY_ID, entity.getId().toString());
    } catch (Exception e) {
      log.error(" tracing original request,error ", e);
    }
  }

  private String readBuyer(Map<String, String> headers) {
    String token = headers.getOrDefault(resourceServer.getBearerTokenHeaderName(), "");
    return JwtDecoderToolkit.decodeJWTToken(token)
        .map(dto -> dto.getPayload().getSub())
        .orElse(null);
  }

  private void recordRequestBody(ServerWebExchange exchange, DataBuffer db) {
    try {
      String entityId =
          (String) exchange.getAttributes().get(KrakenFilterConstants.X_LOG_ENTITY_ID);
      ApiActivityLogEntity entity =
          apiActivityLogRepository.findById(UUID.fromString(entityId)).orElse(null);
      if (db != null) {
        String requestPayload = db.toString(Charset.defaultCharset());
        if (StringUtils.isNotBlank(requestPayload)) {
          entity.setRequest(requestPayload);
        }
      }
      this.apiActivityLogService.save(entity);
      log.info("updateEntity:{}", entity.getId());
    } catch (Exception e) {
      log.error(" tracing original request,error ", e);
    }
  }

  @Override
  public int getOrder() {
    return -1;
  }
}
