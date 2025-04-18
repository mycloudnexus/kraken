package com.consoleconnect.kraken.operator.gateway.runner;

import com.consoleconnect.kraken.operator.core.enums.ActionTypeEnum;
import com.consoleconnect.kraken.operator.core.model.AppProperty;
import com.consoleconnect.kraken.operator.core.model.facet.ComponentAPIFacets;
import com.consoleconnect.kraken.operator.core.repo.ApiActivityLogRepository;
import com.consoleconnect.kraken.operator.core.service.ApiActivityLogService;
import com.consoleconnect.kraken.operator.core.toolkit.JsonToolkit;
import com.consoleconnect.kraken.operator.gateway.dto.RoutingResultDto;
import com.consoleconnect.kraken.operator.gateway.enhancer.OrderDeleteInventoryInjector;
import com.consoleconnect.kraken.operator.gateway.enhancer.ProductTypeRecorder;
import com.consoleconnect.kraken.operator.gateway.repo.HttpRequestRepository;
import com.consoleconnect.kraken.operator.gateway.template.JavaScriptEngine;
import java.util.Map;
import java.util.Optional;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerWebExchange;

@Service
@Slf4j
public class JavaScriptEngineActionRunner extends AbstractActionRunner
    implements ProductTypeRecorder, OrderDeleteInventoryInjector {

  private final JavaScriptEngine javaScriptEngine;
  @Getter private final ApiActivityLogRepository apiActivityLogRepository;
  @Getter private final HttpRequestRepository httpRequestRepository;
  @Getter private final ApiActivityLogService apiActivityLogService;

  public JavaScriptEngineActionRunner(
      AppProperty appProperty,
      JavaScriptEngine javaScriptEngine,
      ApiActivityLogRepository apiActivityLogRepository,
      HttpRequestRepository httpRequestRepository,
      ApiActivityLogService apiActivityLogService) {
    super(appProperty);
    this.javaScriptEngine = javaScriptEngine;
    this.apiActivityLogRepository = apiActivityLogRepository;
    this.httpRequestRepository = httpRequestRepository;
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
    RoutingResultDto routingResultDto = JsonToolkit.fromJson(resultInJson, RoutingResultDto.class);
    recordProductType(exchange, routingResultDto);
    handleRoutingResult(routingResultDto);
    handleInventoryOfDeleteOrder(inputs, routingResultDto, outputs);
    outputs.put(action.getOutputKey(), JsonToolkit.toJson(routingResultDto));
    return Optional.empty();
  }
}
