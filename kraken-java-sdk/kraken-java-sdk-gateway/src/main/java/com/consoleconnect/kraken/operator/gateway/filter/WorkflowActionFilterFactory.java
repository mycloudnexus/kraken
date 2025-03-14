package com.consoleconnect.kraken.operator.gateway.filter;

import com.consoleconnect.kraken.operator.core.entity.WorkflowInstanceEntity;
import com.consoleconnect.kraken.operator.core.enums.WorkflowStatusEnum;
import com.consoleconnect.kraken.operator.core.exception.KrakenException;
import com.consoleconnect.kraken.operator.core.model.AppProperty;
import com.consoleconnect.kraken.operator.core.model.HttpTask;
import com.consoleconnect.kraken.operator.core.model.facet.ComponentAPIFacets;
import com.consoleconnect.kraken.operator.core.model.facet.ComponentWorkflowFacets;
import com.consoleconnect.kraken.operator.core.repo.WorkflowInstanceRepository;
import com.consoleconnect.kraken.operator.core.toolkit.JsonToolkit;
import com.consoleconnect.kraken.operator.gateway.model.WorkflowPayload;
import com.consoleconnect.kraken.operator.gateway.runner.AbstractActionRunner;
import com.netflix.conductor.client.http.MetadataClient;
import com.netflix.conductor.common.metadata.tasks.Task;
import com.netflix.conductor.common.metadata.workflow.StartWorkflowRequest;
import com.netflix.conductor.common.metadata.workflow.WorkflowDef;
import com.netflix.conductor.common.run.Workflow;
import io.orkes.conductor.client.http.OrkesWorkflowClient;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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

  private final WorkflowInstanceRepository workflowInstanceRepository;

  public static final String VAR_SYNCHRONOUS = "synchronous";
  public static final String VAR_WORKFLOW_ENABLED = "enabled";
  public static final String VAR_WORKFLOW_CONFIG = "workflow-config";
  public static final String VAR_ENTITY_ID = "entity-id";
  public static final String VAR_REQUEST_ID = "log-request-id";
  public static final String VAR_BASE_URL = "url";
  public static final String HOST = "Host";
  public static final String CONTENT_LENGTH = "Content-Length";

  public WorkflowActionFilterFactory(WorkflowInstanceRepository workflowInstanceRepository) {
    super(Config.class);
    this.workflowInstanceRepository = workflowInstanceRepository;
  }

  @Override
  public GatewayFilter apply(Config config) {
    ComponentAPIFacets.Action action = config.getAction();
    return (exchange, chain) -> {
      Optional<Map<String, Object>> contextOptional =
          AbstractActionRunner.generateActionContext(
              exchange, action, config.getAppProperty().getEnv());

      Map<String, Object> inputs = contextOptional.get();
      boolean synchronousProcess = getBool(inputs, VAR_SYNCHRONOUS);
      boolean workflowEnabled = getBool(inputs, VAR_WORKFLOW_ENABLED);
      if (!workflowEnabled) {
        return chain.filter(exchange);
      }
      StartWorkflowRequest request = constructWorkflowPayload(config, inputs, exchange);
      try {
        // start workflow
        log.info("start workflow: {}", JsonToolkit.toJson(request));
        String workflowId = config.getWorkflowClient().startWorkflow(request);
        // record workflow instance
        recordWorkflowInstance(request, inputs, workflowId);
        // if async process, then return empty response
        if (!synchronousProcess) {
          return wrapperResponse(exchange, Map.of(), config);
        }
        // if sync process, then waiting for end of workflow
        Workflow workflow = pollingResult(workflowId, config);
        if (workflow.getStatus() == Workflow.WorkflowStatus.RUNNING) {
          throw KrakenException.internalError("workflow timeout!");
        }
        return wrapperResponse(exchange, constructResponse(workflow), config);
      } catch (Exception e) {
        throw KrakenException.internalError("workflow execute error: " + e);
      }
    };
  }

  private void recordWorkflowInstance(
      StartWorkflowRequest request, Map<String, Object> inputs, String workflowId) {
    WorkflowInstanceEntity entity = new WorkflowInstanceEntity();
    entity.setStatus(WorkflowStatusEnum.IN_PROGRESS.name());
    entity.setPayload(request);
    entity.setRequestId((String) inputs.get(VAR_REQUEST_ID));
    entity.setWorkflowInstanceId(workflowId);
    entity.setSynced(false);
    workflowInstanceRepository.save(entity);
  }

  public static boolean getBool(Map<String, Object> inputs, String param) {
    if (inputs.containsKey(param) && inputs.get(param) instanceof Boolean boolVar) {
      return boolVar;
    }
    return Boolean.FALSE;
  }

  private static Map<String, Object> constructResponse(Workflow workflow) {
    return workflow.getTasks().stream()
        .collect(Collectors.toMap(Task::getReferenceTaskName, Task::getOutputData));
  }

  private Workflow pollingResult(String workflowId, Config config) {
    Workflow workflow = new Workflow();
    AppProperty.WorkflowConfig pollConfig = config.getAppProperty().getWorkflow();
    for (int i = 0; i < pollConfig.getPollTimes(); i++) {
      try {
        Thread.sleep(pollConfig.getPollInterval());
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
      workflow = config.getWorkflowClient().getWorkflow(workflowId, true);
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
      Config config, Map<String, Object> inputs, ServerWebExchange exchange) {
    ComponentWorkflowFacets workflowFacets =
        JsonToolkit.fromJson(
            JsonToolkit.toJson(inputs.get(VAR_WORKFLOW_CONFIG)), ComponentWorkflowFacets.class);
    WorkflowPayload payload = new WorkflowPayload();
    payload.setId((String) inputs.get(VAR_ENTITY_ID));
    payload.setRequestId((String) inputs.get(VAR_REQUEST_ID));
    payload.setHeaders(cleanHeaders(exchange));
    workflowFacets.getValidationStage().stream()
        .forEach(task -> constructHttpPayload(task, payload, inputs));
    workflowFacets.getPreparationStage().stream()
        .forEach(task -> constructHttpPayload(task, payload, inputs));
    workflowFacets.getExecutionStage().stream()
        .forEach(task -> constructHttpPayload(task, payload, inputs));

    StartWorkflowRequest request = new StartWorkflowRequest();
    Map<String, Object> map = JsonToolkit.fromJson(payload, Map.class);
    request.setInput(map);
    request.setName(workflowFacets.getMetaData().getWorkflowName());
    request.setVersion(getLatestVersion(config.getMetadataClient(), request.getName()));
    return request;
  }

  private static Map<String, String> cleanHeaders(ServerWebExchange exchange) {
    Map<String, String> singleValueMap = exchange.getRequest().getHeaders().toSingleValueMap();
    singleValueMap.remove(HOST);
    singleValueMap.remove(CONTENT_LENGTH);
    return singleValueMap;
  }

  private static void constructHttpPayload(
      HttpTask task, WorkflowPayload payload, Map<String, Object> inputs) {
    String url = (String) inputs.get(VAR_BASE_URL);
    Map<String, WorkflowPayload.HttpPayload> httpPayloadMap = payload.getPayload();
    WorkflowPayload.HttpPayload httpPayload = new WorkflowPayload.HttpPayload();
    httpPayload.setBody(
        StringUtils.isBlank(task.getEndpoint().getRequestBody())
            ? null
            : JsonToolkit.fromJson(task.getEndpoint().getRequestBody(), Map.class));
    httpPayload.setUrl(url + task.getEndpoint().getPath());
    httpPayload.setMethod(task.getEndpoint().getMethod());
    httpPayloadMap.put(task.getTaskName(), httpPayload);
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
