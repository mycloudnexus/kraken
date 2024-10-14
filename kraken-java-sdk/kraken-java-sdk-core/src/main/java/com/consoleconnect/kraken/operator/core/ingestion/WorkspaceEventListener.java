package com.consoleconnect.kraken.operator.core.ingestion;

import com.consoleconnect.kraken.operator.core.enums.AssetKindEnum;
import com.consoleconnect.kraken.operator.core.event.IngestDataEvent;
import com.consoleconnect.kraken.operator.core.model.AppProperty;
import com.consoleconnect.kraken.operator.core.model.FileDescriptor;
import com.consoleconnect.kraken.operator.core.model.UnifiedAsset;
import com.consoleconnect.kraken.operator.core.model.facet.WorkspaceFacets;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class WorkspaceEventListener extends AbstractAssetEventListener {

  public WorkspaceEventListener(ApplicationEventPublisher eventPublisher, AppProperty appProperty) {
    super(eventPublisher, appProperty);
  }

  public AssetKindEnum getKind() {
    return AssetKindEnum.WORKSPACE;
  }

  @Override
  public void onPostPersist(
      String productId, FileDescriptor fileDescriptor, UnifiedAsset asset, DataIngestionJob job) {
    log.info("onPostPersist,assetKey:{}", asset.getMetadata().getKey());
    WorkspaceFacets facets = UnifiedAsset.getFacets(asset, WorkspaceFacets.class);
    if (facets == null) {
      log.warn("No facets found in workspace asset: {}", asset.getMetadata().getKey());
      return;
    }

    // reload products
    if (facets.getProductPaths() != null) {
      for (String productPath : facets.getProductPaths()) {
        job.ingestData(new IngestDataEvent(asset.getMetadata().getId(), productPath));
      }
    }
    log.info("onPostPersist,assetKey:{} completed", asset.getMetadata().getKey());
  }
}
