package com.consoleconnect.kraken.operator.gateway.service.workflow;

import com.consoleconnect.kraken.operator.core.exception.KrakenException;
import com.consoleconnect.kraken.operator.core.toolkit.JsonToolkit;
import com.consoleconnect.kraken.operator.gateway.entity.HttpRequestEntity;
import com.consoleconnect.kraken.operator.gateway.repo.HttpRequestRepository;
import com.netflix.conductor.sdk.workflow.task.InputParam;
import com.netflix.conductor.sdk.workflow.task.WorkerTask;
import jakarta.annotation.PostConstruct;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Component
@AllArgsConstructor
@Getter
public class WorkflowTaskConfig {
  public static final String FAIL_ORDER_TASK_VALUE = "fail_order_task";
  public static final String REJECT_ORDER_TASK_VALUE = "reject_order_task";
  public static final String EMPTY_TASK_VALUE = "empty_task";
  public static final String PROCESS_ORDER_TASK_VALUE = "process_order_task";
  public static final String NOTIFY_TASK_VALUE = "notify_task";
  private static final String WORKFLOW_PARAM_PREFIX = "${workflow.input.%s}";
  private final HttpRequestRepository repository;
  protected static final Map<String, Map<String, String>> task2InputParamMap = new HashMap<>();

  @WorkerTask(NOTIFY_TASK_VALUE)
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

  @WorkerTask(FAIL_ORDER_TASK_VALUE)
  public void failOrder(@InputParam("id") String id) {
    log.info("Set order to failed: {}", id);
    setOrderState(id, "failed");
  }

  @WorkerTask(EMPTY_TASK_VALUE)
  public void doNothing() {
    log.info("empty task");
  }

  @WorkerTask(REJECT_ORDER_TASK_VALUE)
  public void rejectOrder(@InputParam("id") String id) {
    log.info("Set order to rejected: {}", id);
    setOrderState(id, "rejected");
  }

  @WorkerTask(PROCESS_ORDER_TASK_VALUE)
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

  @PostConstruct
  public void init() {
    Class<?> clazz = this.getClass();
    for (Method method : clazz.getMethods()) {
      WorkerTask annotation = method.getAnnotation(WorkerTask.class);
      if (annotation == null) {
        continue;
      }
      Map<String, String> paramMap =
          Arrays.stream(method.getParameters())
              .collect(
                  Collectors.toMap(
                      Parameter::getName, v -> String.format(WORKFLOW_PARAM_PREFIX, v.getName())));
      task2InputParamMap.put(annotation.value(), paramMap);
    }
  }
}
