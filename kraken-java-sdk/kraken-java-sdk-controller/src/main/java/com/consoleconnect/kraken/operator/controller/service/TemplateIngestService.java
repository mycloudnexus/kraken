package com.consoleconnect.kraken.operator.controller.service;

import com.consoleconnect.kraken.operator.auth.service.UserService;
import com.consoleconnect.kraken.operator.controller.dto.UpgradeTuple;
import com.consoleconnect.kraken.operator.controller.model.MgmtProperty;
import com.consoleconnect.kraken.operator.controller.model.SystemInfo;
import com.consoleconnect.kraken.operator.controller.service.upgrade.UpgradeSourceServiceFactory;
import com.consoleconnect.kraken.operator.core.dto.Tuple2;
import com.consoleconnect.kraken.operator.core.dto.UnifiedAssetDto;
import com.consoleconnect.kraken.operator.core.enums.AssetKindEnum;
import com.consoleconnect.kraken.operator.core.enums.EnvNameEnum;
import com.consoleconnect.kraken.operator.core.enums.UpgradeResultEventEnum;
import com.consoleconnect.kraken.operator.core.enums.UpgradeSourceEnum;
import com.consoleconnect.kraken.operator.core.event.PlatformSettingCompletedEvent;
import com.consoleconnect.kraken.operator.core.ingestion.DataIngestionJob;
import com.consoleconnect.kraken.operator.core.service.EventSinkService;
import com.consoleconnect.kraken.operator.core.service.UnifiedAssetService;
import com.consoleconnect.kraken.operator.core.toolkit.AssetsConstants;
import com.consoleconnect.kraken.operator.core.toolkit.Paging;
import java.time.ZonedDateTime;
import java.util.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class TemplateIngestService {
  private final DataIngestionJob dataIngestionJob;
  private final ApplicationEventPublisher eventPublisher;
  private final UnifiedAssetService unifiedAssetService;
  private final MgmtProperty mgmtProperty;
  private final UserService userService;
  private final UpgradeSourceServiceFactory upgradeSourceServiceFactory;
  private final TemplateUpgradeService templateUpgradeService;
  private final EventSinkService eventSinkService;
  private final SystemInfoService systemInfoService;

  @Value("${spring.build.version}")
  private String buildVersion;

  @EventListener(ApplicationReadyEvent.class)
  public void onApplicationReady(Object event) {
    // trigger  report kraken version upgrade
    reportKrakenVersionUpgrade();
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
      // trigger first installation report
      reportFirstInstallation();
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

  private void reportFirstInstallation() {
    Paging<UnifiedAssetDto> assetDtoPaging =
        unifiedAssetService.findBySpecification(
            Tuple2.ofList(
                AssetsConstants.FIELD_KIND, AssetKindEnum.PRODUCT_TEMPLATE_UPGRADE.getKind()),
            null,
            null,
            PageRequest.of(0, 1, Sort.Direction.ASC, AssetsConstants.FIELD_CREATE_AT),
            null);
    if (CollectionUtils.isEmpty(assetDtoPaging.getData())) {
      return;
    }
    UnifiedAssetDto unifiedAssetDto = assetDtoPaging.getData().get(0);
    // trigger first installation report
    eventSinkService.reportTemplateUpgradeResult(
        unifiedAssetDto,
        UpgradeResultEventEnum.INSTALLED,
        reportEvent -> {
          SystemInfo systemInfo = systemInfoService.find();
          reportEvent.setProductKey(systemInfo.getProductKey());
          reportEvent.setProductSpec(systemInfo.getProductSpec());
          reportEvent.setProductVersion(systemInfo.getControlProductVersion());
          reportEvent.setInstalledAt(ZonedDateTime.now());
        });
  }

  private void reportKrakenVersionUpgrade() {
    SystemInfo systemInfo = systemInfoService.find();
    if (!StringUtils.equalsIgnoreCase(buildVersion, systemInfo.getControlAppVersion())) {
      log.info(
          "Kraken version upgrade report: old version {},current version {}",
          systemInfo.getControlAppVersion(),
          buildVersion);
      eventSinkService.reportKrakenVersionUpgradeResult(
          EnvNameEnum.CONTROL_PLANE, buildVersion, ZonedDateTime.now());
    }
  }
}
