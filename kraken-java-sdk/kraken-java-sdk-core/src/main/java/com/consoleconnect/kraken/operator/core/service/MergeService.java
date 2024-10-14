package com.consoleconnect.kraken.operator.core.service;

import com.consoleconnect.kraken.operator.core.entity.UnifiedAssetEntity;
import java.util.Map;

public interface MergeService {
  Map<String, Object> mergeFacets(
      UnifiedAssetEntity unifiedAssetEntity, Map<String, Object> facetsUpdated);
}
