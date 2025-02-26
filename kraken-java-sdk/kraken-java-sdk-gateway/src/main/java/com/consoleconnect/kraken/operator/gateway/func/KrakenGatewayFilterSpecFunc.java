package com.consoleconnect.kraken.operator.gateway.func;

import static com.consoleconnect.kraken.operator.core.enums.ActionTypeEnum.REWRITE_PATH;
import static org.springframework.cloud.gateway.support.RouteMetadataUtils.CONNECT_TIMEOUT_ATTR;
import static org.springframework.cloud.gateway.support.RouteMetadataUtils.RESPONSE_TIMEOUT_ATTR;

import com.consoleconnect.kraken.operator.core.enums.ActionTypeEnum;
import com.consoleconnect.kraken.operator.core.model.AppProperty;
import com.consoleconnect.kraken.operator.core.model.facet.ComponentAPIFacets;
import com.consoleconnect.kraken.operator.core.repo.WorkflowInstanceRepository;
import com.consoleconnect.kraken.operator.gateway.filter.*;
import com.consoleconnect.kraken.operator.gateway.repo.HttpRequestRepository;
import com.consoleconnect.kraken.operator.gateway.runner.*;
import com.consoleconnect.kraken.operator.gateway.service.FilterHeaderService;
import com.consoleconnect.kraken.operator.gateway.template.JavaScriptEngine;
import io.orkes.conductor.client.http.OrkesMetadataClient;
import io.orkes.conductor.client.http.OrkesWorkflowClient;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.RouteToRequestUrlFilter;
import org.springframework.cloud.gateway.filter.factory.CacheRequestBodyGatewayFilterFactory;
import org.springframework.cloud.gateway.route.builder.GatewayFilterSpec;
import org.springframework.cloud.gateway.route.builder.UriSpec;
import org.springframework.core.Ordered;
import org.springframework.http.MediaType;

@AllArgsConstructor
@Slf4j
public class KrakenGatewayFilterSpecFunc implements Function<GatewayFilterSpec, UriSpec> {

  private final ComponentAPIFacets.RouteMapping mapping;
  private final List<AbstractActionRunner> actionRunners;
  private final JavaScriptEngine javaScriptEngine;
  private final HttpRequestRepository httpRequestRepository;
  private final AppProperty appProperty;
  private final FilterHeaderService filterHeaderService;
  private final OrkesWorkflowClient workflowClient;
  private final OrkesMetadataClient metaDataClient;
  private final WorkflowInstanceRepository workflowInstanceRepository;

  @Override
  public UriSpec apply(GatewayFilterSpec gatewayFilterSpec) {
    if (mapping.getActions() == null) {
      log.warn("no actions found,mapping:{}", mapping);
      return gatewayFilterSpec;
    }

    log.info("mapping:{}", mapping);
    // add traceId
    gatewayFilterSpec.filter(
        (exchange, chain) -> {
          exchange
              .getAttributes()
              .put(KrakenFilterConstants.X_REQUEST_ID, UUID.randomUUID().toString());
          return chain.filter(exchange);
        },
        Ordered.HIGHEST_PRECEDENCE + 10);

    // config metadata
    if (mapping.getMetadata() != null) {
      configureMetadata(gatewayFilterSpec, mapping.getMetadata());
    }

    for (ComponentAPIFacets.Action action : mapping.getActions()) {
      log.info("filter mode, actionType:{}", action.getActionType());
      switch (ActionTypeEnum.fromString(action.getActionType())) {
        case CACHE_REQUEST_BODY -> {
          CacheRequestBodyGatewayFilterFactory.Config cacheConfig =
              new CacheRequestBodyGatewayFilterFactory.Config();
          cacheConfig.setBodyClass(String.class);
          gatewayFilterSpec.filters(new CacheRequestBodyGatewayFilterFactory().apply(cacheConfig));
        }
        case MODIFY_REQUEST_BODY -> gatewayFilterSpec.modifyRequestBody(
            String.class,
            String.class,
            MediaType.APPLICATION_JSON_VALUE,
            (exchange, s) ->
                new ModifyRequestBodyTransformerFunc(
                        action,
                        javaScriptEngine,
                        httpRequestRepository,
                        appProperty,
                        filterHeaderService)
                    .apply(exchange, s));
        case MODIFY_RESPONSE_BODY -> gatewayFilterSpec.modifyResponseBody(
            String.class,
            String.class,
            MediaType.APPLICATION_JSON_VALUE,
            (exchange, s) ->
                new ModifyResponseBodyTransformerFunc(
                        action,
                        javaScriptEngine,
                        httpRequestRepository,
                        appProperty,
                        filterHeaderService)
                    .apply(exchange, s));
        case MOCK_RESPONSE -> gatewayFilterSpec.filter(
            new MockResponseGatewayFilterFactory().apply(action));
        case REWRITE_PATH -> gatewayFilterSpec.filter(
            new ActionGatewayFilterFactory(actionRunners).apply(action),
            RouteToRequestUrlFilter.ROUTE_TO_URL_FILTER_ORDER + 1);
        case WORKFLOW -> {
          WorkflowActionFilterFactory.Config config = new WorkflowActionFilterFactory.Config();
          config.setAction(action);
          config.setWorkflowClient(workflowClient);
          config.setMetadataClient(metaDataClient);
          config.setBaseUri(mapping.getUri());
          config.setAppProperty(appProperty);
          gatewayFilterSpec.filter(
              new WorkflowActionFilterFactory(workflowInstanceRepository).apply(config),
              RouteToRequestUrlFilter.ROUTE_TO_URL_FILTER_ORDER + 1);
        }
        default -> gatewayFilterSpec.filter(
            new ActionGatewayFilterFactory(actionRunners).apply(action), action.getOrder());
      }
    }
    return gatewayFilterSpec;
  }

  private void configureMetadata(
      GatewayFilterSpec gatewayFilterSpec, ComponentAPIFacets.Metadata metadata) {
    log.info("config metadata:{}", metadata);
    if (metadata.isCacheRequestBody()) {
      log.info("enable cacheRequestBody");
      CacheRequestBodyGatewayFilterFactory.Config cacheConfig =
          new CacheRequestBodyGatewayFilterFactory.Config();
      cacheConfig.setBodyClass(String.class);
      gatewayFilterSpec.filters(new CacheRequestBodyGatewayFilterFactory().apply(cacheConfig));
    }
    if (metadata.getRequestHeaders() != null) {
      if (metadata.getRequestHeaders().getAdd() != null) {
        log.info("add request headers:{}", metadata.getRequestHeaders().getAdd());
        metadata.getRequestHeaders().getAdd().forEach(gatewayFilterSpec::addRequestHeader);
      }
      if (metadata.getRequestHeaders().getDelete() != null) {
        log.info("delete request headers:{}", metadata.getRequestHeaders().getDelete());
        metadata.getRequestHeaders().getDelete().forEach(gatewayFilterSpec::removeRequestHeader);
      }
    }
    if (metadata.getResponseHeaders() != null) {
      if (metadata.getResponseHeaders().getAdd() != null) {
        log.info("add response headers:{}", metadata.getResponseHeaders().getAdd());
        metadata.getResponseHeaders().getAdd().forEach(gatewayFilterSpec::addResponseHeader);
      }
      if (metadata.getResponseHeaders().getDelete() != null) {
        log.info("delete response headers:{}", metadata.getResponseHeaders().getDelete());
        metadata.getResponseHeaders().getDelete().forEach(gatewayFilterSpec::removeResponseHeader);
      }
    }

    if (metadata.getResponseTimeout() != null) {
      log.info("set response timeout:{}", metadata.getResponseTimeout());
      gatewayFilterSpec.metadata(RESPONSE_TIMEOUT_ATTR, metadata.getResponseTimeout());
    }

    if (metadata.getConnectTimeout() != null) {
      log.info("set connect timeout:{}", metadata.getConnectTimeout());
      gatewayFilterSpec.metadata(CONNECT_TIMEOUT_ATTR, metadata.getConnectTimeout());
    }
  }
}
