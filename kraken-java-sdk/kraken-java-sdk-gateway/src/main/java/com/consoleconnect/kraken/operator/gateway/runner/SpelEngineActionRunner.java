package com.consoleconnect.kraken.operator.gateway.runner;

import com.consoleconnect.kraken.operator.core.enums.ActionTypeEnum;
import com.consoleconnect.kraken.operator.core.model.AppProperty;
import com.consoleconnect.kraken.operator.core.model.facet.ComponentAPIFacets;
import com.consoleconnect.kraken.operator.gateway.template.SpELEngine;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerWebExchange;

@Service
@Slf4j
public class SpelEngineActionRunner extends AbstractActionRunner {
  public static final String INPUT_CODE = "code";
  public static final String INPUTS = "inputs";

  public SpelEngineActionRunner(AppProperty appProperty) {
    super(appProperty);
  }

  @Override
  public boolean canHandle(ComponentAPIFacets.Action action) {
    return ActionTypeEnum.ENGINE_SPEL == ActionTypeEnum.fromString(action.getActionType());
  }

  @Override
  public Optional<ServerWebExchange> runIt(
      ServerWebExchange exchange,
      ComponentAPIFacets.Action action,
      Map<String, Object> inputs,
      Map<String, Object> outputs) {
    String code = (String) inputs.get(INPUT_CODE);
    String resultInJson = SpELEngine.evaluate(code, inputs, String.class);
    log.info("resultInJson:{}", resultInJson);
    outputs.put(action.getOutputKey(), resultInJson);
    return Optional.empty();
  }
}
