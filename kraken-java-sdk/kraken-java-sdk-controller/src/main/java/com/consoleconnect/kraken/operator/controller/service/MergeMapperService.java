package com.consoleconnect.kraken.operator.controller.service;

import com.consoleconnect.kraken.operator.core.entity.UnifiedAssetEntity;
import com.consoleconnect.kraken.operator.core.service.MergeService;
import com.consoleconnect.kraken.operator.core.service.UnifiedAssetService;
import java.util.Map;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Service("mergeMapperData")
@Primary
public class MergeMapperService implements MergeService {
  @Override
  public Map<String, Object> mergeFacets(
      UnifiedAssetEntity unifiedAssetEntity, Map<String, Object> facetsUpdated) {
    return UnifiedAssetService.mergeFacets(unifiedAssetEntity, facetsUpdated);
  }
}
