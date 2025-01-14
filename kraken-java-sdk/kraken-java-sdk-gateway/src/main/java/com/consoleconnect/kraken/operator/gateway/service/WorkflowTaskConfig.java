package com.consoleconnect.kraken.operator.gateway.service;

import static com.consoleconnect.kraken.operator.core.toolkit.Constants.*;

import com.consoleconnect.kraken.operator.core.exception.KrakenException;
import com.consoleconnect.kraken.operator.core.toolkit.JsonToolkit;
import com.consoleconnect.kraken.operator.gateway.entity.HttpRequestEntity;
import com.consoleconnect.kraken.operator.gateway.repo.HttpRequestRepository;
import com.consoleconnect.kraken.operator.gateway.template.SpELEngine;
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

  @WorkerTask(EVALUATE_PAYLOAD_TASK)
  public Map<String, Object> evaluateTask(
      @InputParam("value") Map<String, Object> value, @InputParam("expression") Object expression) {
    String evaluate = SpELEngine.evaluate(expression, value);
    return StringUtils.isBlank(evaluate)
        ? Collections.emptyMap()
        : JsonToolkit.fromJson(evaluate, Map.class);
  }

  @WorkerTask(LOG_REQUEST_PAYLOAD_TASK)
  public void logRequestPayload(
      @InputParam("payload") Object payload, @InputParam("requestId") String requestId) {
    // log request
    log.info(
        "request payload: requestId= {},  payload= {}", requestId, JsonToolkit.toJson(payload));
  }

  @WorkerTask(LOG_RESPONSE_PAYLOAD_TASK)
  public void logResponsePayload(
      @InputParam("payload") Object payload, @InputParam("requestId") String requestId) {
    // log response
    log.info(
        "response payload: requestId= {},  payload= {}", requestId, JsonToolkit.toJson(payload));
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
