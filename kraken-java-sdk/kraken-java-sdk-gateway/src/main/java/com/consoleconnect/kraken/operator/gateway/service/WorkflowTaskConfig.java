package com.consoleconnect.kraken.operator.gateway.service;

import static com.consoleconnect.kraken.operator.core.toolkit.Constants.*;

import com.consoleconnect.kraken.operator.core.entity.ApiActivityLogEntity;
import com.consoleconnect.kraken.operator.core.entity.WorkflowInstanceEntity;
import com.consoleconnect.kraken.operator.core.enums.WorkflowStatusEnum;
import com.consoleconnect.kraken.operator.core.exception.KrakenException;
import com.consoleconnect.kraken.operator.core.model.ApiActivityRequestLog;
import com.consoleconnect.kraken.operator.core.model.ApiActivityResponseLog;
import com.consoleconnect.kraken.operator.core.repo.ApiActivityLogRepository;
import com.consoleconnect.kraken.operator.core.repo.WorkflowInstanceRepository;
import com.consoleconnect.kraken.operator.core.toolkit.ConstructExpressionUtil;
import com.consoleconnect.kraken.operator.core.toolkit.JsonToolkit;
import com.consoleconnect.kraken.operator.gateway.entity.HttpRequestEntity;
import com.consoleconnect.kraken.operator.gateway.repo.HttpRequestRepository;
import com.consoleconnect.kraken.operator.gateway.template.SpELEngine;
import com.consoleconnect.kraken.operator.gateway.toolkit.ApiActivityLogHelper;
import com.consoleconnect.kraken.operator.workflow.model.EvaluateResult;
import com.consoleconnect.kraken.operator.workflow.model.LogTaskRequest;
import com.consoleconnect.kraken.operator.workflow.service.WorkflowTaskRegister;
import com.netflix.conductor.sdk.workflow.task.InputParam;
import com.netflix.conductor.sdk.workflow.task.WorkerTask;
import java.util.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Component
@AllArgsConstructor
@Getter
public class WorkflowTaskConfig implements WorkflowTaskRegister {
  private final HttpRequestRepository repository;

  private final BackendApiActivityLogService backendApiActivityLogService;
  private final WorkflowInstanceRepository workflowInstanceRepository;
  private final ApiActivityLogRepository apiActivityLogRepository;

  @WorkerTask(NOTIFY_TASK)
  public void notify(
      @InputParam("id") String id, @InputParam("notificationUrl") String notificationUrl) {
    log.info("Failed to delete order: {}", id);
    String msg = String.format("WARNING: Order= %s failed with unexpected error.", id);
    WebClient.create()
        .post()
        .uri(notificationUrl)
        .bodyValue(Map.of("text", msg))
        .retrieve()
        .onStatus(
            HttpStatusCode::isError,
            body -> {
              log.error("notification task failed");
              throw KrakenException.internalError("send alert error!");
            })
        .bodyToMono(String.class)
        .block();
  }

  @WorkerTask(FAIL_ORDER_TASK)
  public void failOrder(@InputParam("id") String id) {
    log.info("Set order to failed: {}", id);
    setOrderState(id, "failed");
  }

  @WorkerTask(value = EVALUATE_PAYLOAD_TASK, pollingInterval = 10)
  public EvaluateResult evaluateTask(
      @InputParam("value") Map<String, Object> value,
      @InputParam("urlExpression") String urlExpression,
      @InputParam("bodyExpression") Object bodyExpression) {
    String evaluate = SpELEngine.evaluate(bodyExpression, value);
    String url = SpELEngine.evaluate(urlExpression, value);
    EvaluateResult result = new EvaluateResult();
    result.setBody(
        StringUtils.isBlank(evaluate)
            ? Collections.emptyMap()
            : JsonToolkit.fromJson(evaluate, Map.class));
    result.setUrl(url);
    return result;
  }

  @WorkerTask(value = EVALUATE_EXPRESSION_TASK, pollingInterval = 10)
  public EvaluateResult evaluateExpressionTask(
      @InputParam("value") Map<String, Object> value, @InputParam("expression") String expression) {
    EvaluateResult result = new EvaluateResult();
    String evaluate =
        SpELEngine.evaluate(ConstructExpressionUtil.constructParam(expression), value);

    result.setSingleResult(StringUtils.isBlank(evaluate) ? Boolean.FALSE.toString() : evaluate);
    return result;
  }

  @WorkerTask(LOG_PAYLOAD_TASK)
  public void logRequestPayload(@InputParam("payload") LogTaskRequest payload) {
    try {
      ApiActivityRequestLog activityRequestLog = ApiActivityLogHelper.extractRequestLog(payload);
      if (activityRequestLog == null) {
        log.error("Invalid activity log, empty request");
        return;
      }
      ApiActivityLogEntity entity =
          backendApiActivityLogService.logApiActivityRequest(activityRequestLog);

      ApiActivityResponseLog activityResponseLog = ApiActivityLogHelper.extractResponseLog(payload);
      if (activityResponseLog == null) {
        log.error("Invalid activity log, empty response");
        return;
      }
      activityResponseLog.setApiActivityLog(entity);
      backendApiActivityLogService.logApiActivityResponse(activityResponseLog);
    } catch (Exception e) {
      log.error("Failed to log api activity log", e);
    }
  }

  @WorkerTask(EMPTY_TASK)
  public void doNothing() {
    log.info("empty task");
  }

  @WorkerTask(REJECT_ORDER_TASK)
  public void rejectOrder(@InputParam("id") String id) {
    log.info("Set order to rejected: {}", id);
    setOrderState(id, "rejected");
  }

  @WorkerTask(PROCESS_ORDER_TASK)
  public void processOrder(@InputParam("id") String id) {
    log.info("Set order to inProgress: {}", id);
    setOrderState(id, "inProgress");
  }

  @WorkerTask(PERSIST_RESPONSE_TASK)
  public void persistResponse(
      @InputParam("id") String id,
      @InputParam("payload") Object payload,
      @InputParam("uniqueIdPath") String uniqueIdPath) {
    log.info("persist response: {}", id);
    if (StringUtils.isNotBlank(id)) {
      repository
          .findById(UUID.fromString(id))
          .ifPresent(
              httpRequestEntity -> {
                httpRequestEntity.setResponse(payload);
                // set unique id to trace order
                setUniqueId(payload, uniqueIdPath, httpRequestEntity);
                repository.save(httpRequestEntity);
              });
    }
  }

  private static void setUniqueId(
      Object payload, String uniqueIdPath, HttpRequestEntity httpRequestEntity) {
    if (uniqueIdPath != null) {
      String expression = ConstructExpressionUtil.constructParam(uniqueIdPath);
      String uniqueId = SpELEngine.evaluate(expression, JsonToolkit.fromJson(payload, Map.class));
      log.info("set uniqueId: {}", uniqueId);
      if (StringUtils.isNotBlank(uniqueId)) {
        Object renderedResponse = httpRequestEntity.getRenderedResponse();
        Map<String, Object> map = JsonToolkit.fromJson(renderedResponse, Map.class);
        map.put("uniqueId", uniqueId);
        httpRequestEntity.setRenderedResponse(map);
      }
    }
  }

  @WorkerTask(WORKFLOW_SUCCESS_TASK)
  public void workflowSuccessTask(@InputParam("id") String id) {
    workflowStateChange(id, WorkflowStatusEnum.SUCCESS.name(), null);
  }

  @WorkerTask(WORKFLOW_FAILED_TASK)
  public void workflowFailedTask(
      @InputParam("id") String id, @InputParam("errorMsg") String errorMsg) {
    workflowStateChange(id, WorkflowStatusEnum.FAILED.name(), errorMsg);
  }

  private void workflowStateChange(
      @InputParam("id") String id,
      @InputParam("status") String status,
      @InputParam("errorMsg") String errorMsg) {
    log.info("workflow {} terminate with: status = {}, errorMsg = {}", id, status, errorMsg);
    if (StringUtils.isBlank(id)) {
      log.warn("id is null");
      return;
    }
    WorkflowInstanceEntity entity = workflowInstanceRepository.findByRequestId(id);
    if (entity != null) {
      entity.setStatus(status);
      entity.setErrorMsg(errorMsg);
      workflowInstanceRepository.save(entity);

      apiActivityLogRepository
          .findByRequestIdAndCallSeq(id, 0)
          .ifPresent(
              apiActivityLogEntity -> {
                apiActivityLogEntity.setWorkflowStatus(status);
                apiActivityLogEntity.setWorkflowInstanceId(entity.getWorkflowInstanceId());
                apiActivityLogEntity.setErrorMsg(errorMsg);
                apiActivityLogRepository.save(apiActivityLogEntity);
              });
    }
  }

  private void setOrderState(String id, String state) {
    Optional<HttpRequestEntity> optional = repository.findById(UUID.fromString(id));
    optional.ifPresent(
        entity -> {
          Map<String, Object> map = JsonToolkit.fromJson(entity.getRenderedResponse(), Map.class);
          map.put("state", state);
          entity.setRenderedResponse(map);
          repository.save(entity);
        });
  }
}
