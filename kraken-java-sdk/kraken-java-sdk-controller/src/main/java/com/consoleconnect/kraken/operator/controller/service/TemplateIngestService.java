package com.consoleconnect.kraken.operator.controller.service;

import com.consoleconnect.kraken.operator.auth.service.UserService;
import com.consoleconnect.kraken.operator.controller.dto.UpgradeTuple;
import com.consoleconnect.kraken.operator.controller.model.MgmtProperty;
import com.consoleconnect.kraken.operator.controller.service.upgrade.UpgradeSourceServiceFactory;
import com.consoleconnect.kraken.operator.core.dto.UnifiedAssetDto;
import com.consoleconnect.kraken.operator.core.enums.AssetKindEnum;
import com.consoleconnect.kraken.operator.core.enums.UpgradeSourceEnum;
import com.consoleconnect.kraken.operator.core.event.PlatformSettingCompletedEvent;
import com.consoleconnect.kraken.operator.core.ingestion.DataIngestionJob;
import com.consoleconnect.kraken.operator.core.service.UnifiedAssetService;
import com.consoleconnect.kraken.operator.core.toolkit.Paging;
import java.util.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@AllArgsConstructor
public class TemplateIngestService {
  private final DataIngestionJob dataIngestionJob;
  private final ApplicationEventPublisher eventPublisher;
  private final UnifiedAssetService unifiedAssetService;
  private final MgmtProperty mgmtProperty;
  private final UserService userService;
  private final UpgradeSourceServiceFactory upgradeSourceServiceFactory;
  private final TemplateUpgradeService templateUpgradeService;

  @EventListener(ApplicationReadyEvent.class)
  public void onApplicationReady(Object event) {
    userService.initSystemUpgradeUser();
    log.info("Platform Boot Up Event Received, event class:{}", event.getClass());
    Paging<UnifiedAssetDto> assetDtoPaging =
        unifiedAssetService.search(
            null, AssetKindEnum.PRODUCT.getKind(), false, null, PageRequest.of(0, 1));
    if (mgmtProperty.isMgmtServerEnabled()) {
      // need not init product&workplace
      return;
    }
    if (CollectionUtils.isEmpty(assetDtoPaging.getData())) {
      log.info("Platform Boot Up Event Received, startup firstly,initialize templates ");
      dataIngestionJob.ingestionWorkspace();
      eventPublisher.publishEvent(new PlatformSettingCompletedEvent());
    } else {
      log.info(
          "Platform Boot Up Event Received, had initialized the templates and ignored for this time  ");
      // upgrading
      List<UpgradeTuple> upgradeTuples =
          upgradeSourceServiceFactory
              .getUpgradeSourceService(UpgradeSourceEnum.CLASSPATH)
              .getTemplateUpgradeRecords(null);
      // direct save
      // template upgrade record
      upgradeTuples.forEach(
          upgradeTuple ->
              upgradeTuple
                  .directSaves()
                  .forEach(
                      directSaved ->
                          templateUpgradeService.ingestData(
                              upgradeTuple.productKey(), directSaved, false)));
    }
    eventPublisher.publishEvent(new PlatformSettingCompletedEvent());
    log.info("Platform Boot Up Event Completed");
  }
}
