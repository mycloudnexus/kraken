package com.consoleconnect.kraken.operator.gateway.runner;

import com.consoleconnect.kraken.operator.core.model.AppProperty;
import com.consoleconnect.kraken.operator.core.model.facet.ComponentAPIFacets;
import com.consoleconnect.kraken.operator.core.toolkit.JsonToolkit;
import com.consoleconnect.kraken.operator.gateway.filter.KrakenFilterConstants;
import com.consoleconnect.kraken.operator.gateway.template.SpELEngine;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.server.ServerWebExchange;

@Getter
@AllArgsConstructor
@Slf4j
public abstract class AbstractActionRunner implements ResponseCodeTransform, MappingTransformer {

  public static final String ATTRIBUTE_KEY_PREFIX = "x-kraken-";

  private final AppProperty appProperty;

  public abstract boolean canHandle(ComponentAPIFacets.Action action);

  public final Optional<ServerWebExchange> run(
      ServerWebExchange exchange, ComponentAPIFacets.Action action) {
    log.info("run action:{}", action);
    Optional<Map<String, Object>> contextOptional =
        generateActionContext(exchange, action, appProperty.getEnv());
    if (contextOptional.isEmpty()) {
      log.warn("context is empty,ignore the action,{}", action.getActionType());
      return Optional.empty();
    }
    Map<String, Object> inputs = contextOptional.get();

    Map<String, Object> outputs = new HashMap<>();
    Optional<ServerWebExchange> serverWebExchangeOptional =
        runIt(exchange, action, inputs, outputs);
    log.info("outputs:{}", outputs);
    for (Map.Entry<String, Object> entry : outputs.entrySet()) {
      addAttribute(exchange, entry.getKey(), entry.getValue());
    }
    return serverWebExchangeOptional;
  }

  protected abstract Optional<ServerWebExchange> runIt(
      ServerWebExchange exchange,
      ComponentAPIFacets.Action action,
      Map<String, Object> inputs,
      Map<String, Object> outputs);

  protected void addAttribute(ServerWebExchange exchange, String key, Object value) {
    if (value == null) {
      return;
    }
    if (!key.startsWith(ATTRIBUTE_KEY_PREFIX)) key = ATTRIBUTE_KEY_PREFIX + key;
    exchange.getAttributes().put(key, value);
  }

  public static Optional<Map<String, Object>> generateActionContext(
      ServerWebExchange exchange, ComponentAPIFacets.Action action) {
    return generateActionContext(exchange, action, new HashMap<>());
  }

  public static Optional<Map<String, Object>> generateActionContext(
      ServerWebExchange exchange, ComponentAPIFacets.Action action, Map<String, Object> env) {

    Map<String, Object> context = new HashMap<>();
    if (env != null) context.put("env", env);

    flowToContext(exchange, context, KrakenFilterConstants.X_ORIGINAL_REQUEST_BODY, "body");

    Map<String, String> query = exchange.getRequest().getQueryParams().toSingleValueMap();
    context.put("query", query);
    context.put("mefRequestBody", context.get("body"));
    context.put("path", exchange.getRequest().getPath().toString());
    context.put("method", exchange.getRequest().getMethod().name());
    exchange
        .getAttributes()
        .forEach(
            (k, v) -> {
              if (k.toLowerCase().startsWith(ATTRIBUTE_KEY_PREFIX)) {
                String value = exchange.getAttribute(k);
                String key = k.substring(ATTRIBUTE_KEY_PREFIX.length());
                try {
                  context.put(key, JsonToolkit.fromJson(value, Object.class));
                } catch (Exception e) {
                  context.put(key, value);
                }
              }
            });
    if (action.getWith() != null) {
      context.putAll(action.getWith());
    }

    if (action.getCondition() != null) {
      log.info("condition:{}", action.getCondition());
      boolean result = SpELEngine.evaluate(action.getCondition(), context, Boolean.class);
      if (!result) {
        log.warn("condition not met, skip the expression");
        return Optional.empty();
      }
    }
    if (action.getEnv() != null) {
      action
          .getEnv()
          .forEach(
              (k, v) -> {
                Object obj = SpELEngine.evaluate(v, context, Object.class);
                if (obj instanceof String strObj) {
                  obj = toJsonObject(strObj);
                }
                context.put(k, obj);
              });
    }

    flowToContext(
        exchange,
        context,
        KrakenFilterConstants.X_KRAKEN_TARGET_VALUE_MAPPER,
        TARGET_VALUE_MAPPER_KEY);

    return Optional.of(context);
  }

  public static void flowToContext(
      ServerWebExchange exchange, Map<String, Object> context, String fromKey, String toKey) {
    String fromKeyValue = exchange.getAttribute(fromKey);
    if (Objects.nonNull(fromKeyValue)) {
      context.put(toKey, JsonToolkit.fromJson(fromKeyValue, Object.class));
    }
  }

  public static Optional<Object> readCachedData(String key, ServerWebExchange exchange) {
    String payload = exchange.getAttribute(key);
    if (payload == null) {
      return Optional.empty();
    }
    try {
      return Optional.of(JsonToolkit.fromJson(payload, Object.class));
    } catch (Exception e) {
      log.error("DBCrudFilterFactory: failed to read cached data", e);
      return Optional.empty();
    }
  }

  private static Object toJsonObject(String value) {
    try {
      return JsonToolkit.fromJson(value, Object.class);
    } catch (Exception e) {
      return value;
    }
  }
}
