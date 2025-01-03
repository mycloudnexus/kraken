package com.consoleconnect.kraken.operator.gateway.filter;

import com.consoleconnect.kraken.operator.core.model.AppProperty;
import com.consoleconnect.kraken.operator.core.model.facet.ComponentAPIFacets;
import com.netflix.conductor.client.http.MetadataClient;
import io.orkes.conductor.client.http.OrkesWorkflowClient;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class WorkflowActionFilterFactory
    extends AbstractGatewayFilterFactory<WorkflowActionFilterFactory.Config> {

  public WorkflowActionFilterFactory() {
    super(Config.class);
  }

  @Override
  public GatewayFilter apply(Config config) {
    return (exchange, chain) -> chain.filter(exchange);
  }

  @Data
  public static class Config {
    private ComponentAPIFacets.Action action;
    private OrkesWorkflowClient workflowClient;
    private MetadataClient metadataClient;
    private String baseUri;
    private AppProperty appProperty;
  }
}
