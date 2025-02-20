package com.consoleconnect.kraken.operator.sync.service;

import com.consoleconnect.kraken.operator.core.dto.UnifiedAssetDto;
import com.consoleconnect.kraken.operator.core.enums.AssetKindEnum;
import com.consoleconnect.kraken.operator.core.enums.ResourceLoaderTypeEnum;
import com.consoleconnect.kraken.operator.core.event.IngestDataEvent;
import com.consoleconnect.kraken.operator.core.ingestion.DataIngestionJob;
import com.consoleconnect.kraken.operator.core.repo.UnifiedAssetRepository;
import com.consoleconnect.kraken.operator.core.toolkit.JsonToolkit;
import com.consoleconnect.kraken.operator.sync.model.SyncProperty;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class SellerContactSyncHandler implements ClientSyncHandler, ParentIdSelector {

  private final SyncProperty syncProperty;
  @Getter private final UnifiedAssetRepository unifiedAssetRepository;
  private final DataIngestionJob dataIngestionJob;

  @Override
  public AssetKindEnum getKind() {
    return AssetKindEnum.COMPONENT_SELLER_CONTACT;
  }

  @Override
  public void handleAssets(List<UnifiedAssetDto> assets) {
    if (CollectionUtils.isEmpty(assets)) {
      log.warn("handle seller contacts, no assets returned");
      return;
    }
    handleSellerContacts(assets);
  }

  public void handleSellerContacts(List<UnifiedAssetDto> assets) {
    assets.forEach(
        assetDto -> {
          IngestDataEvent event = new IngestDataEvent();
          event.setParentId(parentIdFromProduct());
          event.setAsset(assetDto);
          event.setFullPath(
              ResourceLoaderTypeEnum.generatePath(
                  ResourceLoaderTypeEnum.RAW, JsonToolkit.toJson(assetDto)));
          event.setEnforceSync(syncProperty.isAssetConfigOverwriteFlag());
          event.setKind(AssetKindEnum.COMPONENT_SELLER_CONTACT);
          dataIngestionJob.ingestData(event);
        });
  }
}
