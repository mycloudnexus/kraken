package com.consoleconnect.kraken.operator.workflow.config;

import com.consoleconnect.kraken.operator.core.model.AppProperty;
import io.orkes.conductor.client.ApiClient;
import io.orkes.conductor.client.http.OrkesMetadataClient;
import io.orkes.conductor.client.http.OrkesWorkflowClient;
import lombok.AllArgsConstructor;
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
}
