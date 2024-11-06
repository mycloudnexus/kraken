package com.consoleconnect.kraken.operator.gateway.runner;

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR;

import com.consoleconnect.kraken.operator.core.dto.StateValueMappingDto;
import com.consoleconnect.kraken.operator.core.model.AppProperty;
import com.consoleconnect.kraken.operator.core.model.facet.ComponentAPIFacets;
import com.consoleconnect.kraken.operator.core.toolkit.JsonToolkit;
import com.consoleconnect.kraken.operator.gateway.entity.HttpRequestEntity;
import com.consoleconnect.kraken.operator.gateway.filter.KrakenFilterConstants;
import com.consoleconnect.kraken.operator.gateway.model.HttpResponseContext;
import com.consoleconnect.kraken.operator.gateway.repo.HttpRequestRepository;
import com.consoleconnect.kraken.operator.gateway.service.FilterHeaderService;
import com.consoleconnect.kraken.operator.gateway.template.JavaScriptEngine;
import com.consoleconnect.kraken.operator.gateway.template.SpELEngine;
import java.util.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.reactivestreams.Publisher;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@AllArgsConstructor
@Slf4j
@Data
public abstract class AbstractBodyTransformerFunc
    implements ResponseCodeTransform, MappingTransformer {
  public static final String ENGINE_JAVASCRIPT = "javascript";
  public static final String ENGINE_SPEL = "spel";
  public static final String INPUT_ENGINE = "engine";
  public static final String STATE_MAPPING = "stateMapping";
  public static final String STATE = "state";
  public static final String SUCCESS_STATUS = "successStatus";

  @Getter private final ComponentAPIFacets.Action action;
  private final JavaScriptEngine javaScriptEngine;
  private final HttpRequestRepository httpRequestRepository;
  private final AppProperty appProperty;
  private final FilterHeaderService filterHeaderService;

  public Publisher<String> transform(ServerWebExchange exchange, String s, boolean postRequest) {
    Optional<Map<String, Object>> contextOptional =
        AbstractActionRunner.generateActionContext(exchange, action);
    if (contextOptional.isEmpty()) {
      log.info("context is empty");
      return Mono.just(s);
    }
    Map<String, Object> context = contextOptional.get();
    Optional<Integer> responseStatusOpt;
    if (postRequest) {
      Object expectedSuccessStatus = context.getOrDefault(SUCCESS_STATUS, null);
      HttpResponseContext httpResponseContext = new HttpResponseContext();
      httpResponseContext.setBody(JsonToolkit.fromJson(s, Object.class));
      httpResponseContext.setStatus(
          Objects.requireNonNull(exchange.getResponse().getStatusCode()).value());

      responseStatusOpt = checkResponseCode(httpResponseContext, expectedSuccessStatus);
      context.put("responseBody", JsonToolkit.fromJson(s, Object.class));

      int responseStatus = httpResponseContext.getStatus();
      if (responseStatusOpt.isPresent()) {
        responseStatus = responseStatusOpt.get();
        exchange.getResponse().setStatusCode(HttpStatusCode.valueOf(responseStatus));
      }
      context.put("responseStatus", responseStatus);
    }
    String engine = (String) context.get(INPUT_ENGINE);
    log.info("engine:{}", engine);
    String retJsonString = null;
    if (ENGINE_JAVASCRIPT.equals(engine)) {
      String script = (String) context.get(SpelEngineActionRunner.INPUT_CODE);
      String scriptId = action.getId() + ".js";
      javaScriptEngine.addSourceIfNotPresent(scriptId, script);
      retJsonString = javaScriptEngine.execute(scriptId, context);

    } else if (ENGINE_SPEL.equals(engine)) {
      retJsonString = rendResponseWithSpEl(context, postRequest);
    } else {
      Object expression = context.get(SpelEngineActionRunner.INPUT_CODE);
      if (expression instanceof String exp) {
        retJsonString = exp;
      } else {
        retJsonString = JsonToolkit.toJson(expression);
      }
    }
    log.info("retJsonString:{}", retJsonString);

    if (postRequest) {
      exchange.getAttributes().put(KrakenFilterConstants.X_KRAKEN_RESPONSE_BODY, s);
      exchange
          .getAttributes()
          .put(KrakenFilterConstants.X_KRAKEN_RENDERED_RESPONSE_BODY, retJsonString);
      updateRequest(exchange, s, exchange.getResponse().getStatusCode().value());
    } else {
      createRequest(exchange, retJsonString);
    }

    return Mono.just(retJsonString == null ? StringUtils.EMPTY : retJsonString);
  }

  private void createRequest(ServerWebExchange exchange, String requestBody) {
    final HttpRequestEntity entity = DBCrudActionRunner.generateHttpRequestEntity(exchange);
    entity.setBizType("undefined");
    entity.setQueryParameters(exchange.getRequest().getQueryParams().toSingleValueMap());
    entity.setHeaders(
        filterHeaderService.filterHeaders(exchange.getRequest().getHeaders().toSingleValueMap()));
    entity.setRequest(requestBody == null ? null : JsonToolkit.fromJson(requestBody, Object.class));
    Route route = exchange.getRequiredAttribute(GATEWAY_ROUTE_ATTR);
    entity.setUri(route.getUri().getHost());
    HttpRequestEntity createdEntity = this.httpRequestRepository.save(entity);
    log.info("createdEntity:{}", createdEntity.getId());
    exchange
        .getAttributes()
        .put(KrakenFilterConstants.X_TRANSFORMED_ENTITY_ID, createdEntity.getId().toString());
  }

  protected void updateRequest(
      ServerWebExchange exchange, String responseBody, int responseStatus) {
    String entityId = exchange.getAttribute(KrakenFilterConstants.X_TRANSFORMED_ENTITY_ID);
    if (entityId == null) {
      log.error("BodyTransformer: entityId is null");
      return;
    }
    log.info("update entity:{}", entityId);
    Optional<HttpRequestEntity> optionalEntity =
        httpRequestRepository.findById(UUID.fromString(entityId));
    if (optionalEntity.isEmpty()) {
      log.error("BodyTransformer: entity not found");
      return;
    }
    HttpRequestEntity updatedEntity = optionalEntity.get();
    updatedEntity.setResponse(JsonToolkit.fromJson(responseBody, Object.class));
    updatedEntity.setHttpStatusCode(responseStatus);
    this.httpRequestRepository.save(updatedEntity);
  }

  private String rendResponseWithSpEl(Map<String, Object> context, boolean postRequest) {
    Object o = context.get(SpelEngineActionRunner.INPUT_CODE);

    SpELEngine.parseToList(o, context, new ArrayList<>());
    String retJsonString =
        SpELEngine.evaluate(o, context, postRequest && !action.isPostResultRender());
    Object targetObj = context.get(TARGET_VALUE_MAPPER_KEY);
    StateValueMappingDto responseTargetMapperDto = new StateValueMappingDto();
    if (Objects.nonNull(targetObj)) {
      responseTargetMapperDto =
          JsonToolkit.fromJson(JsonToolkit.toJson(targetObj), StateValueMappingDto.class);
    }
    retJsonString = renderStatus(responseTargetMapperDto, retJsonString);
    if (StringUtils.isBlank(retJsonString)) {
      return StringUtils.EMPTY;
    }
    retJsonString =
        SpELEngine.evaluate(
            JsonToolkit.fromJson(retJsonString, Object.class), context, postRequest);

    context.put("mefResponseBody", retJsonString);
    // calculate final state
    if (action.isPostResultRender()) {
      retJsonString = calculateBasedOnResponseBody(retJsonString, context);
    }
    // clear empty attribute
    return deleteNodeByPath(responseTargetMapperDto.getTargetCheckPathMapper(), retJsonString);
  }
}
