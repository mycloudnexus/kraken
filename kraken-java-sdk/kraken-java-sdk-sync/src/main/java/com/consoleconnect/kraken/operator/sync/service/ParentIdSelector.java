package com.consoleconnect.kraken.operator.sync.service;

import static com.consoleconnect.kraken.operator.core.enums.AssetKindEnum.PRODUCT;

import com.consoleconnect.kraken.operator.core.entity.UnifiedAssetEntity;
import com.consoleconnect.kraken.operator.core.repo.UnifiedAssetRepository;
import com.consoleconnect.kraken.operator.core.service.UnifiedAssetService;
import org.springframework.data.domain.Page;

public interface ParentIdSelector {

  UnifiedAssetRepository getUnifiedAssetRepository();

  default String parentIdFromProduct() {
    Page<UnifiedAssetEntity> assetEntities =
        getUnifiedAssetRepository()
            .search(null, PRODUCT.getKind(), null, UnifiedAssetService.getSearchPageRequest(0, 1));
    return assetEntities.getContent().isEmpty()
        ? null
        : assetEntities.getContent().get(0).getKey();
  }
}
