package com.consoleconnect.kraken.operator.gateway.service;

import com.consoleconnect.kraken.operator.core.dto.UnifiedAssetDto;
import com.consoleconnect.kraken.operator.core.enums.AssetKindEnum;
import com.consoleconnect.kraken.operator.core.event.PlatformSettingCompletedEvent;
import com.consoleconnect.kraken.operator.core.model.UnifiedAsset;
import com.consoleconnect.kraken.operator.core.model.facet.SystemInfoFacets;
import com.consoleconnect.kraken.operator.core.service.CompatibilityCheckService;
import com.consoleconnect.kraken.operator.core.service.UnifiedAssetService;
import com.consoleconnect.kraken.operator.data.repo.AssetReleaseRepository;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class KrakenReleaseMonitor {
  private final AssetReleaseRepository assetReleaseRepository;
  private final ApplicationEventPublisher applicationEventPublisher;
  private final UnifiedAssetService unifiedAssetService;
  private final CompatibilityCheckService compatibilityCheckService;
  @Getter private String currentProductReleaseId;

  @Value("${spring.build.version}")
  private String buildVersion;

  public KrakenReleaseMonitor(
      AssetReleaseRepository assetReleaseRepository,
      UnifiedAssetService unifiedAssetService,
      CompatibilityCheckService compatibilityCheckService,
      ApplicationEventPublisher applicationEventPublisher) {
    this.assetReleaseRepository = assetReleaseRepository;
    this.applicationEventPublisher = applicationEventPublisher;
    this.unifiedAssetService = unifiedAssetService;
    this.compatibilityCheckService = compatibilityCheckService;
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
    AtomicReference<String> productVersion = new AtomicReference<>();
    List<UnifiedAssetDto> systemDtos =
        unifiedAssetService.findByKind(AssetKindEnum.SYSTEM_INFO.getKind());
    if (CollectionUtils.isNotEmpty(systemDtos)) {
      UnifiedAssetDto unifiedAssetDto = systemDtos.get(0);
      SystemInfoFacets facets = UnifiedAsset.getFacets(unifiedAssetDto, SystemInfoFacets.class);
      if (SystemInfoFacets.SystemStatus.UPGRADING.equals(facets.getStatus())) {
        log.info("The application is being upgraded, task is skipped.");
        return;
      }
      if (!compatibilityCheckService.check(buildVersion, facets.getProductVersion())) {
        log.info(
            "current app version {}, product version {} is not compatible, task is skipped.",
            buildVersion,
            productVersion.get());
        return;
      }
    }
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
