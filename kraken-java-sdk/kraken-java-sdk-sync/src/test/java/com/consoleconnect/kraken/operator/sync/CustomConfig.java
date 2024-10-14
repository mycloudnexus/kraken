package com.consoleconnect.kraken.operator.sync;

import com.consoleconnect.kraken.operator.core.model.AppProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.consoleconnect.kraken")
@EnableJpaRepositories(basePackages = "com.consoleconnect.kraken")
@EntityScan(basePackages = "com.consoleconnect.kraken")
public class CustomConfig implements ApplicationListener<WebServerInitializedEvent> {

  int serverPort;
  @Autowired AppProperty appProperty;

  @Override
  public void onApplicationEvent(WebServerInitializedEvent event) {
    serverPort = event.getWebServer().getPort();
  }
}
