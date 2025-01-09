package com.consoleconnect.kraken.operator.workflow.service;

import static com.consoleconnect.kraken.operator.core.toolkit.Constants.*;

import com.netflix.conductor.sdk.workflow.task.WorkerTask;

public interface WorkflowTaskRegister {

  @WorkerTask(NOTIFY_TASK_VALUE)
  void notify(String id, String notificationUrl);

  @WorkerTask(FAIL_ORDER_TASK_VALUE)
  void failOrder(String id);

  @WorkerTask(EMPTY_TASK_VALUE)
  void doNothing();

  @WorkerTask(PROCESS_ORDER_TASK_VALUE)
  void processOrder(String id);

  @WorkerTask(REJECT_ORDER_TASK_VALUE)
  void rejectOrder(String id);
}
