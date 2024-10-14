package com.consoleconnect.kraken.operator.controller.config;

import com.consoleconnect.kraken.operator.controller.model.MgmtEndpointEnabled;
import com.consoleconnect.kraken.operator.controller.model.MgmtProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MgmtConfig {

  @Bean
  @ConditionalOnProperty(value = "app.mgmt.enabled", havingValue = "true", matchIfMissing = false)
  public MgmtEndpointEnabled mgmtEndpointEnabled() {
    return new MgmtEndpointEnabled();
  }

  @Bean
  @ConfigurationProperties(prefix = "app.mgmt")
  public MgmtProperty mgmtProperty() {
    return new MgmtProperty();
  }
}
