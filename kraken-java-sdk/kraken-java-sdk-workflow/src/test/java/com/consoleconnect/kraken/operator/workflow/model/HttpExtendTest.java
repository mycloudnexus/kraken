package com.consoleconnect.kraken.operator.workflow.model;

import static org.junit.jupiter.api.Assertions.*;

import com.consoleconnect.kraken.operator.core.enums.TaskEnum;
import com.consoleconnect.kraken.operator.core.model.HttpTask;
import com.consoleconnect.kraken.operator.core.model.facet.ComponentAPITargetFacets;
import com.netflix.conductor.common.metadata.workflow.WorkflowTask;
import java.util.HashMap;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
class HttpExtendTest {

  @Test
  void givenTaskName_thenGetWorkerTask_thenOK() {
    HttpTask httpTask = new HttpTask();
    httpTask.setTaskName("test-task");
    httpTask.setEndpoint(new ComponentAPITargetFacets.Endpoint());
    httpTask.setConditionCheck(new HttpTask.ConditionCheck());
    httpTask.setTaskType(TaskEnum.HTTP.getName());
    HttpExtend extend = new HttpExtend("status_check");
    extend
        .body(new HashMap<>())
        .url("url")
        .headers(new HashMap<>())
        .method(HttpExtend.Input.HttpMethod.GET)
        .input(new HttpExtend.Input());
    List<WorkflowTask> workflowDefTasks = extend.getWorkflowDefTasks();
    assertNotNull(workflowDefTasks);
  }
}
