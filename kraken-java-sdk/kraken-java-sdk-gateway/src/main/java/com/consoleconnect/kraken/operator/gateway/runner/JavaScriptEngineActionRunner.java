package com.consoleconnect.kraken.operator.gateway.runner;

import com.consoleconnect.kraken.operator.core.enums.ActionTypeEnum;
import com.consoleconnect.kraken.operator.core.exception.KrakenException;
import com.consoleconnect.kraken.operator.core.model.AppProperty;
import com.consoleconnect.kraken.operator.core.model.facet.ComponentAPIFacets;
import com.consoleconnect.kraken.operator.core.toolkit.JsonToolkit;
import com.consoleconnect.kraken.operator.gateway.template.JavaScriptEngine;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerWebExchange;

@Service
@Slf4j
public class JavaScriptEngineActionRunner extends AbstractActionRunner {

  private final JavaScriptEngine javaScriptEngine;
  private static final String ERROR_MSG_VAR = "errorMsg";
  private static final String ERROR_CODE_VAR = "errorCode";

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
    Map<String, Object> map =
        JsonToolkit.fromJson(resultJson, new TypeReference<Map<String, Object>>() {});
    String errorMsg = (String) map.get(ERROR_MSG_VAR);
    String errorCode = (String)map.get(ERROR_CODE_VAR);
    log.info("errorCode:{}, errorMsg:{}", errorCode, errorMsg);
    int errorCodeInt = StringUtils.isNotBlank(errorCode)
            ? Integer.parseInt(errorCode)
            : HttpStatus.BAD_REQUEST.value();
    if (StringUtils.isNotBlank(errorMsg)) {
      if (HttpStatus.BAD_REQUEST.value() == errorCodeInt) {
        throw KrakenException.badRequestInvalidBody(errorMsg);
      } else if (HttpStatus.UNPROCESSABLE_ENTITY.value() == errorCodeInt) {
        throw KrakenException.unProcessableEntityInvalidValue(errorMsg);
      }
    }
  }
}
