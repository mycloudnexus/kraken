package com.consoleconnect.kraken.operator.gateway.service;

import com.consoleconnect.kraken.operator.core.event.PlatformSettingCompletedEvent;
import com.consoleconnect.kraken.operator.data.repo.AssetReleaseRepository;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class KrakenReleaseMonitor {
  private final AssetReleaseRepository assetReleaseRepository;
  private final ApplicationEventPublisher applicationEventPublisher;
  @Getter private String currentProductReleaseId;

  public KrakenReleaseMonitor(
      AssetReleaseRepository assetReleaseRepository,
      ApplicationEventPublisher applicationEventPublisher) {
    this.assetReleaseRepository = assetReleaseRepository;
    this.applicationEventPublisher = applicationEventPublisher;
    assetReleaseRepository
        .findFirstByOrderByCreatedAtDesc()
        .ifPresent(
            deploymentProcessEntity ->
                currentProductReleaseId = deploymentProcessEntity.getVersion());

    log.info(
        "PlatformSettingMonitor initialized, current product release id: {}",
        currentProductReleaseId);
  }

  @Scheduled(cron = "${app.cron-job.check-release:-}")
  public void checkRelease() {
    log.info("Checking deployment process,current product release id: {}", currentProductReleaseId);
    assetReleaseRepository
        .findFirstByOrderByCreatedAtDesc()
        .ifPresent(
            deploymentProcessEntity -> {
              if (!deploymentProcessEntity.getVersion().equals(currentProductReleaseId)) {
                log.info("New release found: {}", deploymentProcessEntity.getVersion());
                applicationEventPublisher.publishEvent(new PlatformSettingCompletedEvent());
                currentProductReleaseId = deploymentProcessEntity.getVersion();
              }
            });
  }
}
