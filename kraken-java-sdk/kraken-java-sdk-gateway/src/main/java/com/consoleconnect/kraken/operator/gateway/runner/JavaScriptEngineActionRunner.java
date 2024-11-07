package com.consoleconnect.kraken.operator.gateway.runner;

import com.consoleconnect.kraken.operator.core.enums.ActionTypeEnum;
import com.consoleconnect.kraken.operator.core.exception.KrakenException;
import com.consoleconnect.kraken.operator.core.model.AppProperty;
import com.consoleconnect.kraken.operator.core.model.facet.ComponentAPIFacets;
import com.consoleconnect.kraken.operator.core.toolkit.JsonToolkit;
import com.consoleconnect.kraken.operator.gateway.template.JavaScriptEngine;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerWebExchange;

@Service
@Slf4j
public class JavaScriptEngineActionRunner extends AbstractActionRunner {

  private final JavaScriptEngine javaScriptEngine;
  public static final String ERROR_MSG_VAR = "errorMsg";

  public JavaScriptEngineActionRunner(AppProperty appProperty, JavaScriptEngine javaScriptEngine) {
    super(appProperty);
    this.javaScriptEngine = javaScriptEngine;
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
    handleErrorMsg(resultInJson);
    outputs.put(action.getOutputKey(), resultInJson);
    return Optional.empty();
  }

  private void handleErrorMsg(String resultJson) {
    Map<String, Object> map = JsonToolkit.fromJson(resultJson, Map.class);
    String errorMsg = (String) map.get(ERROR_MSG_VAR);
    if (StringUtils.isNotBlank(errorMsg)) {
      throw KrakenException.badRequest(errorMsg);
    }
  }
}
