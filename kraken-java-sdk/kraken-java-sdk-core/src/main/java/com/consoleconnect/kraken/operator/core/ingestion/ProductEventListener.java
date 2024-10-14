package com.consoleconnect.kraken.operator.core.ingestion;

import com.consoleconnect.kraken.operator.core.enums.AssetKindEnum;
import com.consoleconnect.kraken.operator.core.event.IngestDataEvent;
import com.consoleconnect.kraken.operator.core.model.AppProperty;
import com.consoleconnect.kraken.operator.core.model.FileDescriptor;
import com.consoleconnect.kraken.operator.core.model.UnifiedAsset;
import com.consoleconnect.kraken.operator.core.model.facet.ProductFacets;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ProductEventListener extends AbstractAssetEventListener {

  public ProductEventListener(ApplicationEventPublisher eventPublisher, AppProperty appProperty) {
    super(eventPublisher, appProperty);
  }

  public AssetKindEnum getKind() {
    return AssetKindEnum.PRODUCT;
  }

  @Override
  public void onPostPersist(
      String productId, FileDescriptor fileDescriptor, UnifiedAsset asset, DataIngestionJob job) {
    log.info(
        "onPostPersist,  assetKind:{},assetKey:{}", asset.getKind(), asset.getMetadata().getKey());
    ProductFacets facets = UnifiedAsset.getFacets(asset, ProductFacets.class);

    List<String> initialFilterComponents = getAppProperty().getInitializeExcludeAssets();
    if (facets.getComponentPaths() != null) {
      for (String componentPath : facets.getComponentPaths()) {
        if (initialFilterComponents.contains(componentPath)) {
          log.info("onPostPersist,bypass componentPath:{}", componentPath);
          continue;
        }
        job.ingestData(new IngestDataEvent(asset.getMetadata().getId(), componentPath));
      }
    }
  }
}
