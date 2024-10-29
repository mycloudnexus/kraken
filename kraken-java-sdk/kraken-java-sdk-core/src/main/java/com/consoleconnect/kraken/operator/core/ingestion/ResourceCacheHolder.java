package com.consoleconnect.kraken.operator.core.ingestion;

import com.consoleconnect.kraken.operator.core.dto.Tuple2;
import com.consoleconnect.kraken.operator.core.dto.UnifiedAssetDto;
import com.consoleconnect.kraken.operator.core.enums.AssetKindEnum;
import com.consoleconnect.kraken.operator.core.exception.KrakenException;
import com.consoleconnect.kraken.operator.core.model.UnifiedAsset;
import com.consoleconnect.kraken.operator.core.model.facet.ProductReleaseDownloadFacets;
import com.consoleconnect.kraken.operator.core.service.UnifiedAssetService;
import com.consoleconnect.kraken.operator.core.toolkit.AssetsConstants;
import com.consoleconnect.kraken.operator.core.toolkit.Paging;
import com.consoleconnect.kraken.operator.core.toolkit.YamlToolkit;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ResourceCacheHolder {
  public static final String SPLIT_CHAR = "#";
  private final UnifiedAssetService unifiedAssetService;
  private static final Map<String, SoftReference<Map<String, String>>> CACHE = new HashMap<>();

  public String get(String key) {
    if (!key.contains(SPLIT_CHAR)) {
      throw KrakenException.badRequest(
          "Mgmt upgrade: invalid key: " + key + ",should include signal #");
    }
    String newKey = key.substring(0, key.indexOf(SPLIT_CHAR));
    String templateId = key.substring(key.indexOf(SPLIT_CHAR) + 1);
    SoftReference<Map<String, String>> contentMapRef = CACHE.get(templateId);
    if (contentMapRef == null || contentMapRef.get() == null) {
      contentMapRef = loadCache(templateId);
    }
    Map<String, String> contentMap = contentMapRef.get();
    return Optional.ofNullable(contentMap).map(t -> t.get(newKey)).orElse(null);
  }

  public SoftReference<Map<String, String>> loadCache(String templateUpgradeId) {
    Paging<UnifiedAssetDto> downloadAssetPaging =
        unifiedAssetService.findBySpecification(
            Tuple2.ofList(
                AssetsConstants.FIELD_PARENT_ID,
                templateUpgradeId,
                AssetsConstants.FIELD_KIND,
                AssetKindEnum.PRODUCT_RELEASE_DOWNLOAD.getKind()),
            null,
            null,
            null,
            null);
    UnifiedAssetDto downloadAsset = downloadAssetPaging.getData().get(0);
    ProductReleaseDownloadFacets facets =
        UnifiedAsset.getFacets(downloadAsset, ProductReleaseDownloadFacets.class);
    SoftReference<Map<String, String>> softReference = new SoftReference<>(facets.getContentMap());
    CACHE.put(templateUpgradeId, softReference);
    return softReference;
  }

  public String appendTemplateIdToFullPath(String templateId, String fullPath) {
    return fullPath + SPLIT_CHAR + templateId;
  }

  public void clearCache() {
    CACHE.clear();
  }

  public UnifiedAsset findSourceProduct(ProductReleaseDownloadFacets downloadFacets) {
    Map<String, String> contentMap = downloadFacets.getContentMap();
    for (Map.Entry<String, String> entry : contentMap.entrySet()) {
      Optional<UnifiedAsset> assetOptional =
          YamlToolkit.parseYaml(entry.getValue(), UnifiedAsset.class);
      if (assetOptional.isPresent()) {
        UnifiedAsset unifiedAsset = assetOptional.get();
        if (unifiedAsset.getKind().equalsIgnoreCase(AssetKindEnum.PRODUCT.getKind())) {
          return unifiedAsset;
        }
      }
    }
    throw KrakenException.internalError("Unable to find product yaml");
  }
}
