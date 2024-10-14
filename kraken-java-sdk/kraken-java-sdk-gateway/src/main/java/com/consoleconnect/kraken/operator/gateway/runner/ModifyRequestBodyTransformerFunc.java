package com.consoleconnect.kraken.operator.gateway.runner;

import com.consoleconnect.kraken.operator.core.model.AppProperty;
import com.consoleconnect.kraken.operator.core.model.facet.ComponentAPIFacets;
import com.consoleconnect.kraken.operator.gateway.repo.HttpRequestRepository;
import com.consoleconnect.kraken.operator.gateway.service.FilterHeaderService;
import com.consoleconnect.kraken.operator.gateway.template.JavaScriptEngine;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.cloud.gateway.filter.factory.rewrite.RewriteFunction;
import org.springframework.web.server.ServerWebExchange;

@Slf4j
public class ModifyRequestBodyTransformerFunc extends AbstractBodyTransformerFunc
    implements RewriteFunction<String, String> {

  public ModifyRequestBodyTransformerFunc(
      ComponentAPIFacets.Action action,
      JavaScriptEngine javaScriptEngine,
      HttpRequestRepository httpRequestRepository,
      AppProperty appProperty,
      FilterHeaderService filterHeaderService) {
    super(action, javaScriptEngine, httpRequestRepository, appProperty, filterHeaderService);
  }

  @Override
  public Publisher<String> apply(ServerWebExchange exchange, String s) {
    return transform(exchange, s, false);
  }
}
