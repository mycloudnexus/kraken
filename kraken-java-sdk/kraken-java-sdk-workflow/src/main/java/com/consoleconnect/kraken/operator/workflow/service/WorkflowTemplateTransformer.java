package com.consoleconnect.kraken.operator.workflow.service;

import static com.consoleconnect.kraken.operator.core.enums.WorkflowStageEnum.*;
import static com.consoleconnect.kraken.operator.core.toolkit.Constants.*;

import com.consoleconnect.kraken.operator.core.enums.OperatorEnum;
import com.consoleconnect.kraken.operator.core.enums.TaskEnum;
import com.consoleconnect.kraken.operator.core.enums.WorkflowStageEnum;
import com.consoleconnect.kraken.operator.core.exception.KrakenException;
import com.consoleconnect.kraken.operator.core.model.AppProperty;
import com.consoleconnect.kraken.operator.core.model.ConditionItem;
import com.consoleconnect.kraken.operator.core.model.HttpTask;
import com.consoleconnect.kraken.operator.core.model.UnifiedAsset;
import com.consoleconnect.kraken.operator.core.model.facet.ComponentAPITargetFacets;
import com.consoleconnect.kraken.operator.core.model.facet.ComponentWorkflowFacets;
import com.consoleconnect.kraken.operator.core.toolkit.ConstructExpressionUtil;
import com.consoleconnect.kraken.operator.core.toolkit.JsonToolkit;
import com.consoleconnect.kraken.operator.workflow.config.WorkflowConfig;
import com.consoleconnect.kraken.operator.workflow.model.EvaluateObject;
import com.consoleconnect.kraken.operator.workflow.model.HttpExtend;
import com.consoleconnect.kraken.operator.workflow.model.LogTaskRequest;
import com.google.common.collect.Streams;
import com.netflix.conductor.common.metadata.tasks.TaskDef;
import com.netflix.conductor.common.metadata.workflow.WorkflowDef;
import com.netflix.conductor.common.metadata.workflow.WorkflowTask;
import com.netflix.conductor.common.run.Workflow;
import com.netflix.conductor.sdk.workflow.def.tasks.*;
import java.util.*;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class WorkflowTemplateTransformer {
  private final WorkflowConfig.BuildInTask buildInTask;
  private final AppProperty appProperty;
  private static final String SUB_REQUEST_BODY = "${workflow.input.payload.%s.body}";
  private static final String SUB_REQUEST_URL = "${workflow.input.payload.%s.url}";
  private static final String SUB_REQUEST_HEADER = "${workflow.input.headers}";
  private static final String TERMINATE_TASK = "TERMINATE_TASK_";
  private static final String EVALUATE_BODY_RESULT = "${evaluate_payload_task_%s.output.body}";
  private static final String EVALUATE_URL_RESULT = "${evaluate_payload_task_%s.output.url}";
  private static final String EVALUATE_EXPRESSION_RESULT =
      "${evaluate_expression_task_%s.output.singleResult}";
  private static final String CONTAINS_EXPRESSION = "%s.contains('%s')";
  private static final String SUB_TASK_OUTPUT = "${%s.output}";
  private static final String SUB_TASK_INPUT = "${%s.input}";
  private static final String SUB_HTTP_TASK_RESPONSE = "${%s.output.response.body}";
  private static final String HTTP_STATUS_SWITCH_CASE_EXPRESSION =
      "${%s.output.response.statusCode}";
  private static final String OUT_PUT = "output";
  private static final String REQUEST_ID = "requestId";
  private static final String PAYLOAD = "payload";

  private static final int RETRY_COUNT = 3;
  private static final String SWITCH_HTTP_CHECK_NAME_PREFIX = "switch_http_check_%s";
  private static final String SWITCH_CUSTOMIZED_CHECK_PREFIX = "switch_%s";

  public WorkflowTemplateTransformer(
      WorkflowConfig.BuildInTask buildInTask, AppProperty appProperty) {
    this.buildInTask = buildInTask;
    this.appProperty = appProperty;
  }

  public WorkflowDef transfer(UnifiedAsset asset) {
    ComponentWorkflowFacets facets = UnifiedAsset.getFacets(asset, ComponentWorkflowFacets.class);
    WorkflowDef workflowDef = new WorkflowDef();
    workflowDef.setName(facets.getMetaData().getWorkflowName());
    workflowDef.setOwnerEmail(appProperty.getWorkflow().getEmail());
    List<WorkflowTask> taskList = new ArrayList<>();
    workflowDef.setTasks(taskList);
    List<HttpTask> httpTasks =
        Streams.concat(
                facets.getValidationStage().stream(),
                facets.getPreparationStage().stream(),
                facets.getExecutionStage().stream())
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
                    constructSimpleTask(EVALUATE_PAYLOAD_TASK, task, httpTasks)
                        .getWorkflowDefTasks());
                // add http task
                tasks.addAll(constructHttpTask(task).getRight());
                // add log task
                tasks.addAll(
                    constructSimpleTask(LOG_PAYLOAD_TASK, task, httpTasks).getWorkflowDefTasks());
                // add check http status task for each http task
                tasks.addAll(constructHttpCheckSwitchTask(task.getTaskName(), stage));
                if (task.getConditionCheck() != null
                    && CollectionUtils.isNotEmpty(task.getConditionCheck().getConditionItems())) {
                  tasks.addAll(
                      constructSimpleTask(EVALUATE_EXPRESSION_TASK, task, httpTasks)
                          .getWorkflowDefTasks());
                  tasks.addAll(constructCustomizedSwitchTask(task));
                }
              });
    }
    buildAfterEachStageTask(workflowDef, stage, httpTasks.get(httpTasks.size() - 1));
  }

  private void buildAfterEachStageTask(
      WorkflowDef workflowDef, WorkflowStageEnum stage, HttpTask httpTask) {
    if (stage == PREPARATION_STAGE) {
      workflowDef
          .getTasks()
          .addAll(constructSimpleTask(PROCESS_ORDER_TASK, null, null).getWorkflowDefTasks());
    }
    if (stage == EXECUTION_STAGE) {
      workflowDef
          .getTasks()
          .addAll(constructSimpleTask(PERSIST_RESPONSE_TASK, httpTask, null).getWorkflowDefTasks());
    }
  }

  private List<WorkflowTask> constructHttpCheckSwitchTask(
      String taskName, WorkflowStageEnum stage) {
    Switch switchTask =
        new Switch(
            String.format(SWITCH_HTTP_CHECK_NAME_PREFIX, taskName),
            String.format(HTTP_STATUS_SWITCH_CASE_EXPRESSION, taskName));
    Map<String, List<Task<?>>> branches = new HashMap<>();
    branches.put(
        String.valueOf(HttpStatus.OK.value()),
        List.of(constructSimpleTask(EMPTY_TASK, null, null)));
    switchTask.decisionCases(branches);
    switchTask.defaultCase(
        constructSimpleTask(stage == EXECUTION_STAGE ? NOTIFY_TASK : FAIL_ORDER_TASK, null, null),
        new Terminate(getUniqueTaskRef(TERMINATE_TASK), Workflow.WorkflowStatus.COMPLETED, null));
    return switchTask.getWorkflowDefTasks();
  }

  private List<WorkflowTask> constructCustomizedSwitchTask(HttpTask task) {
    Switch switchTask =
        new Switch(
            String.format(SWITCH_CUSTOMIZED_CHECK_PREFIX, task.getTaskName()),
            String.format(EVALUATE_EXPRESSION_RESULT, task.getTaskName()));
    Map<String, List<Task<?>>> branches = new HashMap<>();
    branches.put(
        Boolean.FALSE.toString(),
        List.of(
            constructSimpleTask(task.getConditionCheck().getBuildInTask(), null, null),
            new Terminate(
                getUniqueTaskRef(TERMINATE_TASK), Workflow.WorkflowStatus.COMPLETED, null)));
    switchTask.decisionCases(branches);
    switchTask.setOptional(true);
    return switchTask.getWorkflowDefTasks();
  }

  private SimpleTask constructSimpleTask(String taskName, HttpTask httpTask, List<HttpTask> tasks) {
    String httpTaskName = httpTask == null ? StringUtils.EMPTY : httpTask.getTaskName();
    String taskRef =
        Objects.equals(taskName, EVALUATE_PAYLOAD_TASK)
                || Objects.equals(taskName, EVALUATE_EXPRESSION_TASK)
            ? String.format("%s_%s", taskName, httpTaskName)
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
      HttpTask httpTask,
      Map<String, String> input,
      List<HttpTask> tasks) {
    SimpleTask simpleTask = new SimpleTask(taskName, taskRef);
    switch (taskName) {
      case EVALUATE_PAYLOAD_TASK -> {
        EvaluateObject evaluateObject = buildEvaluateObject(httpTask, tasks);
        simpleTask
            .getInput()
            .putAll(JsonToolkit.fromJson(JsonToolkit.toJson(evaluateObject), Map.class));
      }
      case LOG_PAYLOAD_TASK -> {
        simpleTask.getInput().putAll(input);
        LogTaskRequest request = new LogTaskRequest();
        request.setRequestPayload(String.format(SUB_TASK_INPUT, httpTask.getTaskName()));
        request.setResponsePayload(String.format(SUB_TASK_OUTPUT, httpTask.getTaskName()));
        request.setRequestId(String.format(WORKFLOW_PARAM_PREFIX, REQUEST_ID));
        simpleTask.getInput().putAll(Map.of(PAYLOAD, request));
      }
      case PERSIST_RESPONSE_TASK -> {
        simpleTask.getInput().putAll(input);
        simpleTask
            .getInput()
            .putAll(Map.of(PAYLOAD, String.format(SUB_HTTP_TASK_RESPONSE, httpTask.getTaskName())));
      }
      case EVALUATE_EXPRESSION_TASK -> {
        simpleTask.getInput().putAll(input);
        EvaluateObject evaluateObject = constructMultiConditionExpression(httpTask, tasks);
        simpleTask
            .getInput()
            .putAll(JsonToolkit.fromJson(JsonToolkit.toJson(evaluateObject), Map.class));
      }
      default -> simpleTask.getInput().putAll(input);
    }
    simpleTask.setOptional(true);
    return simpleTask;
  }

  private static EvaluateObject constructMultiConditionExpression(
      HttpTask httpTask, List<HttpTask> tasks) {
    List<ConditionItem> conditionItems = httpTask.getConditionCheck().getConditionItems();
    String joinExpression =
        conditionItems.stream()
            .map(
                conditionItem -> {
                  StringBuilder builder = new StringBuilder();
                  String s =
                      ConstructExpressionUtil.formatTaskExpression(conditionItem.getExpression());
                  String value = conditionItem.getValue();
                  if (conditionItem.getOperator() == OperatorEnum.IN) {
                    builder.append(String.format(CONTAINS_EXPRESSION, s, value));
                  } else {
                    builder
                        .append(s)
                        .append(conditionItem.getOperator().getDescription())
                        .append(value);
                  }
                  return builder.toString();
                })
            .collect(Collectors.joining(httpTask.getConditionCheck().getJoin().getDescription()));

    EvaluateObject evaluateObject = buildEvaluateObject(httpTask, tasks);
    evaluateObject.setExpression(joinExpression);
    return evaluateObject;
  }

  private static EvaluateObject buildEvaluateObject(HttpTask httpTask, List<HttpTask> tasks) {
    Map<String, Map<String, String>> value = new HashMap<>();
    tasks.stream()
        .filter(task -> StringUtils.isNotBlank(task.getEndpoint().getPath()))
        .forEach(
            task ->
                value.put(
                    task.getTaskName(),
                    Map.of(OUT_PUT, String.format(SUB_TASK_OUTPUT, task.getTaskName()))));
    EvaluateObject evaluateObject = new EvaluateObject();
    evaluateObject.setBodyExpression(String.format(SUB_REQUEST_BODY, httpTask.getTaskName()));
    evaluateObject.setUrlExpression(String.format(SUB_REQUEST_URL, httpTask.getTaskName()));
    evaluateObject.setValue(value);
    return evaluateObject;
  }

  private Pair<TaskDef, List<WorkflowTask>> constructHttpTask(HttpTask httpTask) {
    if (!TaskEnum.HTTP.getName().equalsIgnoreCase(httpTask.getTaskType())) {
      throw KrakenException.badRequest(
          String.format("not supported task type: %s", httpTask.getTaskType()));
    }
    ComponentAPITargetFacets.Endpoint endpoint = httpTask.getEndpoint();
    HttpExtend http =
        new HttpExtend(httpTask.getTaskName())
            .url(String.format(EVALUATE_URL_RESULT, httpTask.getTaskName()))
            .method(HttpExtend.Input.HttpMethod.getIgnoreCase(endpoint.getMethod()));
    http.headers(SUB_REQUEST_HEADER);
    http.setOptional(true);
    TaskDef def = new TaskDef(http.getName());
    if (!HttpMethod.GET.name().equalsIgnoreCase(endpoint.getMethod())) {
      http.body(String.format(EVALUATE_BODY_RESULT, httpTask.getTaskName()));
      def.setRetryCount(0);
    } else {
      // add retry for get request
      def.setRetryCount(RETRY_COUNT);
    }
    return Pair.of(def, http.getWorkflowDefTasks());
  }
}
