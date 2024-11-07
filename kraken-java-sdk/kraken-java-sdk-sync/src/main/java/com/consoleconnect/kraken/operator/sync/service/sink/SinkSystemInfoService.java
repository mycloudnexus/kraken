package com.consoleconnect.kraken.operator.sync.service.sink;

import com.consoleconnect.kraken.operator.core.dto.UnifiedAssetDto;
import com.consoleconnect.kraken.operator.core.enums.AssetKindEnum;
import com.consoleconnect.kraken.operator.core.enums.ResourceLoaderTypeEnum;
import com.consoleconnect.kraken.operator.core.event.IngestDataEvent;
import com.consoleconnect.kraken.operator.core.ingestion.DataIngestionJob;
import com.consoleconnect.kraken.operator.core.service.UnifiedAssetService;
import com.consoleconnect.kraken.operator.core.toolkit.JsonToolkit;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class SinkSystemInfoService implements ClientSinkService {
  private final UnifiedAssetService unifiedAssetService;
  private final DataIngestionJob dataIngestionJob;

  @Override
  public AssetKindEnum getKind() {
    return AssetKindEnum.SYSTEM_INFO;
  }

  @Override
  public void handleLatest(List<UnifiedAssetDto> latestAssets) {
    latestAssets.forEach(this::ingestSystemInfo);
  }

  private void ingestSystemInfo(UnifiedAssetDto assetDto) {
    List<UnifiedAssetDto> products =
        unifiedAssetService.findByKind(AssetKindEnum.PRODUCT.getKind());
    if (CollectionUtils.isEmpty(products)) {
      return;
    }
    String productId = products.get(0).getId();
    IngestDataEvent event =
        new IngestDataEvent(
            productId, ResourceLoaderTypeEnum.RAW.getKind() + JsonToolkit.toJson(assetDto));
    event.setMergeLabels(true);
    event.setEnforceSync(true);
    dataIngestionJob.ingestData(event);
  }
}
