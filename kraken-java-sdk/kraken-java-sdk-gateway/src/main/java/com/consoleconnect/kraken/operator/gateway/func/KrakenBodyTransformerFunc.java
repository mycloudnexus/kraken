package com.consoleconnect.kraken.operator.gateway.func;

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.CACHED_REQUEST_BODY_ATTR;

import com.consoleconnect.kraken.operator.core.exception.KrakenException;
import com.consoleconnect.kraken.operator.core.model.facet.ComponentAPIFacets;
import com.consoleconnect.kraken.operator.core.toolkit.JsonToolkit;
import com.consoleconnect.kraken.operator.gateway.filter.GeneratePathFromBodyGatewayFilterFactory;
import com.consoleconnect.kraken.operator.gateway.model.HttpContext;
import com.consoleconnect.kraken.operator.gateway.model.HttpRequestContext;
import com.consoleconnect.kraken.operator.gateway.model.HttpResponseContext;
import com.consoleconnect.kraken.operator.gateway.template.JavaScriptEngine;
import java.util.Objects;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.reactivestreams.Publisher;
import org.springframework.cloud.gateway.filter.factory.rewrite.RewriteFunction;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@AllArgsConstructor
public class KrakenBodyTransformerFunc implements RewriteFunction<String, String> {

  private final ComponentAPIFacets.Action action;
  private final JavaScriptEngine javaScriptEngine;
  private final boolean isResponse;

  @Override
  public Publisher<String> apply(ServerWebExchange exchange, String s) {
    log.info(
        "X_FILTER_ID:{}",
        exchange.getAttributes().get(GeneratePathFromBodyGatewayFilterFactory.X_FILTER_ID));

    Optional<String> transformerKey = getTransformerKey(exchange);
    if (transformerKey.isEmpty()) {
      log.info("transformer key is empty");
      return StringUtils.isBlank(s) ? Mono.empty() : Mono.just(s);
    }

    if (StringUtils.isBlank(s)) {
      log.info("body is empty");
      return Mono.empty();
    }

    HttpContext httpContext = new HttpContext();
    HttpRequestContext httpRequestContext = new HttpRequestContext();
    httpRequestContext.setUri(exchange.getRequest().getURI().toString());
    httpRequestContext.setMethod(exchange.getRequest().getMethod().toString());
    httpRequestContext.setPath(exchange.getRequest().getPath().toString());

    if (!isResponse) {
      httpRequestContext.setBody(JsonToolkit.fromJson(s, Object.class));
    } else {
      // Read cached request body
      String requestBody = exchange.getAttribute(CACHED_REQUEST_BODY_ATTR);
      log.info("cached request body: {}", requestBody);
      httpRequestContext.setBody(JsonToolkit.fromJson(requestBody, Object.class));
    }
    httpContext.setRequest(httpRequestContext);

    HttpResponseContext httpResponseContext = new HttpResponseContext();
    if (isResponse) {
      if (HttpStatusCode.valueOf(404)
          .isSameCodeAs(Objects.requireNonNull(exchange.getResponse().getStatusCode()))) {
        throw KrakenException.notFoundDefault();
      }
      httpResponseContext.setBody(JsonToolkit.fromJson(s, Object.class));
      httpResponseContext.setStatus(
          Objects.requireNonNull(exchange.getResponse().getStatusCode()).value());
      httpContext.setResponse(httpResponseContext);
    }

    String response = javaScriptEngine.execute(transformerKey.get(), httpContext);
    return Mono.just(response);
  }

  private Optional<String> getTransformerKey(ServerWebExchange exchange) {
    if (isResponse) {
      if (action.getResponse() != null
          && action.getResponse().getBody() != null
          && action.getResponse().getBody().getTransformerKey() != null) {
        return Optional.of(action.getResponse().getBody().getTransformerKey());
      }
    } else {
      if (action.getRequestBody() != null && action.getRequestBody().getTransformerKey() != null) {
        return Optional.of(action.getRequestBody().getTransformerKey());
      }
    }

    String filterId = exchange.getAttribute(GeneratePathFromBodyGatewayFilterFactory.X_FILTER_ID);
    if (filterId != null && action.getFilters() != null && !action.getFilters().isEmpty()) {
      Optional<ComponentAPIFacets.Filter> filter =
          action.getFilters().stream().filter(x -> x.getId().equals(filterId)).findFirst();

      if (isResponse) {
        return filter
            .map(ComponentAPIFacets.Filter::getResponse)
            .map(ComponentAPIFacets.HttpResponse::getBody)
            .map(ComponentAPIFacets.Body::getTransformerKey);
      } else {
        return filter
            .map(ComponentAPIFacets.Filter::getRequestBody)
            .map(ComponentAPIFacets.Body::getTransformerKey);
      }
    }

    return Optional.empty();
  }
}
