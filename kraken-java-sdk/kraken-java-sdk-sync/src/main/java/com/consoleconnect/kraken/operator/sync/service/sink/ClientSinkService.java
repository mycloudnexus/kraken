package com.consoleconnect.kraken.operator.sync.service.sink;

import com.consoleconnect.kraken.operator.core.dto.UnifiedAssetDto;
import com.consoleconnect.kraken.operator.core.enums.AssetKindEnum;
import java.util.List;

public interface ClientSinkService {
  AssetKindEnum getKind();

  void handleLatest(List<UnifiedAssetDto> latestAssets);
}
