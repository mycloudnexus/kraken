package com.consoleconnect.kraken.operator.core;

import com.consoleconnect.kraken.operator.core.event.PlatformSettingCompletedEvent;
import com.consoleconnect.kraken.operator.core.ingestion.DataIngestionJob;
import com.consoleconnect.kraken.operator.core.model.AppProperty;
import com.consoleconnect.kraken.operator.core.service.UnifiedAssetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.EventListener;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.consoleconnect.kraken")
@EnableJpaRepositories(basePackages = "com.consoleconnect.kraken")
@EntityScan(basePackages = "com.consoleconnect.kraken")
public class CustomConfig implements ApplicationListener<WebServerInitializedEvent> {

  int serverPort;
  @Autowired AppProperty appProperty;
  @Autowired UnifiedAssetService unifiedAssetService;
  @Autowired DataIngestionJob dataIngestionJob;
  @Autowired ApplicationEventPublisher eventPublisher;

  @Override
  public void onApplicationEvent(WebServerInitializedEvent event) {
    serverPort = event.getWebServer().getPort();
  }

  @EventListener(ApplicationReadyEvent.class)
  public void onApplicationReady(Object event) {
    dataIngestionJob.ingestionWorkspace();
    eventPublisher.publishEvent(new PlatformSettingCompletedEvent());
  }
}
