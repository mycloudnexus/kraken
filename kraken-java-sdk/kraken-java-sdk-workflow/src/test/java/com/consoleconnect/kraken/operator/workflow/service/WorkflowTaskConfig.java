package com.consoleconnect.kraken.operator.workflow.service;

import static com.consoleconnect.kraken.operator.core.toolkit.Constants.*;

import com.netflix.conductor.sdk.workflow.task.InputParam;
import com.netflix.conductor.sdk.workflow.task.WorkerTask;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@AllArgsConstructor
@Getter
public class WorkflowTaskConfig {

  @WorkerTask(NOTIFY_TASK_VALUE)
  public void notifyOrder() {
    log.info("notify task");
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
  }

  @WorkerTask(PROCESS_ORDER_TASK_VALUE)
  public void processOrder(@InputParam("id") String id) {
    log.info("Set order to inProgress: {}", id);
  }

  private void setOrderState(String id, String state) {
    log.info("set order id: {} state: {}", id, state);
  }
}
