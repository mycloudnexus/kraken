package com.consoleconnect.kraken.operator.core.service;

import com.consoleconnect.kraken.operator.core.dto.UnifiedAssetDto;
import com.consoleconnect.kraken.operator.core.enums.AssetKindEnum;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CompatibilityCheckService {
  private final UnifiedAssetService unifiedAssetService;

  public boolean check(String appVersion, String productVersion) {
    // read compatible asset from db
    List<UnifiedAssetDto> assetDtos =
        unifiedAssetService.findByKind(AssetKindEnum.PRODUCT_COMPATIBILITY.getKind());
    if (CollectionUtils.isEmpty(assetDtos)) {
      return true;
    }
    return StringUtils.isNoneEmpty(appVersion, productVersion);
    // check whether match
  }
}
