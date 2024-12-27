package com.consoleconnect.kraken.operator.gateway.service.workflow;

import static com.consoleconnect.kraken.operator.core.enums.WorkflowStageEnum.*;
import static com.consoleconnect.kraken.operator.gateway.service.workflow.WorkflowTaskConfig.*;

import com.consoleconnect.kraken.operator.core.enums.TaskEnum;
import com.consoleconnect.kraken.operator.core.enums.WorkflowStageEnum;
import com.consoleconnect.kraken.operator.core.exception.KrakenException;
import com.consoleconnect.kraken.operator.core.model.UnifiedAsset;
import com.consoleconnect.kraken.operator.core.model.facet.ComponentAPITargetFacets;
import com.consoleconnect.kraken.operator.core.model.facet.ComponentWorkflowFacets;
import com.consoleconnect.kraken.operator.core.model.workflow.HttpExtend;
import com.consoleconnect.kraken.operator.core.model.workflow.HttpTask;
import com.netflix.conductor.common.metadata.tasks.TaskDef;
import com.netflix.conductor.common.metadata.workflow.WorkflowDef;
import com.netflix.conductor.common.metadata.workflow.WorkflowTask;
import com.netflix.conductor.sdk.workflow.def.tasks.SimpleTask;
import com.netflix.conductor.sdk.workflow.def.tasks.Switch;
import com.netflix.conductor.sdk.workflow.def.tasks.Task;
import java.util.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

public class WorkflowTemplateTransformer {
  private static final String SUB_REQUEST_BODY = "${workflow.input.%s.requestBody}";
  private static final String SUB_REQUEST_HEADER = "${workflow.input.%s.requestHeader}";
  private static final String HTTP_STATUS_SWITCH_CASE_EXPRESSION =
      "${%s.output.response.statusCode}";

  private static final int RETRY_COUNT = 3;
  private static final String SWITCH_HTTP_CHECK_NAME_PREFIX = "switch_http_check_%s";
  private static final String SWITCH_CUSTOMIZED_CHECK_PREFIX = "switch_%s";

  public WorkflowDef transfer(UnifiedAsset asset) {
    ComponentWorkflowFacets facets = UnifiedAsset.getFacets(asset, ComponentWorkflowFacets.class);
    WorkflowDef workflowDef = new WorkflowDef();
    workflowDef.setName(asset.getMetadata().getKey());
    List<WorkflowTask> taskList = new ArrayList<>();
    workflowDef.setTasks(taskList);
    buildWorkflowByStage(facets.getValidationStage(), workflowDef, VALIDATION_STAGE);
    buildWorkflowByStage(facets.getPreparationStage(), workflowDef, PREPARATION_STAGE);
    buildWorkflowByStage(facets.getExecutionStage(), workflowDef, EXECUTION_STAGE);
    return workflowDef;
  }

  private void buildWorkflowByStage(
      List<HttpTask> customizedTasks, WorkflowDef workflowDef, WorkflowStageEnum stage) {
    List<WorkflowTask> tasks = workflowDef.getTasks();
    if (CollectionUtils.isNotEmpty(customizedTasks)) {
      customizedTasks.stream()
          .forEachOrdered(
              task -> {
                // add http task
                tasks.addAll(constructHttpTask(task).getRight());
                // add check http status task for each http task
                tasks.addAll(constructHttpCheckSwitchTask(task.getTaskName(), stage));
                if (task.getConditionCheck() != null
                    && StringUtils.isNotBlank(task.getConditionCheck().getCaseExpression())) {
                  tasks.addAll(
                      constructCustomizedSwitchTask(task.getTaskName(), task.getConditionCheck()));
                }
              });
    }
    buildAfterEachStageTask(workflowDef, stage);
  }

  private void buildAfterEachStageTask(WorkflowDef workflowDef, WorkflowStageEnum stage) {
    if (stage == PREPARATION_STAGE) {
      workflowDef
          .getTasks()
          .addAll(constructSimpleTask(PROCESS_ORDER_TASK_VALUE).getWorkflowDefTasks());
    }
  }

  private List<WorkflowTask> constructHttpCheckSwitchTask(
      String taskName, WorkflowStageEnum stage) {
    Switch switchTask =
        new Switch(
            String.format(SWITCH_HTTP_CHECK_NAME_PREFIX, taskName),
            String.format(HTTP_STATUS_SWITCH_CASE_EXPRESSION, taskName));
    Map<String, List<Task<?>>> branches = new HashMap<>();
    branches.put("200", List.of(constructSimpleTask(EMPTY_TASK_VALUE)));
    branches.put("201", List.of(constructSimpleTask(EMPTY_TASK_VALUE)));
    switchTask.decisionCases(branches);
    switchTask.defaultCase(
        constructSimpleTask(stage == EXECUTION_STAGE ? NOTIFY_TASK_VALUE : FAIL_ORDER_TASK_VALUE));
    return switchTask.getWorkflowDefTasks();
  }

  private List<WorkflowTask> constructCustomizedSwitchTask(
      String taskName, HttpTask.ConditionCheck conditionCheck) {
    // expression format should be like: ${workflow.taskName.output.response.body.state} == DELETED
    String caseExpression = conditionCheck.getCaseExpression();
    String buildInTaskName = conditionCheck.getBuildInTask();
    String[] split = caseExpression.trim().split("==");
    if (split.length < 2) {
      throw KrakenException.badRequest(
          String.format("wrong format of case expression: %s", caseExpression));
    }
    String expression = split[0];
    String caseValue = split[1];
    Switch switchTask =
        new Switch(String.format(SWITCH_CUSTOMIZED_CHECK_PREFIX, taskName), expression);
    Map<String, List<Task<?>>> branches = new HashMap<>();
    branches.put(caseValue, List.of(constructSimpleTask(buildInTaskName)));
    switchTask.decisionCases(branches);
    switchTask.setOptional(true);
    return switchTask.getWorkflowDefTasks();
  }

  private SimpleTask constructSimpleTask(String taskName) {
    return task2InputParamMap.entrySet().stream()
        .filter(v -> Objects.equals(taskName, v.getKey()))
        .map(entry -> buildSimpleTask(entry.getKey(), entry.getKey(), entry.getValue()))
        .findAny()
        .orElseThrow(
            () ->
                KrakenException.internalError(
                    String.format("can not found build-in task: %s", taskName)));
  }

  private SimpleTask buildSimpleTask(String taskName, String taskRef, Map<String, String> input) {
    SimpleTask simpleTask = new SimpleTask(taskName, taskRef);
    simpleTask.getInput().putAll(input);
    simpleTask.setOptional(true);
    return simpleTask;
  }

  private Pair<TaskDef, List<WorkflowTask>> constructHttpTask(HttpTask httpTask) {
    if (!TaskEnum.HTTP.getName().equalsIgnoreCase(httpTask.getTaskType())) {
      throw KrakenException.badRequest(
          String.format("not supported task type: %s", httpTask.getTaskType()));
    }
    ComponentAPITargetFacets.Endpoint endpoint = httpTask.getEndpoint();
    HttpExtend http =
        new HttpExtend(httpTask.getTaskName())
            .url(endpoint.getUrl())
            .method(HttpExtend.Input.HttpMethod.getIgnoreCase(endpoint.getMethod()));
    http.headers(SUB_REQUEST_HEADER);
    TaskDef def = new TaskDef(http.getName());
    if ("GET".equalsIgnoreCase(endpoint.getMethod())) {
      http.body(String.format(SUB_REQUEST_BODY, httpTask.getTaskName()));
      def.setRetryCount(0);
    } else {
      // add retry for get request
      def.setRetryCount(RETRY_COUNT);
    }
    return Pair.of(def, http.getWorkflowDefTasks());
  }
}
