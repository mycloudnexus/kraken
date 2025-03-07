package com.consoleconnect.kraken.operator.workflow.config;

import static com.consoleconnect.kraken.operator.core.toolkit.Constants.WORKFLOW_PARAM_PREFIX;

import com.consoleconnect.kraken.operator.core.exception.KrakenException;
import com.consoleconnect.kraken.operator.core.model.AppProperty;
import com.consoleconnect.kraken.operator.workflow.service.WorkflowTaskRegister;
import com.netflix.conductor.sdk.workflow.executor.task.AnnotatedWorkerExecutor;
import com.netflix.conductor.sdk.workflow.task.WorkerTask;
import io.orkes.conductor.client.ApiClient;
import io.orkes.conductor.client.OrkesClients;
import io.orkes.conductor.client.http.OrkesMetadataClient;
import io.orkes.conductor.client.http.OrkesWorkflowClient;
import jakarta.annotation.PostConstruct;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@AutoConfigureAfter(AppProperty.class)
@Getter
@AllArgsConstructor
@Slf4j
public class WorkflowConfig {
  private final AppProperty appProperty;
  private final ApplicationContext context;
  private static final String TASK_LOCATION = "com.consoleconnect.kraken.operator.workflow.service";

  @Bean
  public OrkesWorkflowClient getWorkflowClient(AppProperty appProperty) {
    return new OrkesWorkflowClient(getApiClient(appProperty));
  }

  @Bean
  public OrkesMetadataClient getMetaDataClient(AppProperty appProperty) {
    return new OrkesMetadataClient(getApiClient(appProperty));
  }

  @Bean
  public ApiClient getApiClient(AppProperty appProperty) {
    return new ApiClient(appProperty.getWorkflow().getBaseUrl());
  }

  @Data
  public static class BuildInTask {
    private Map<String, Map<String, String>> params = new HashMap<>();
  }

  @Bean
  public BuildInTask getBuildinTask() {
    BuildInTask buildInTask = new BuildInTask();
    Map<String, Map<String, String>> task2InputParamMap = new HashMap<>();
    for (Method method : WorkflowTaskRegister.class.getMethods()) {
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

    buildInTask.setParams(task2InputParamMap);
    return buildInTask;
  }

  @PostConstruct
  public void init() {
    if (!appProperty.getWorkflow().isEnableRegisterWorker()) {
      return;
    }
    if (appProperty.getWorkflow() != null && appProperty.getWorkflow().isEnabled()) {
      if (CollectionUtils.isNotEmpty(appProperty.getWorkflow().getClusterUrl())) {
        log.info("start to init worker for cluster");
        appProperty.getWorkflow().getClusterUrl().stream().forEach(this::initWorker);
      } else {
        log.info("start to init worker for standalone conductor");
        initWorker(appProperty.getWorkflow().getBaseUrl());
      }
    }
  }

  private void initWorker(String nodeUrl) {
    try {
      ApiClient apiClient = new ApiClient(nodeUrl);
      log.info("register worker for node: {}", nodeUrl);
      OrkesClients oc = new OrkesClients(apiClient);
      AnnotatedWorkerExecutor annotatedWorkerExecutor =
          new AnnotatedWorkerExecutor(
              oc.getTaskClient(), appProperty.getWorkflow().getPollingIntervalMills());
      WorkflowTaskRegister workflowTaskConfig = context.getBean(WorkflowTaskRegister.class);
      annotatedWorkerExecutor.addBean(workflowTaskConfig);
      annotatedWorkerExecutor.initWorkers(TASK_LOCATION);
    } catch (Exception e) {
      throw KrakenException.internalError(
          String.format("Failed to register worker for %s, error: %s", nodeUrl, e.getMessage()));
    }
  }
}
