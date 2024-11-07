package com.consoleconnect.kraken.operator.core.service;

import com.consoleconnect.kraken.operator.core.dto.UnifiedAssetDto;
import com.consoleconnect.kraken.operator.core.enums.AssetKindEnum;
import com.consoleconnect.kraken.operator.core.model.UnifiedAsset;
import com.consoleconnect.kraken.operator.core.model.facet.CompatibilityFacets;
import com.consoleconnect.kraken.operator.core.toolkit.Constants;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CompatibilityCheckService {
  private final UnifiedAssetService unifiedAssetService;

  public boolean check(String appVersion, String productVersion) {
    if (StringUtils.isBlank(appVersion) || StringUtils.isBlank(productVersion)) {
      return true;
    }
    // read compatible asset from db
    List<UnifiedAssetDto> assetDtos =
        unifiedAssetService.findByKind(AssetKindEnum.PRODUCT_COMPATIBILITY.getKind());
    if (CollectionUtils.isEmpty(assetDtos)) {
      return true;
    }
    CompatibilityFacets facets =
        UnifiedAsset.getFacets(assetDtos.get(0), CompatibilityFacets.class);
    Map<String, List<String>> compatibility = facets.getCompatibility();
    List<String> productVersions = compatibility.get(Constants.formatVersion(appVersion));
    String formattedVersion = Constants.formatVersion(productVersion);
    return productVersions.stream()
        .anyMatch(version -> CompatibilityCheckService.compareVersion(version, formattedVersion));
  }

  private static boolean compareVersion(String compatibleVersion, String targetVersion) {
    if (compatibleVersion.equals(targetVersion)) {
      return true;
    }
    if (compatibleVersion.contains(Constants.DOT)) {
      String childVersion =
          compatibleVersion.substring(compatibleVersion.lastIndexOf(Constants.DOT) + 1);
      if (!childVersion.equals(Constants.X)) {
        return false;
      }
      String newCompatibleVersion =
          compatibleVersion.substring(0, compatibleVersion.lastIndexOf(Constants.DOT));
      String newTargetVersion =
          targetVersion.substring(0, targetVersion.lastIndexOf(Constants.DOT));
      return compareVersion(newCompatibleVersion, newTargetVersion);
    }
    return false;
  }
}
