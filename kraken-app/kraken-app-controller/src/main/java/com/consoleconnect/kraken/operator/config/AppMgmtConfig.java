package com.consoleconnect.kraken.operator.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppMgmtConfig {

  @Bean
  @ConfigurationProperties(prefix = "app")
  public AppMgmtProperty appMgmtProperty() {
    return new AppMgmtProperty();
  }

  @Bean
  @ConfigurationProperties(prefix = "app.demo")
  public AppDemoProperty appDemoProperty() {
    return new AppDemoProperty();
  }
}
