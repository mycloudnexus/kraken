package com.consoleconnect.kraken.operator.workflow.config;

import static com.consoleconnect.kraken.operator.core.toolkit.Constants.WORKFLOW_PARAM_PREFIX;

import com.consoleconnect.kraken.operator.core.model.AppProperty;
import com.consoleconnect.kraken.operator.workflow.service.WorkflowTaskRegister;
import com.netflix.conductor.sdk.workflow.task.WorkerTask;
import io.orkes.conductor.client.ApiClient;
import io.orkes.conductor.client.http.OrkesMetadataClient;
import io.orkes.conductor.client.http.OrkesWorkflowClient;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@AutoConfigureAfter(AppProperty.class)
@Getter
@AllArgsConstructor
public class WorkflowConfig {
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
}
