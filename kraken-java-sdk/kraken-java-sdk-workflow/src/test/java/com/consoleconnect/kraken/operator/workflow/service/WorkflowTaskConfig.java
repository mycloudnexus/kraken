package com.consoleconnect.kraken.operator.workflow.service;

import static com.consoleconnect.kraken.operator.core.toolkit.Constants.*;

import com.consoleconnect.kraken.operator.core.toolkit.JsonToolkit;
import com.consoleconnect.kraken.operator.workflow.model.EvaluateResult;
import com.consoleconnect.kraken.operator.workflow.model.LogTaskRequest;
import com.netflix.conductor.sdk.workflow.task.InputParam;
import com.netflix.conductor.sdk.workflow.task.WorkerTask;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@AllArgsConstructor
@Getter
public class WorkflowTaskConfig implements WorkflowTaskRegister {

  @WorkerTask(NOTIFY_TASK)
  public void notify(
      @InputParam("id") String id, @InputParam("notificationUrl") String notificationUrl) {
    log.info("Failed to delete order: {}", id);
  }

  @WorkerTask(FAIL_ORDER_TASK)
  public void failOrder(@InputParam("id") String id) {
    log.info("Set order to failed: {}", id);
  }

  @WorkerTask(value = EVALUATE_PAYLOAD_TASK, pollingInterval = 10)
  public EvaluateResult evaluateTask(
      @InputParam("value") Map<String, Object> value,
      @InputParam("urlExpression") String urlExpression,
      @InputParam("bodyExpression") Object bodyExpression) {
    return new EvaluateResult();
  }

  @WorkerTask(value = EVALUATE_EXPRESSION_TASK, pollingInterval = 10)
  public EvaluateResult evaluateExpressionTask(
      @InputParam("value") Map<String, Object> value, @InputParam("expression") String expression) {
    return new EvaluateResult();
  }

  @WorkerTask(LOG_PAYLOAD_TASK)
  public void logRequestPayload(@InputParam("payload") LogTaskRequest payload) {
    log.info("log payload: {}", JsonToolkit.toJson(payload));
  }

  @WorkerTask(EMPTY_TASK)
  public void doNothing() {
    log.info("empty task");
  }

  @WorkerTask(REJECT_ORDER_TASK)
  public void rejectOrder(@InputParam("id") String id) {
    log.info("Set order to rejected: {}", id);
  }

  @WorkerTask(PROCESS_ORDER_TASK)
  public void processOrder(@InputParam("id") String id) {
    log.info("Set order to inProgress: {}", id);
  }

  @WorkerTask(PERSIST_RESPONSE_TASK)
  public void persistResponse(@InputParam("id") String id, @InputParam("payload") Object payload) {
    log.info("persist response: {}", id);
  }

  @WorkerTask(WORKFLOW_SUCCESS_TASK)
  public void workflowSuccessTask(@InputParam("id") String id) {
    log.info("process workflow: {}", id);
  }

  @WorkerTask(WORKFLOW_FAILED_TASK)
  public void workflowFailedTask(
      @InputParam("id") String id, @InputParam("errorMsg") String errorMsg) {
    log.info("process workflow: {}", id);
  }
}
