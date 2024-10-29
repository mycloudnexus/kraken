package com.consoleconnect.kraken.operator.controller.service;

import com.consoleconnect.kraken.operator.core.dto.UnifiedAssetDto;
import com.consoleconnect.kraken.operator.core.enums.AssetKindEnum;
import com.consoleconnect.kraken.operator.core.enums.AssetStatusEnum;
import com.consoleconnect.kraken.operator.core.exception.KrakenException;
import com.consoleconnect.kraken.operator.core.model.SyncMetadata;
import com.consoleconnect.kraken.operator.core.service.UnifiedAssetService;
import com.consoleconnect.kraken.operator.core.toolkit.DateTime;
import org.springframework.util.Assert;

public abstract class AssetStatusManager {

  public abstract UnifiedAssetService getUnifiedAssetService();

  public UnifiedAssetDto activateAsset(
      String assetId, AssetKindEnum accepted, String checkMessage) {
    UnifiedAssetDto assetDto = getUnifiedAssetService().findOne(assetId);
    Assert.state(assetDto.getKind().equals(accepted.getKind()), checkMessage);
    if (AssetStatusEnum.ACTIVATED.getKind().equals(assetDto.getMetadata().getStatus())) {
      throw KrakenException.badRequest("The asset has been activated.");
    }
    assetDto.getMetadata().setStatus(AssetStatusEnum.ACTIVATED.getKind());
    return assetDto;
  }

  public UnifiedAssetDto deactivateAsset(
      String assetId, AssetKindEnum accepted, String checkMessage) {
    UnifiedAssetDto assetDto = getUnifiedAssetService().findOne(assetId);
    Assert.state(assetDto.getKind().equals(accepted.getKind()), checkMessage);
    if (!AssetStatusEnum.ACTIVATED.getKind().equals(assetDto.getMetadata().getStatus())) {
      throw KrakenException.badRequest("The asset is not activated.");
    }
    assetDto.getMetadata().setStatus(AssetStatusEnum.DEACTIVATED.getKind());
    return assetDto;
  }

  public void afterCompletion(UnifiedAssetDto assetDto, String createdBy) {
    SyncMetadata syncMetadata = new SyncMetadata("", "", DateTime.nowInUTCString(), createdBy);
    getUnifiedAssetService().syncAsset(assetDto.getParentId(), assetDto, syncMetadata, true);
  }
}
