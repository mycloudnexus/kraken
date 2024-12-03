package com.consoleconnect.kraken.operator.core.config;

import com.consoleconnect.kraken.operator.core.model.AppProperty;
import io.orkes.conductor.client.ApiClient;
import io.orkes.conductor.client.http.OrkesMetadataClient;
import io.orkes.conductor.client.http.OrkesWorkflowClient;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@AutoConfigureAfter(AppProperty.class)
public class WorkflowConfig {

  @Bean
  public OrkesWorkflowClient getWorkflowClient(AppProperty appProperty) {
    return new OrkesWorkflowClient(getApiClient(appProperty));
  }

  @Bean
  public OrkesMetadataClient getMetaDataClient(AppProperty appProperty) {
    return new OrkesMetadataClient(getApiClient(appProperty));
  }

  private ApiClient getApiClient(AppProperty appProperty) {
    return new ApiClient(appProperty.getWorkflow().getBaseUrl());
  }
}
