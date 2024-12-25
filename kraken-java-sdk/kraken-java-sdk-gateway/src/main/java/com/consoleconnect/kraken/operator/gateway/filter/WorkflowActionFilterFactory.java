package com.consoleconnect.kraken.operator.gateway.filter;

import com.consoleconnect.kraken.operator.core.exception.KrakenException;
import com.consoleconnect.kraken.operator.core.model.AppProperty;
import com.consoleconnect.kraken.operator.core.model.facet.ComponentAPIFacets;
import com.consoleconnect.kraken.operator.core.toolkit.JsonToolkit;
import com.consoleconnect.kraken.operator.gateway.runner.AbstractActionRunner;
import com.netflix.conductor.client.http.MetadataClient;
import com.netflix.conductor.common.metadata.workflow.StartWorkflowRequest;
import com.netflix.conductor.common.metadata.workflow.WorkflowDef;
import com.netflix.conductor.common.run.Workflow;
import io.orkes.conductor.client.http.OrkesWorkflowClient;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class WorkflowActionFilterFactory
    extends AbstractGatewayFilterFactory<WorkflowActionFilterFactory.Config> {
  public static final String TARGET_CONFIG_MAP = "targetApiConfigMap";
  public static final String URI = "uri";

  public WorkflowActionFilterFactory() {
    super(Config.class);
  }

  @Override
  public GatewayFilter apply(Config config) {
    ComponentAPIFacets.Action action = config.getAction();
    return (exchange, chain) -> {
      Optional<Map<String, Object>> contextOptional =
          AbstractActionRunner.generateActionContext(
              exchange, action, config.getAppProperty().getEnv());
      Map<String, Object> inputs = contextOptional.get();
      Map<String, Object> targetApiConfigMap = (Map<String, Object>) inputs.get(TARGET_CONFIG_MAP);

      StartWorkflowRequest request =
          constructWorkflowPayload(targetApiConfigMap, config, inputs, exchange, action);

      try {
        // Initialise WorkflowExecutor and Conductor Workers
        String workflowId = config.getWorkflowClient().startWorkflow(request);
        //        Workflow workflow = pollingResult(workflowId, config);
        //        if (workflow.getStatus() == Workflow.WorkflowStatus.RUNNING) {
        //          throw KrakenException.internalError("workflow timeout!");
        //        }
        //        Map<String, Object> result = workflow.getOutput();
        //        WorkflowResponse response =
        //            JsonToolkit.fromJson(JsonToolkit.toJson(Collections.emptyMap()),
        // WorkflowResponse.class);
        //        Map<String, Object> responseMap =
        //            response.getResult().values().stream()
        //                .collect(
        //                    Collectors.toMap(
        //                        WorkflowResponse.ItemResponse::getId,
        //                        WorkflowResponse.ItemResponse::getResponse));
        return wrapperResponse(exchange, Map.of(), config);
      } catch (Exception e) {
        throw KrakenException.internalError("workflow execute error: " + e);
      }
    };
  }

  private Workflow pollingResult(String workflowId, Config config) {
    Workflow workflow = new Workflow();
    for (int i = 0; i < 30; i++) {
      try {
        Thread.sleep(2000l);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
      workflow = config.getWorkflowClient().getWorkflow(workflowId, false);
      log.info("workflow: {} status: {}", workflow.getWorkflowName(), workflow.getStatus());
      if (workflow.getStatus().isSuccessful()) {
        break;
      } else if (workflow.getStatus() != Workflow.WorkflowStatus.RUNNING) {
        throw KrakenException.internalError(
            "workflow error: " + workflow.getReasonForIncompletion());
      }
    }
    return workflow;
  }

  private StartWorkflowRequest constructWorkflowPayload(
      Map<String, Object> targetApiConfigMap,
      Config config,
      Map<String, Object> inputs,
      ServerWebExchange exchange,
      ComponentAPIFacets.Action action) {
    Map<String, Object> workflowPayload = new HashMap<>();
    String payload =
        "{\"getStatusUrl\":\"https://79822d20-557f-4620-b5c3-b83d8457d8e0.mock.pstmn.io/api/v2/company/consolecore-poping-company/connections/66397d963cfa4f56f9135d41\",\"disableOrderUrl\":\"https://79822d20-557f-4620-b5c3-b83d8457d8e0.mock.pstmn.io/api/company/consolecore-poping-company/connections/66397d963cfa4f56f9135d41/disable\",\"pollingStatusUrl\":\"https://79822d20-557f-4620-b5c3-b83d8457d8e0.mock.pstmn.io/api/v2/company/consolecore-poping-company/connections/66397d963cfa4f56f9135d41\",\"token\":\"Bearer eyJvcmciOiI2MjZjMzMxMDM2YmE4NDAwMDFjMzBhYWIiLCJpZCI6IjMxNDM1ZmIxZmY0MjQxYjRiOTc3MTQzYmJlMTAzYzAyIiwiaCI6Im11cm11cjEyOCJ9\",\"disableOrderPayload\":{},\"deleteConnectionUrl\":\"https://79822d20-557f-4620-b5c3-b83d8457d8e0.mock.pstmn.io/api/v2/company/consolecore-poping-company/connections/66397d963cfa4f56f9135d41\"}";
    StartWorkflowRequest request = new StartWorkflowRequest();
    Map map = JsonToolkit.fromJson(payload, Map.class);
    map.put("orderId", inputs.get("entity-id"));
    request.setInput(map);
    request.setName("delete_eline_order");
    request.setVersion(getLatestVersion(config.getMetadataClient(), request.getName()));
    return request;
  }

  private Mono<Void> wrapperResponse(
      ServerWebExchange exchange, Map<String, Object> responseMap, Config config) {
    ServerHttpResponse httpResponse = exchange.getResponse();
    httpResponse.setStatusCode(HttpStatusCode.valueOf(200));
    Map<String, String> headers = exchange.getRequest().getHeaders().toSingleValueMap();
    headers.forEach((key, value) -> httpResponse.getHeaders().set(key, value));
    byte[] bytes = JsonToolkit.toJson(responseMap).getBytes(StandardCharsets.UTF_8);
    DataBuffer buffer = httpResponse.bufferFactory().wrap(bytes);
    exchange.getAttributes().put(config.getAction().getOutputKey(), responseMap);
    return httpResponse.writeWith(Mono.just(buffer));
  }

  private int getLatestVersion(MetadataClient metadataClient, String processName) {
    WorkflowDef workflowDef = metadataClient.getWorkflowDef(processName, null);
    if (workflowDef == null) {
      throw KrakenException.notFound("not found workflow process: " + processName);
    }
    return workflowDef.getVersion();
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
