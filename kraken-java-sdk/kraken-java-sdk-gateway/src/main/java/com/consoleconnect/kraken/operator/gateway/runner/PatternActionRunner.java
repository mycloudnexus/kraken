package com.consoleconnect.kraken.operator.gateway.runner;

import com.consoleconnect.kraken.operator.core.enums.ActionTypeEnum;
import com.consoleconnect.kraken.operator.core.model.AppProperty;
import com.consoleconnect.kraken.operator.core.model.facet.ComponentAPIFacets;
import com.consoleconnect.kraken.operator.core.service.ApiActivityLogService;
import com.consoleconnect.kraken.operator.gateway.dto.RoutingResultDto;
import com.consoleconnect.kraken.operator.gateway.enhancer.ProductTypeRecorder;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerWebExchange;

@Getter
@Service
@Slf4j
public class PatternActionRunner extends AbstractActionRunner implements ProductTypeRecorder {
  public static final String INPUT_PATTERN = "pattern";
  public static final String INPUT_INPUT = "input";
  private final ApiActivityLogService apiActivityLogService;

  public PatternActionRunner(AppProperty appProperty, ApiActivityLogService apiActivityLogService) {
    super(appProperty);
    this.apiActivityLogService = apiActivityLogService;
  }

  @Override
  public boolean canHandle(ComponentAPIFacets.Action action) {
    return ActionTypeEnum.PATTERN == ActionTypeEnum.fromString(action.getActionType());
  }

  @Override
  protected Optional<ServerWebExchange> runIt(
      ServerWebExchange exchange,
      ComponentAPIFacets.Action action,
      Map<String, Object> inputs,
      Map<String, Object> outputs) {
    String pattern = (String) inputs.get(INPUT_PATTERN);
    String input = (String) inputs.get(INPUT_INPUT);
    Pattern r = Pattern.compile(pattern);
    Matcher m = r.matcher(input);

    if (m.find()) {
      // Get the names of the capture groups from the pattern
      Pattern groupNamesPattern = Pattern.compile("\\(\\?<(.+?)>");
      Matcher groupNamesMatcher = groupNamesPattern.matcher(pattern);

      // Print all the variables that were found
      while (groupNamesMatcher.find()) {
        String variableName = groupNamesMatcher.group(1);
        String variableValue = m.group(variableName);
        outputs.put(variableName, variableValue);
      }
    }
    recordBizType(exchange, inputs);

    return Optional.empty();
  }

  public void recordBizType(ServerWebExchange exchange, Map<String, Object> inputs) {
    Object bizTypeObj = inputs.get("bizType");
    if (Objects.nonNull(bizTypeObj) && "SHARE".equals(bizTypeObj)) {
      RoutingResultDto routingResultDto = new RoutingResultDto();
      routingResultDto.setProductType("SHARE");
      recordProductType(exchange, routingResultDto);
    }
  }
}
