package com.consoleconnect.kraken.operator.sync.service;

import com.consoleconnect.kraken.operator.core.dto.UnifiedAssetDto;
import com.consoleconnect.kraken.operator.core.enums.AssetKindEnum;
import java.util.List;

public interface ClientSyncHandler {
  AssetKindEnum getKind();

  void handleAssets(List<UnifiedAssetDto> assets);
}
