package com.consoleconnect.kraken.operator.core.service;

import com.consoleconnect.kraken.operator.core.event.IngestDataEvent;
import com.consoleconnect.kraken.operator.core.event.IngestionDataResult;
import com.consoleconnect.kraken.operator.core.event.PlatformSettingChangedEvent;
import com.consoleconnect.kraken.operator.core.event.PlatformSettingCompletedEvent;
import com.consoleconnect.kraken.operator.core.ingestion.DataIngestionJob;
import java.util.concurrent.CompletableFuture;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
@Slf4j
public class DataIngestionService {
  private final DataIngestionJob dataIngestionJob;
  private final ApplicationEventPublisher eventPublisher;

  @EventListener(PlatformSettingChangedEvent.class)
  public void onPlatformSettingChanged(PlatformSettingChangedEvent event) {
    log.info("Platform Setting Changed Event Received, event class:{}", event.getClass());
    dataIngestionJob.ingestionWorkspace();
    eventPublisher.publishEvent(new PlatformSettingCompletedEvent());
    log.info("Platform Setting Changed Event Completed");
  }

  @Async
  @EventListener(IngestDataEvent.class)
  public CompletableFuture<IngestionDataResult> ingestData(IngestDataEvent event) {
    log.info("Ingest Data Event Received, event class:{}", event.getClass());
    IngestionDataResult result = dataIngestionJob.ingestData(event);
    if (result.getCode() == HttpStatus.OK.value()) {
      eventPublisher.publishEvent(new PlatformSettingCompletedEvent());
    }
    return CompletableFuture.completedFuture(result);
  }
}
