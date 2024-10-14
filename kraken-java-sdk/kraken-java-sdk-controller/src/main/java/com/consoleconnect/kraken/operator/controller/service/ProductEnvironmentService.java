package com.consoleconnect.kraken.operator.controller.service;

import com.consoleconnect.kraken.operator.controller.dto.CreateEnvRequest;
import com.consoleconnect.kraken.operator.core.dto.UnifiedAssetDto;
import com.consoleconnect.kraken.operator.core.enums.AssetKindEnum;
import com.consoleconnect.kraken.operator.core.event.IngestionDataResult;
import com.consoleconnect.kraken.operator.core.exception.KrakenException;
import com.consoleconnect.kraken.operator.core.model.SyncMetadata;
import com.consoleconnect.kraken.operator.core.model.UnifiedAsset;
import com.consoleconnect.kraken.operator.core.service.UnifiedAssetService;
import com.consoleconnect.kraken.operator.core.toolkit.DateTime;
import com.consoleconnect.kraken.operator.core.toolkit.Paging;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class ProductEnvironmentService {
  private final UnifiedAssetService unifiedAssetService;

  public Paging<UnifiedAssetDto> search(String productId, PageRequest pageable) {
    return unifiedAssetService.search(
        productId, AssetKindEnum.PRODUCT_ENV.getKind(), false, null, pageable);
  }

  @Transactional
  public UnifiedAssetDto create(String productId, CreateEnvRequest request, String createdBy) {

    UnifiedAssetDto product = unifiedAssetService.findOne(productId);
    String key = product.getMetadata().getKey() + ".env." + System.currentTimeMillis();
    UnifiedAsset unifiedAsset =
        UnifiedAsset.of(AssetKindEnum.PRODUCT_ENV.getKind(), key, request.getName());
    SyncMetadata syncMetadata = new SyncMetadata("", "", DateTime.nowInUTCString(), createdBy);
    IngestionDataResult result =
        unifiedAssetService.syncAsset(
            product.getMetadata().getKey(), unifiedAsset, syncMetadata, true);
    if (result.getCode() != 200) {
      throw new KrakenException(result.getCode(), result.getMessage());
    }
    return unifiedAssetService.findOne(result.getData().getId().toString());
  }
}
