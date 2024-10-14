package com.consoleconnect.kraken.operator.controller.service;

import com.consoleconnect.kraken.operator.auth.dto.CreateUserRequest;
import com.consoleconnect.kraken.operator.auth.enums.UserRoleEnum;
import com.consoleconnect.kraken.operator.auth.model.User;
import com.consoleconnect.kraken.operator.auth.security.UserContext;
import com.consoleconnect.kraken.operator.auth.service.UserService;
import com.consoleconnect.kraken.operator.controller.dto.UpgradeRecord;
import com.consoleconnect.kraken.operator.controller.event.TemplateSynCompletedEvent;
import com.consoleconnect.kraken.operator.controller.event.TemplateUpgradeEvent;
import com.consoleconnect.kraken.operator.controller.listener.TemplateSynCompletedListener;
import com.consoleconnect.kraken.operator.controller.model.MgmtProperty;
import com.consoleconnect.kraken.operator.core.dto.UnifiedAssetDto;
import com.consoleconnect.kraken.operator.core.entity.UnifiedAssetEntity;
import com.consoleconnect.kraken.operator.core.enums.AssetKindEnum;
import com.consoleconnect.kraken.operator.core.event.IngestDataEvent;
import com.consoleconnect.kraken.operator.core.event.IngestionDataResult;
import com.consoleconnect.kraken.operator.core.event.PlatformSettingCompletedEvent;
import com.consoleconnect.kraken.operator.core.exception.KrakenException;
import com.consoleconnect.kraken.operator.core.ingestion.DataIngestionJob;
import com.consoleconnect.kraken.operator.core.ingestion.ResourceLoaderFactory;
import com.consoleconnect.kraken.operator.core.model.AppProperty;
import com.consoleconnect.kraken.operator.core.model.FileContentDescriptor;
import com.consoleconnect.kraken.operator.core.model.UnifiedAsset;
import com.consoleconnect.kraken.operator.core.model.facet.ProductFacets;
import com.consoleconnect.kraken.operator.core.model.facet.WorkspaceFacets;
import com.consoleconnect.kraken.operator.core.repo.UnifiedAssetRepository;
import com.consoleconnect.kraken.operator.core.service.UnifiedAssetService;
import com.consoleconnect.kraken.operator.core.toolkit.Paging;
import com.consoleconnect.kraken.operator.core.toolkit.YamlToolkit;
import java.util.*;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@AllArgsConstructor
public class TemplateIngestService {
  private final DataIngestionJob dataIngestionJob;
  private final ApplicationEventPublisher eventPublisher;
  private final UnifiedAssetService unifiedAssetService;
  private final AppProperty appProperty;
  private final MgmtProperty mgmtProperty;
  private final ResourceLoaderFactory resourceLoaderFactory;
  private final UnifiedAssetRepository unifiedAssetRepository;
  private final TemplateUpgradeService templateUpgradeService;
  private final TemplateSynCompletedListener templateSynCompletedListener;
  private final UserService userService;

  @EventListener(ApplicationReadyEvent.class)
  public void onApplicationReady(Object event) {
    initSystemUpgrade();
    log.info("Platform Boot Up Event Received, event class:{}", event.getClass());
    Paging<UnifiedAssetDto> assetDtoPaging =
        unifiedAssetService.search(
            null, AssetKindEnum.PRODUCT.getKind(), false, null, PageRequest.of(0, 1));
    if (CollectionUtils.isEmpty(assetDtoPaging.getData())) {
      log.info("Platform Boot Up Event Received, startup firstly,initialize templates ");
      dataIngestionJob.ingestionWorkspace();
      eventPublisher.publishEvent(new PlatformSettingCompletedEvent());
    } else {
      log.info(
          "Platform Boot Up Event Received, had initialized the templates and ignored for this time  ");
      List<UpgradeTuple> upgradeTuples = getTemplateUpgradeRecords();
      // direct save
      upgradeTuples.forEach(
          upgradeTuple ->
              upgradeTuple.directSaves.forEach(
                  directSaved -> ingestData(upgradeTuple.productKey, directSaved, false)));
    }
    log.info("Platform Boot Up Event Completed");
  }

  public void initSystemUpgrade() {
    Paging<User> userPaging =
        userService.search(UserContext.SYSTEM.toLowerCase(), PageRequest.of(0, 1));
    if (userPaging.getTotal() == 0) {
      CreateUserRequest request = new CreateUserRequest();
      request.setEmail(UserContext.SYSTEM);
      request.setRole(UserRoleEnum.USER.name());
      request.setName(UserContext.SYSTEM);
      request.setPassword(UUID.randomUUID().toString());
      userService.create(request, UserContext.SYSTEM);
    }
  }

  @EventListener(TemplateUpgradeEvent.class)
  @Transactional
  public String triggerManualTemplateUpgrade(TemplateUpgradeEvent event) {
    log.info("Template upgrade  Event Received, event class:{}", event.getClass());
    List<UpgradeTuple> upgradeTuples = getTemplateUpgradeRecords();
    UnifiedAssetDto productAsset = templateUpgradeService.getProductAsset();
    for (UpgradeTuple upgradeTuple : upgradeTuples) {
      // compare from version
      upgradeTuple.versionChangedTemplates.forEach(
          versionRecord -> ingestData(productAsset.getMetadata().getKey(), versionRecord, false));

      // config from template upgrade path
      upgradeTuple.enforceUpgradeTemplates.forEach(
          upgradeRecord -> ingestData(productAsset.getMetadata().getKey(), upgradeRecord, true));
      //
      TemplateSynCompletedEvent templateSynCompletedEvent = new TemplateSynCompletedEvent();
      HashSet<UpgradeRecord> upgradeRecords = new HashSet<>(upgradeTuple.versionChangedTemplates);
      upgradeRecords.addAll(upgradeTuple.enforceUpgradeTemplates);
      Collection<UpgradeRecord> values =
          upgradeRecords.stream()
              .collect(
                  Collectors.groupingBy(
                      t -> t.key(),
                      Collectors.collectingAndThen(Collectors.toList(), list -> list.get(0))))
              .values();
      List<UpgradeRecord> finalList = new ArrayList<>(values);
      templateSynCompletedEvent.setTemplateUpgradeRecords(finalList);
      templateSynCompletedEvent.setEnvId(event.getEnvId());
      templateSynCompletedEvent.setTemplateUpgradeId(event.getTemplateUpgradeId());
      templateSynCompletedEvent.setUserId(event.getUserId());
      IngestionDataResult ingestionDataResult =
          templateSynCompletedListener.listenPlatformSettingCompletedEvent(
              templateSynCompletedEvent);
      String deploymentId = ingestionDataResult.getData().getId().toString();
      if (StringUtils.isNotBlank(deploymentId)) {
        return deploymentId;
      }
    }
    log.info("Template upgrade  completed");
    throw KrakenException.internalError("Template upgrade error:Unknown reason");
  }

  private List<UpgradeTuple> getTemplateUpgradeRecords() {
    String fullPath = appProperty.getTenant().getWorkspacePath();
    UnifiedAsset workplace = readFromPath(fullPath);
    WorkspaceFacets workspaceFacets = UnifiedAsset.getFacets(workplace, WorkspaceFacets.class);
    // product
    return workspaceFacets.getProductPaths().stream()
        .map(this::genProductUpgradeRecord)
        .filter(Objects::nonNull)
        .toList();
  }

  private UpgradeTuple genProductUpgradeRecord(String productPath) {
    UnifiedAsset productAsset = readFromPath(productPath);
    ProductFacets productFacets = UnifiedAsset.getFacets(productAsset, ProductFacets.class);
    // component
    List<String> componentPaths = productFacets.getComponentPaths();
    if (CollectionUtils.isEmpty(componentPaths)) {
      return null;
    }
    List<UpgradeRecord> templateUpgradeRecords =
        componentPaths.stream()
            .filter(path -> !appProperty.getInitializeExcludeAssets().contains(path))
            .map(
                componentPath -> {
                  UnifiedAsset unifiedAsset = readFromPath(componentPath);
                  if (unifiedAsset == null) {
                    return null;
                  }
                  return new UpgradeRecord(
                      unifiedAsset.getMetadata().getKey(),
                      unifiedAsset.getKind(),
                      unifiedAsset.getMetadata().getVersion(),
                      componentPath);
                })
            .filter(Objects::nonNull)
            .toList();

    List<String> componentKeys = templateUpgradeRecords.stream().map(UpgradeRecord::key).toList();
    Map<String, UnifiedAssetEntity> dbComponents =
        unifiedAssetRepository.findAllByKeyIn(componentKeys).stream()
            .collect(Collectors.toMap(UnifiedAssetEntity::getKey, t -> t));
    List<String> distributeKinds = mgmtProperty.getTemplateUpgrade().getDistributeKinds();
    List<UpgradeRecord> newVersionComponents = new ArrayList<>();
    List<UpgradeRecord> directSaves = new ArrayList<>();
    for (UpgradeRecord templateUpgradeRecord : templateUpgradeRecords) {
      if (!distributeKinds.contains(templateUpgradeRecord.kind())) {
        directSaves.add(templateUpgradeRecord);
      } else {
        if (!dbComponents.containsKey(templateUpgradeRecord.key())
            || templateUpgradeRecord.version()
                > dbComponents.get(templateUpgradeRecord.key()).getVersion()) {
          newVersionComponents.add(templateUpgradeRecord);
        }
      }
    }
    List<String> enforceUpgrades =
        productFacets.getTemplateUpgradePaths() == null
            ? List.of()
            : productFacets.getTemplateUpgradePaths();
    List<UpgradeRecord> upgradeComponents =
        enforceUpgrades.stream()
            .map(
                upgradePath -> {
                  UnifiedAsset unifiedAsset = readFromPath(upgradePath);
                  if (unifiedAsset == null) {
                    return null;
                  }
                  return new UpgradeRecord(
                      unifiedAsset.getMetadata().getKey(),
                      unifiedAsset.getKind(),
                      unifiedAsset.getMetadata().getVersion(),
                      upgradePath);
                })
            .filter(Objects::nonNull)
            .toList();

    return new UpgradeTuple(
        newVersionComponents, upgradeComponents, directSaves, productAsset.getMetadata().getKey());
  }

  protected UnifiedAsset readFromPath(String fullPath) {
    Optional<FileContentDescriptor> fileContentDescriptor =
        resourceLoaderFactory.readFile(fullPath);
    if (fileContentDescriptor.isEmpty()) {
      log.warn("Unified Asset Not Found,Path: {}", fullPath);
      return null;
    }
    return YamlToolkit.parseYaml(fileContentDescriptor.get().getContent(), UnifiedAsset.class)
        .orElse(null);
  }

  protected void ingestData(String parentKey, UpgradeRecord upgradeRecord, boolean enforce) {
    boolean mergeLabels =
        mgmtProperty.getTemplateUpgrade().getMergeLabelKinds().contains(upgradeRecord.kind());
    IngestDataEvent event =
        new IngestDataEvent(
            parentKey,
            upgradeRecord.fullPath(),
            mergeLabels,
            templateUpgradeService.getSystemUpgradeUser());
    event.setEnforceSync(enforce);
    dataIngestionJob.ingestData(event);
  }

  public record UpgradeTuple(
      List<UpgradeRecord> versionChangedTemplates,
      List<UpgradeRecord> enforceUpgradeTemplates,
      List<UpgradeRecord> directSaves,
      String productKey) {}
}
