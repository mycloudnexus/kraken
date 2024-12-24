package com.consoleconnect.kraken.operator.sync.service;

import com.consoleconnect.kraken.operator.core.dto.UnifiedAssetDto;
import com.consoleconnect.kraken.operator.core.enums.AssetKindEnum;
import com.consoleconnect.kraken.operator.core.ingestion.DataIngestionJob;
import com.consoleconnect.kraken.operator.core.repo.UnifiedAssetRepository;
import com.consoleconnect.kraken.operator.sync.model.SyncProperty;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class BuyerSyncService implements ClientSyncHandler {

  private final SyncProperty appProperty;
  private final UnifiedAssetRepository unifiedAssetRepository;
  private final DataIngestionJob dataIngestionJob;

  @Override
  public AssetKindEnum getKind() {
    return AssetKindEnum.PRODUCT_BUYER;
  }

  @Override
  public void handleLatest(List<UnifiedAssetDto> latestAssets) {}
}
