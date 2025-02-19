package com.consoleconnect.kraken.operator.gateway.runner;

import com.consoleconnect.kraken.operator.core.entity.ApiActivityLogEntity;
import com.consoleconnect.kraken.operator.core.enums.ActionTypeEnum;
import com.consoleconnect.kraken.operator.core.model.AppProperty;
import com.consoleconnect.kraken.operator.core.model.facet.ComponentAPIFacets;
import com.consoleconnect.kraken.operator.core.repo.ApiActivityLogRepository;
import com.consoleconnect.kraken.operator.core.service.ApiActivityLogService;
import com.consoleconnect.kraken.operator.core.toolkit.JsonToolkit;
import com.consoleconnect.kraken.operator.gateway.dto.RoutingResultDto;
import com.consoleconnect.kraken.operator.gateway.filter.KrakenFilterConstants;
import com.consoleconnect.kraken.operator.gateway.template.JavaScriptEngine;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerWebExchange;

@Service
@Slf4j
public class JavaScriptEngineActionRunner extends AbstractActionRunner {

  private final JavaScriptEngine javaScriptEngine;
  private final ApiActivityLogRepository apiActivityLogRepository;
  private final ApiActivityLogService apiActivityLogService;

  public JavaScriptEngineActionRunner(
      AppProperty appProperty,
      JavaScriptEngine javaScriptEngine,
      ApiActivityLogRepository apiActivityLogRepository,
      ApiActivityLogService apiActivityLogService) {
    super(appProperty);
    this.javaScriptEngine = javaScriptEngine;
    this.apiActivityLogRepository = apiActivityLogRepository;
    this.apiActivityLogService = apiActivityLogService;
  }

  @Override
  public boolean canHandle(ComponentAPIFacets.Action action) {
    return ActionTypeEnum.ENGINE_JAVASCRIPT == ActionTypeEnum.fromString(action.getActionType());
  }

  @Override
  public Optional<ServerWebExchange> runIt(
      ServerWebExchange exchange,
      ComponentAPIFacets.Action action,
      Map<String, Object> inputs,
      Map<String, Object> outputs) {
    String scriptId = action.getId() + ".js";
    String code = (String) inputs.get(SpelEngineActionRunner.INPUT_CODE);
    javaScriptEngine.addSourceIfNotPresent(scriptId, code);
    String resultInJson = javaScriptEngine.execute(scriptId, inputs);
    log.info("resultInJson:{}", resultInJson);
    recordProductType(exchange, resultInJson);
    handleRoutingResult(resultInJson);
    outputs.put(action.getOutputKey(), resultInJson);
    return Optional.empty();
  }

  public void recordProductType(ServerWebExchange exchange, String resultJson) {
    RoutingResultDto routingResultDto = JsonToolkit.fromJson(resultJson, RoutingResultDto.class);
    Object entityId = exchange.getAttribute(KrakenFilterConstants.X_LOG_ENTITY_ID);
    if (entityId != null) {
      apiActivityLogRepository
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

  private void updateProductType(String productType, ApiActivityLogEntity entity) {
    entity.setProductType(productType);
    apiActivityLogService.save(entity);
  }
}
