package com.consoleconnect.kraken.operator.core.service;

import com.consoleconnect.kraken.operator.core.entity.UnifiedAssetEntity;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class MergeMapperService implements MergeService {
  @Override
  public Map<String, Object> mergeFacets(
      UnifiedAssetEntity unifiedAssetEntity, Map<String, Object> facetsUpdated) {
    return facetsUpdated;
  }
}
