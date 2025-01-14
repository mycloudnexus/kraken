package com.consoleconnect.kraken.operator.workflow.service;

import static com.consoleconnect.kraken.operator.core.toolkit.Constants.*;

import com.consoleconnect.kraken.operator.workflow.model.LogTaskRequest;
import com.netflix.conductor.sdk.workflow.task.InputParam;
import com.netflix.conductor.sdk.workflow.task.WorkerTask;
import java.util.Map;

public interface WorkflowTaskRegister {

  @WorkerTask(NOTIFY_TASK)
  void notify(String id, String notificationUrl);

  @WorkerTask(FAIL_ORDER_TASK)
  void failOrder(String id);

  @WorkerTask(EMPTY_TASK)
  void doNothing();

  @WorkerTask(PROCESS_ORDER_TASK)
  void processOrder(String id);

  @WorkerTask(REJECT_ORDER_TASK)
  void rejectOrder(String id);

  @WorkerTask(EVALUATE_PAYLOAD_TASK)
  Map<String, Object> evaluateTask(Map<String, Object> value, Object expression);

  @WorkerTask(LOG_PAYLOAD_TASK)
  void logRequestPayload(LogTaskRequest payload);

  @WorkerTask(PERSIST_RESPONSE_TASK)
  void persistResponse(@InputParam("id") String id, @InputParam("payload") Object payload);
}
