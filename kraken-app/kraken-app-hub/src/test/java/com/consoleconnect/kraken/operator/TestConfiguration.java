package com.consoleconnect.kraken.operator;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.test.web.reactive.server.WebTestClient;

@Component
public class TestConfiguration implements ApplicationContextAware {
  ApplicationContext applicationContext;

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    this.applicationContext = applicationContext;
  }

  @Primary
  @Bean
  public WebTestClient getWebTestClient() {
    return WebTestClient.bindToApplicationContext(applicationContext).build();
  }
}
