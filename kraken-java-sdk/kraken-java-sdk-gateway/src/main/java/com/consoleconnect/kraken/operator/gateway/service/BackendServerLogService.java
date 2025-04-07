package com.consoleconnect.kraken.operator.gateway.service;

import static com.consoleconnect.kraken.operator.gateway.filter.KrakenFilterConstants.GATEWAY_SERVICE;
import static com.consoleconnect.kraken.operator.gateway.filter.KrakenFilterConstants.X_KRAKEN_URL;
import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR;

import com.consoleconnect.kraken.operator.core.entity.ApiActivityLogEntity;
import com.consoleconnect.kraken.operator.core.model.ApiActivityRequestLog;
import com.consoleconnect.kraken.operator.core.model.ApiActivityResponseLog;
import com.consoleconnect.kraken.operator.core.model.AppProperty;
import com.consoleconnect.kraken.operator.core.repo.ApiActivityLogRepository;
import com.consoleconnect.kraken.operator.core.service.ApiActivityLogService;
import com.consoleconnect.kraken.operator.gateway.filter.KrakenFilterConstants;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerWebExchange;

@Service
@AllArgsConstructor
@Slf4j
public class BackendServerLogService {
  protected final AppProperty appProperty;
  protected final ApiActivityLogRepository apiActivityLogRepository;
  protected final FilterHeaderService filterHeaderService;
  protected final ApiActivityLogService apiActivityLogService;
  private final BackendApiActivityLogService backendApiActivityLogService;

  public void recordRequestParameter(ServerWebExchange exchange) {
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
              .responseIp(
                  exchange.getRequest().getRemoteAddress() != null
                      ? exchange.getRequest().getRemoteAddress().getHostName()
                      : null)
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

  public void recordRequestBody(ServerWebExchange exchange, Supplier<String> fnGetRequest) {
    try {
      String entityId =
          (String) exchange.getAttributes().get(KrakenFilterConstants.X_LOG_TRANSFORMED_ENTITY_ID);

      String request = null;
      String payload = fnGetRequest.get();
      if (StringUtils.isNotBlank(payload)) {
        request = payload;
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

  public void recordResponse(ServerWebExchange exchange, Supplier<String> fnGetResponse) {
    Optional<ApiActivityLogEntity> updatedEntityOptional =
        find(KrakenFilterConstants.X_LOG_TRANSFORMED_ENTITY_ID, exchange, apiActivityLogRepository);
    if (updatedEntityOptional.isEmpty()) {
      return;
    }
    ApiActivityLogEntity updatedEntity = updatedEntityOptional.get();
    String response = null;
    String responseStr = fnGetResponse.get();
    log.info(" the backend response is {}", responseStr);
    if (StringUtils.isNoneBlank(responseStr)) {
      response = responseStr;
    }
    Integer statusCode = Objects.requireNonNull(exchange.getResponse().getStatusCode()).value();
    ApiActivityResponseLog responseLog =
        ApiActivityResponseLog.builder()
            .apiActivityLog(updatedEntity)
            .responseIp(updatedEntity.getUri())
            .response(response)
            .httpStatusCode(statusCode)
            .build();
    backendApiActivityLogService.logApiActivityResponse(responseLog);
  }

  private Optional<ApiActivityLogEntity> find(
      String key, ServerWebExchange exchange, ApiActivityLogRepository apiActivityLogRepository) {
    String entityId = exchange.getAttribute(key);
    if (entityId == null) {
      log.error("AbstractGlobalFilter: entityId is null");
      return Optional.empty();
    }
    log.info("AbstractGlobalFilter update entity:{}", entityId);
    return apiActivityLogRepository.findById(UUID.fromString(entityId));
  }

  protected int getAndUpdateCallSeq(ServerWebExchange exchange) {
    String attrSeq =
        (String)
            exchange
                .getAttributes()
                .getOrDefault(KrakenFilterConstants.X_KRAKEN_LOG_CALL_SEQ, "-1");
    int seq = Integer.parseInt(attrSeq) + 1;
    exchange.getAttributes().put(KrakenFilterConstants.X_KRAKEN_LOG_CALL_SEQ, String.valueOf(seq));

    return seq;
  }
}
