package com.consoleconnect.kraken.operator.gateway.runner;

import com.consoleconnect.kraken.operator.core.enums.ActionTypeEnum;
import com.consoleconnect.kraken.operator.core.model.AppProperty;
import com.consoleconnect.kraken.operator.core.model.facet.ComponentAPIFacets;
import java.net.URI;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerWebExchange;

@Service
@Slf4j
public class RewritePathActionRunner extends AbstractActionRunner {
  public static final String INPUT_PATH = "path";
  public static final String INPUT_METHOD = "method";
  public static final String INPUT_URL = "url";
  public static final String WORKFLOW_ENABLED = "workflowEnabled";

  public RewritePathActionRunner(AppProperty appProperty) {
    super(appProperty);
  }

  @Override
  public boolean canHandle(ComponentAPIFacets.Action action) {
    return ActionTypeEnum.REWRITE_PATH == ActionTypeEnum.fromString(action.getActionType());
  }

  @Override
  protected Optional<ServerWebExchange> runIt(
      ServerWebExchange exchange,
      ComponentAPIFacets.Action action,
      Map<String, Object> inputs,
      Map<String, Object> outputs) {
    String path = (String) inputs.get(INPUT_PATH);
    String method = (String) inputs.get(INPUT_METHOD);
    String url = (String) inputs.get(INPUT_URL);
    boolean workflowEnabled = (boolean) inputs.getOrDefault(WORKFLOW_ENABLED, false);
    if (workflowEnabled) {
      return Optional.empty();
    }
    exchange
        .getAttributes()
        .put(ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR, URI.create(url + path));

    ServerHttpRequest request =
        exchange
            .getRequest()
            .mutate()
            .uri(buildUri(exchange, path))
            .method(HttpMethod.valueOf(method.toUpperCase()))
            .build();
    return Optional.of(exchange.mutate().request(request).build());
  }

  private URI buildUri(ServerWebExchange exchange, String targetPath) {
    URI targetUri = URI.create(targetPath);
    String queryStr =
        Stream.of(exchange.getRequest().getURI().getQuery(), targetUri.getQuery())
            .filter(Objects::nonNull)
            .collect(Collectors.joining("&"));
    String url =
        LoadTargetAPIConfigActionRunner.encodeUrlParam(
            Stream.of(targetUri.getPath(), queryStr).collect(Collectors.joining("?")));
    return URI.create(url);
  }
}
