package com.consoleconnect.kraken.operator.core.config;

import com.consoleconnect.kraken.operator.core.model.AppProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

  @Bean
  @ConfigurationProperties(prefix = "app")
  public AppProperty appProperty() {
    return new AppProperty();
  }

  @Bean
  @ConditionalOnProperty(value = "app.dual-version-config.enabled", havingValue = "true")
  AppProperty.DualVersionConfig dualVersionConfig(AppProperty appProperty) {
    return appProperty.getDualVersionConfig();
  }
}
