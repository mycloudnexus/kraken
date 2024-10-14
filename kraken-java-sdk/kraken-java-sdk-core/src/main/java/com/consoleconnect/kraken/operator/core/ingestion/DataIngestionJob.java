package com.consoleconnect.kraken.operator.core.ingestion;

import com.consoleconnect.kraken.operator.core.dto.UnifiedAssetDto;
import com.consoleconnect.kraken.operator.core.event.IngestDataEvent;
import com.consoleconnect.kraken.operator.core.event.IngestionDataResult;
import com.consoleconnect.kraken.operator.core.model.*;
import com.consoleconnect.kraken.operator.core.service.UnifiedAssetService;
import com.consoleconnect.kraken.operator.core.toolkit.DateTime;
import com.consoleconnect.kraken.operator.core.toolkit.YamlToolkit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@AllArgsConstructor
@Slf4j
@Service
public class DataIngestionJob {
  private final UnifiedAssetService assetService;
  @Getter private final AppProperty appProperty;
  private final List<AbstractAssetEventListener> assetEventListeners;

  private final ResourceLoaderFactory resourceLoaderFactory;

  @Transactional
  public IngestionDataResult ingestionWorkspace() {
    log.info("ingestionWorkspace,tenant:{}", appProperty.getTenant());
    String fullPath = appProperty.getTenant().getWorkspacePath();
    if (fullPath == null) {
      log.warn("Cannot find workspace path from tenant");
      return IngestionDataResult.of(HttpStatus.BAD_REQUEST.value(), "Cannot find workspace path");
    }
    Optional<FileContentDescriptor> workspaceYamlContent = readFile(fullPath);
    if (workspaceYamlContent.isEmpty()) {
      log.warn("Cannot read workspace yaml content from {}", fullPath);
      return IngestionDataResult.of(
          HttpStatus.BAD_REQUEST.value(), "Cannot read workspace yaml content");
    }
    Optional<UnifiedAsset> assetOptional =
        YamlToolkit.parseYaml(workspaceYamlContent.get().getContent(), UnifiedAsset.class);
    if (assetOptional.isEmpty()) {
      log.warn("Cannot parse workspace yaml content from {}", fullPath);
      return IngestionDataResult.of(
          HttpStatus.BAD_REQUEST.value(), "Cannot parse workspace yaml content");
    }
    UnifiedAsset asset = assetOptional.get();

    onPrePersist(new FileDescriptor(fullPath), asset);
    final IngestionDataResult syncAssetResult =
        assetService.syncAsset(
            null,
            asset,
            new SyncMetadata(
                fullPath, workspaceYamlContent.get().getSha(), DateTime.nowInUTCString()),
            false);
    asset.getMetadata().setId(syncAssetResult.getData().getId().toString());
    onPostPersist(null, new FileDescriptor(fullPath), asset);
    log.info("Ingesting data from {} completed", fullPath);
    return syncAssetResult;
  }

  @Transactional
  public IngestionDataResult ingestData(IngestDataEvent event) {
    log.info("Ingest data event received:{}", event);
    String parentId = event.getParentId();
    String fullPath = event.getFullPath();

    Optional<FileContentDescriptor> yamlContentOptional = resourceLoaderFactory.readFile(fullPath);
    if (yamlContentOptional.isEmpty()) {
      log.warn("Cannot read yaml content from {}", fullPath);
      return IngestionDataResult.of(
          HttpStatus.NOT_FOUND.value(), "Cannot read yaml content from " + fullPath);
    }

    Optional<UnifiedAsset> assetOptional =
        YamlToolkit.parseYaml(yamlContentOptional.get().getContent(), UnifiedAsset.class);

    if (assetOptional.isEmpty()) {
      log.warn("Cannot parse yaml/json content from {}", fullPath);
      return IngestionDataResult.of(
          HttpStatus.BAD_REQUEST.value(), "Cannot parse yaml/json content from " + fullPath);
    }
    UnifiedAsset asset = assetOptional.get();

    if (parentId == null && asset.getMetadata().getProductKey() != null) {
      parentId = asset.getMetadata().getProductKey();
    }
    if (event.isMergeLabels()) {
      UnifiedAssetDto db = assetService.findOne(asset.getMetadata().getKey());
      Map<String, String> labels = new HashMap<>();
      Optional.ofNullable(db.getMetadata().getLabels()).ifPresent(labels::putAll);
      Optional.ofNullable(asset.getMetadata().getLabels()).ifPresent(labels::putAll);
      if (MapUtils.isNotEmpty(labels)) {
        asset.getMetadata().setLabels(labels);
      }
    }
    onPrePersist(new FileDescriptor(fullPath), asset);
    IngestionDataResult syncAssetResult =
        assetService.syncAsset(
            parentId,
            asset,
            new SyncMetadata(
                fullPath,
                yamlContentOptional.get().getSha(),
                DateTime.nowInUTCString(),
                event.getUserId()),
            event.isEnforceSync());
    asset.getMetadata().setId(syncAssetResult.getData().getId().toString());
    onPostPersist(parentId, new FileDescriptor(fullPath), asset);
    if (syncAssetResult.getData() != null) {
      UnifiedAssetService.toAsset(syncAssetResult.getData(), true);
    }
    return syncAssetResult;
  }

  private void onPrePersist(FileDescriptor fileDescriptor, UnifiedAsset asset) {
    assetEventListeners.stream()
        .filter(it -> it.getKind().getKind().equalsIgnoreCase(asset.getKind()))
        .forEach(it -> it.onPrePersist(fileDescriptor, asset, this));
  }

  private void onPostPersist(String productId, FileDescriptor fileDescriptor, UnifiedAsset asset) {
    assetEventListeners.stream()
        .filter(it -> it.getKind().getKind().equalsIgnoreCase(asset.getKind()))
        .forEach(it -> it.onPostPersist(productId, fileDescriptor, asset, this));
  }

  public Optional<FileContentDescriptor> readFile(String fullPath) {
    return resourceLoaderFactory.readFile(fullPath);
  }
}
