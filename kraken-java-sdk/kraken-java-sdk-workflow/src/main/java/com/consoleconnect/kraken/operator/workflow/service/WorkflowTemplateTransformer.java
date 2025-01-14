package com.consoleconnect.kraken.operator.workflow.service;

import static com.consoleconnect.kraken.operator.core.enums.WorkflowStageEnum.*;
import static com.consoleconnect.kraken.operator.core.toolkit.Constants.*;

import com.consoleconnect.kraken.operator.core.enums.TaskEnum;
import com.consoleconnect.kraken.operator.core.enums.WorkflowStageEnum;
import com.consoleconnect.kraken.operator.core.exception.KrakenException;
import com.consoleconnect.kraken.operator.core.model.HttpTask;
import com.consoleconnect.kraken.operator.core.model.UnifiedAsset;
import com.consoleconnect.kraken.operator.core.model.facet.ComponentAPITargetFacets;
import com.consoleconnect.kraken.operator.core.model.facet.ComponentWorkflowFacets;
import com.consoleconnect.kraken.operator.core.toolkit.JsonToolkit;
import com.consoleconnect.kraken.operator.workflow.config.WorkflowConfig;
import com.consoleconnect.kraken.operator.workflow.model.EvaluateObject;
import com.consoleconnect.kraken.operator.workflow.model.HttpExtend;
import com.google.common.collect.Streams;
import com.netflix.conductor.common.metadata.tasks.TaskDef;
import com.netflix.conductor.common.metadata.workflow.WorkflowDef;
import com.netflix.conductor.common.metadata.workflow.WorkflowTask;
import com.netflix.conductor.common.run.Workflow;
import com.netflix.conductor.sdk.workflow.def.tasks.*;
import java.util.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;

@Component
public class WorkflowTemplateTransformer {
  private final WorkflowConfig.BuildInTask buildInTask;
  private static final String SUB_REQUEST_BODY = "${workflow.input.payload.%s.body}";
  private static final String SUB_REQUEST_URL = "${workflow.input.payload.%s.url}";
  private static final String SUB_REQUEST_HEADER = "${workflow.input.headers}";
  private static final String TERMINATE_TASK = "TERMINATE_TASK_";
  private static final String EVALUATE_RESULT = "${evaluate_payload_task_%s.output}";
  private static final String SUB_HTTP_TASK_RESPONSE = "${%s.output.response.body}";
  private static final String HTTP_STATUS_SWITCH_CASE_EXPRESSION =
      "${%s.output.response.statusCode}";

  private static final int RETRY_COUNT = 3;
  private static final String SWITCH_HTTP_CHECK_NAME_PREFIX = "switch_http_check_%s";
  private static final String SWITCH_CUSTOMIZED_CHECK_PREFIX = "switch_%s";

  public WorkflowTemplateTransformer(WorkflowConfig.BuildInTask buildInTask) {
    this.buildInTask = buildInTask;
  }

  public WorkflowDef transfer(UnifiedAsset asset) {
    ComponentWorkflowFacets facets = UnifiedAsset.getFacets(asset, ComponentWorkflowFacets.class);
    WorkflowDef workflowDef = new WorkflowDef();
    workflowDef.setName(facets.getMetaData().getWorkflowName());
    workflowDef.setOwnerEmail("example@email.com");
    List<WorkflowTask> taskList = new ArrayList<>();
    workflowDef.setTasks(taskList);
    List<HttpTask> httpTasks =
        Streams.concat(
                facets.getExecutionStage().stream(),
                facets.getValidationStage().stream(),
                facets.getPreparationStage().stream())
            .toList();
    buildWorkflowByStage(facets.getValidationStage(), workflowDef, VALIDATION_STAGE, httpTasks);
    buildWorkflowByStage(facets.getPreparationStage(), workflowDef, PREPARATION_STAGE, httpTasks);
    buildWorkflowByStage(facets.getExecutionStage(), workflowDef, EXECUTION_STAGE, httpTasks);
    return workflowDef;
  }

  private void buildWorkflowByStage(
      List<HttpTask> customizedTasks,
      WorkflowDef workflowDef,
      WorkflowStageEnum stage,
      List<HttpTask> httpTasks) {
    List<WorkflowTask> tasks = workflowDef.getTasks();
    if (CollectionUtils.isNotEmpty(customizedTasks)) {
      customizedTasks.stream()
          .forEachOrdered(
              task -> {
                if (StringUtils.isBlank(task.getEndpoint().getPath())) {
                  return;
                }
                // add evaluate sample task
                tasks.addAll(
                    constructSimpleTask(EVALUATE_PAYLOAD_TASK, task.getTaskName(), httpTasks)
                        .getWorkflowDefTasks());
                // add log request task
                tasks.addAll(
                    constructSimpleTask(LOG_REQUEST_PAYLOAD_TASK, task.getTaskName(), httpTasks)
                        .getWorkflowDefTasks());
                // add http task
                tasks.addAll(constructHttpTask(task).getRight());
                // add log response task
                tasks.addAll(
                    constructSimpleTask(LOG_RESPONSE_PAYLOAD_TASK, task.getTaskName(), httpTasks)
                        .getWorkflowDefTasks());
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
          .addAll(constructSimpleTask(PROCESS_ORDER_TASK, null, null).getWorkflowDefTasks());
    }
  }

  private List<WorkflowTask> constructHttpCheckSwitchTask(
      String taskName, WorkflowStageEnum stage) {
    Switch switchTask =
        new Switch(
            String.format(SWITCH_HTTP_CHECK_NAME_PREFIX, taskName),
            String.format(HTTP_STATUS_SWITCH_CASE_EXPRESSION, taskName));
    Map<String, List<Task<?>>> branches = new HashMap<>();
    branches.put("200", List.of(constructSimpleTask(EMPTY_TASK, null, null)));
    switchTask.decisionCases(branches);
    switchTask.defaultCase(
        constructSimpleTask(stage == EXECUTION_STAGE ? NOTIFY_TASK : FAIL_ORDER_TASK, null, null),
        new Terminate(getUniqueTaskRef(TERMINATE_TASK), Workflow.WorkflowStatus.COMPLETED, null));
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
    branches.put(
        caseValue,
        List.of(
            constructSimpleTask(buildInTaskName, null, null),
            new Terminate(
                getUniqueTaskRef(TERMINATE_TASK), Workflow.WorkflowStatus.COMPLETED, null)));
    switchTask.decisionCases(branches);
    switchTask.setOptional(true);
    return switchTask.getWorkflowDefTasks();
  }

  private SimpleTask constructSimpleTask(String taskName, String httpTask, List<HttpTask> tasks) {
    String taskRef =
        Objects.equals(taskName, EVALUATE_PAYLOAD_TASK)
            ? String.format("%s_%s", EVALUATE_PAYLOAD_TASK, httpTask)
            : getUniqueTaskRef(taskName);
    return buildInTask.getParams().entrySet().stream()
        .filter(v -> Objects.equals(taskName, v.getKey()))
        .map(entry -> buildSimpleTask(entry.getKey(), taskRef, httpTask, entry.getValue(), tasks))
        .findAny()
        .orElseThrow(
            () ->
                KrakenException.internalError(
                    String.format("can not found build-in task: %s", taskName)));
  }

  private static String getUniqueTaskRef(String key) {
    return key
        + com.consoleconnect.kraken.operator.core.toolkit.StringUtils.shortenUUID(
            UUID.randomUUID().toString());
  }

  private SimpleTask buildSimpleTask(
      String taskName,
      String taskRef,
      String httpTask,
      Map<String, String> input,
      List<HttpTask> tasks) {
    SimpleTask simpleTask = new SimpleTask(taskName, taskRef);
    switch (taskName) {
      case EVALUATE_PAYLOAD_TASK -> {
        Map<String, Map<String, String>> value = new HashMap<>();
        tasks.stream()
            .filter(task -> StringUtils.isNotBlank(task.getEndpoint().getPath()))
            .forEach(
                task ->
                    value.put(
                        task.getTaskName(),
                        Map.of("output", String.format("${%s.output}", task.getTaskName()))));
        EvaluateObject evaluateObject = new EvaluateObject();
        evaluateObject.setExpression(String.format(SUB_REQUEST_BODY, httpTask));
        evaluateObject.setValue(value);
        simpleTask
            .getInput()
            .putAll(JsonToolkit.fromJson(JsonToolkit.toJson(evaluateObject), Map.class));
      }
      case LOG_REQUEST_PAYLOAD_TASK -> {
        simpleTask.getInput().putAll(input);
        simpleTask.getInput().putAll(Map.of("payload", String.format(EVALUATE_RESULT, httpTask)));
      }
      case LOG_RESPONSE_PAYLOAD_TASK -> {
        simpleTask.getInput().putAll(input);
        simpleTask
            .getInput()
            .putAll(Map.of("payload", String.format(SUB_HTTP_TASK_RESPONSE, httpTask)));
      }
      default -> simpleTask.getInput().putAll(input);
    }
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
            .url(String.format(SUB_REQUEST_URL, httpTask.getTaskName()))
            .method(HttpExtend.Input.HttpMethod.getIgnoreCase(endpoint.getMethod()));
    http.headers(SUB_REQUEST_HEADER);
    http.setOptional(true);
    TaskDef def = new TaskDef(http.getName());
    if (!"GET".equalsIgnoreCase(endpoint.getMethod())) {
      http.body(String.format(EVALUATE_RESULT, httpTask.getTaskName()));
      def.setRetryCount(0);
    } else {
      // add retry for get request
      def.setRetryCount(RETRY_COUNT);
    }
    return Pair.of(def, http.getWorkflowDefTasks());
  }
}
