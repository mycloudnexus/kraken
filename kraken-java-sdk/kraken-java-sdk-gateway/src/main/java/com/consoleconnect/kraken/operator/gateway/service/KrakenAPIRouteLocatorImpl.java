package com.consoleconnect.kraken.operator.gateway.service;

import com.consoleconnect.kraken.operator.core.dto.UnifiedAssetDto;
import com.consoleconnect.kraken.operator.core.enums.AssetKindEnum;
import com.consoleconnect.kraken.operator.core.enums.ParentProductTypeEnum;
import com.consoleconnect.kraken.operator.core.model.AppProperty;
import com.consoleconnect.kraken.operator.core.model.facet.ComponentAPIFacets;
import com.consoleconnect.kraken.operator.core.service.UnifiedAssetService;
import com.consoleconnect.kraken.operator.core.toolkit.JsonToolkit;
import com.consoleconnect.kraken.operator.gateway.func.KrakenGatewayFilterSpecFunc;
import com.consoleconnect.kraken.operator.gateway.repo.HttpRequestRepository;
import com.consoleconnect.kraken.operator.gateway.runner.AbstractActionRunner;
import com.consoleconnect.kraken.operator.gateway.template.JavaScriptEngine;
import io.orkes.conductor.client.http.OrkesMetadataClient;
import io.orkes.conductor.client.http.OrkesWorkflowClient;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.BooleanSpec;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@AllArgsConstructor
@Service
@Slf4j
public class KrakenAPIRouteLocatorImpl implements RouteLocator {
  private final RouteLocatorBuilder routeLocatorBuilder;
  private final UnifiedAssetService service;
  private final List<AbstractActionRunner> actionRunners;
  private final JavaScriptEngine javaScriptEngine;
  private final HttpRequestRepository httpRequestRepository;
  private final AppProperty appProperty;
  private final FilterHeaderService filterHeaderService;
  private final OrkesWorkflowClient workflowClient;
  private final OrkesMetadataClient metadataClient;

  private List<UnifiedAssetDto> listProducts() {
    return service
        .search(
            null,
            AssetKindEnum.PRODUCT.getKind(),
            true,
            null,
            ParentProductTypeEnum.ACCESS_ELINE.getKind(),
            PageRequest.of(0, Integer.MAX_VALUE))
        .getData();
  }

  private List<UnifiedAssetDto> listComponents(String productId) {
    return service
        .search(
            productId,
            AssetKindEnum.COMPONENT_API.getKind(),
            true,
            null,
            ParentProductTypeEnum.ACCESS_ELINE.getKind(),
            PageRequest.of(0, Integer.MAX_VALUE))
        .getData();
  }

  private void loadComponentMapping(
      List<ComponentAPIFacets.RouteMapping> mappings, RouteLocatorBuilder.Builder builder) {
    for (ComponentAPIFacets.RouteMapping mapping : mappings) {
      ComponentAPIFacets.Trigger trigger = mapping.getTrigger();
      builder.route(
          r -> {
            BooleanSpec booleanSpec = r.path(filterSlash(trigger.getPath()));
            if (trigger.getMethod() != null) {
              log.info("method: {}", trigger.getMethod());
              booleanSpec = booleanSpec.and().method(trigger.getMethod().toUpperCase());
            }
            return booleanSpec
                .filters(
                    f ->
                        new KrakenGatewayFilterSpecFunc(
                                mapping,
                                actionRunners,
                                javaScriptEngine,
                                httpRequestRepository,
                                appProperty,
                                filterHeaderService,
                                workflowClient,
                                metadataClient)
                            .apply(f))
                .uri("no://op");
          });
    }
  }

  @Override
  public Flux<Route> getRoutes() {
    log.info("loading routes");
    RouteLocatorBuilder.Builder builder = routeLocatorBuilder.routes();
    for (UnifiedAssetDto product : listProducts()) {
      for (UnifiedAssetDto component : listComponents(product.getId())) {
        ComponentAPIFacets componentAPIFacets =
            JsonToolkit.fromJson(
                JsonToolkit.toJson(component.getFacets()), ComponentAPIFacets.class);
        if (CollectionUtils.isEmpty(componentAPIFacets.getMappings())) {
          log.info("loading routes has no configurable items");
          continue;
        }
        loadComponentMapping(componentAPIFacets.getMappings(), builder);
      }
    }

    return builder.build().getRoutes();
  }

  private String[] filterSlash(String path) {
    return path.startsWith("/") ? new String[] {path.substring(1), path} : new String[] {path};
  }
}
