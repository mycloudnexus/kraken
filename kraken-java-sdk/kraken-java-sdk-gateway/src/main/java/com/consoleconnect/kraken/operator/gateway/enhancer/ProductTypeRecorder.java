package com.consoleconnect.kraken.operator.gateway.enhancer;

import com.consoleconnect.kraken.operator.core.entity.ApiActivityLogEntity;
import com.consoleconnect.kraken.operator.core.service.ApiActivityLogService;
import com.consoleconnect.kraken.operator.gateway.dto.RoutingResultDto;
import com.consoleconnect.kraken.operator.gateway.filter.KrakenFilterConstants;
import java.util.Objects;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.server.ServerWebExchange;

public interface ProductTypeRecorder {

  ApiActivityLogService getApiActivityLogService();

  default void recordProductType(ServerWebExchange exchange, RoutingResultDto routingResultDto) {
    Object entityId = exchange.getAttribute(KrakenFilterConstants.X_LOG_ENTITY_ID);
    if (entityId != null) {
      getApiActivityLogService()
          .findById(UUID.fromString(entityId.toString()))
          .ifPresent(
              entity -> {
                if (Objects.nonNull(routingResultDto)
                    && StringUtils.isNotBlank(routingResultDto.getProductType())) {
                  updateProductType(routingResultDto.getProductType(), entity);
                }
              });
    }
  }

  default void updateProductType(String productType, ApiActivityLogEntity entity) {
    entity.setProductType(productType);
    getApiActivityLogService().save(entity);
  }
}
