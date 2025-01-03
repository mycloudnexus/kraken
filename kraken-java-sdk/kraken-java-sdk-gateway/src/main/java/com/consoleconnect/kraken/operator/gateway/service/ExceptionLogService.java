package com.consoleconnect.kraken.operator.gateway.service;

import com.consoleconnect.kraken.operator.core.entity.ApiActivityLogEntity;
import com.consoleconnect.kraken.operator.core.exception.KrakenExceptionHandler;
import com.consoleconnect.kraken.operator.core.repo.ApiActivityLogRepository;
import com.consoleconnect.kraken.operator.core.service.ApiActivityLogService;
import com.consoleconnect.kraken.operator.gateway.filter.KrakenFilterConstants;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerWebExchange;

@Service
@AllArgsConstructor
public class ExceptionLogService implements InitializingBean {
  private final KrakenExceptionHandler krakenExceptionHandler;
  private final ApiActivityLogRepository apiActivityLogRepository;
  private final ApiActivityLogService apiActivityLogService;

  @Override
  public void afterPropertiesSet() throws Exception {
    krakenExceptionHandler.registerCallback(
        (request, errorMap, httpStatusCode) -> {
          ServerWebExchange exchange = request.exchange();
          Object entityId = exchange.getAttribute(KrakenFilterConstants.X_LOG_ENTITY_ID);
          if (entityId != null) {
            apiActivityLogRepository
                .findById(UUID.fromString(entityId.toString()))
                .ifPresent(
                    entity -> {
                      if (entity.getHttpStatusCode() == null && entity.getResponse() == null) {
                        updateEntity(httpStatusCode.value(), errorMap, entity);
                      }
                    });
          }
          Object transformedEntityId =
              exchange.getAttribute(KrakenFilterConstants.X_LOG_TRANSFORMED_ENTITY_ID);
          if (transformedEntityId != null) {
            apiActivityLogRepository
                .findById(UUID.fromString(transformedEntityId.toString()))
                .ifPresent(
                    entity -> {
                      if (entity.getHttpStatusCode() == null && entity.getResponse() == null) {
                        updateEntity(httpStatusCode.value(), errorMap, entity);
                      }
                    });
          }
        });
  }

  private void updateEntity(int code, Object errorMap, ApiActivityLogEntity entity) {
    entity.setResponse(errorMap);
    entity.setHttpStatusCode(code);
    apiActivityLogService.save(entity);
  }
}
