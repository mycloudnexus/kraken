package com.consoleconnect.kraken.operator.sync.service;

import static com.consoleconnect.kraken.operator.core.enums.AssetKindEnum.PRODUCT;
import static com.consoleconnect.kraken.operator.core.enums.AssetKindEnum.PRODUCT_BUYER;

import com.consoleconnect.kraken.operator.core.dto.UnifiedAssetDto;
import com.consoleconnect.kraken.operator.core.entity.UnifiedAssetEntity;
import com.consoleconnect.kraken.operator.core.enums.AssetKindEnum;
import com.consoleconnect.kraken.operator.core.enums.ResourceLoaderTypeEnum;
import com.consoleconnect.kraken.operator.core.event.IngestDataEvent;
import com.consoleconnect.kraken.operator.core.ingestion.DataIngestionJob;
import com.consoleconnect.kraken.operator.core.model.facet.BuyerOnboardFacets;
import com.consoleconnect.kraken.operator.core.repo.UnifiedAssetRepository;
import com.consoleconnect.kraken.operator.core.service.UnifiedAssetService;
import com.consoleconnect.kraken.operator.core.toolkit.JsonToolkit;
import com.consoleconnect.kraken.operator.sync.model.SyncProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class BuyerSyncHandler implements ClientSyncHandler {

  private final SyncProperty syncProperty;
  private final UnifiedAssetRepository unifiedAssetRepository;
  private final DataIngestionJob dataIngestionJob;

  @Override
  public AssetKindEnum getKind() {
    return AssetKindEnum.PRODUCT_BUYER;
  }

  @Override
  public void handleAssets(List<UnifiedAssetDto> assets) {
    handleBuyers(assets);
  }

  public void handleBuyers(List<UnifiedAssetDto> assetDtoList) {
    if (CollectionUtils.isEmpty(assetDtoList)) {
      log.warn("handleBuyers No assetDtoList returned");
      return;
    }
    for (UnifiedAssetDto dto : assetDtoList) {
      String envId = extractEnvId(dto);
      String buyerId = extractBuyerId(dto);
      if (StringUtils.isBlank(envId) || StringUtils.isBlank(buyerId)) {
        continue;
      }

      Optional<UnifiedAssetEntity> existingBuyer = findExistingBuyer(envId, buyerId);
      if (existingBuyer.isEmpty() || hasChanges(dto, existingBuyer.get())) {
        processBuyerUpdate(dto, parentIdFromProduct());
      } else {
        log.info("No need to handle buyer:{} in env:{}", buyerId, envId);
      }
    }
  }

  private boolean hasChanges(UnifiedAssetDto dto, UnifiedAssetEntity existingBuyer) {
    UnifiedAssetDto existingBuyerDto = UnifiedAssetService.toAsset(existingBuyer, true);
    BuyerOnboardFacets existingFacets =
        JsonToolkit.fromJson(
            JsonToolkit.toJson(existingBuyerDto.getFacets()),
            new TypeReference<BuyerOnboardFacets>() {});
    BuyerOnboardFacets currentFacets =
        JsonToolkit.fromJson(
            JsonToolkit.toJson(dto.getFacets()), new TypeReference<BuyerOnboardFacets>() {});
    return !existingBuyerDto.getMetadata().getStatus().equals(dto.getMetadata().getStatus())
        || !(Objects.nonNull(existingFacets.getBuyerInfo().getCompanyName())
            && existingFacets
                .getBuyerInfo()
                .getCompanyName()
                .equals(currentFacets.getBuyerInfo().getCompanyName()));
  }

  private Optional<UnifiedAssetEntity> findExistingBuyer(String envId, String buyerId) {
    Page<UnifiedAssetEntity> existedData =
        unifiedAssetRepository.findBuyers(
            null, PRODUCT_BUYER.getKind(), envId, buyerId, null, null, PageRequest.of(0, 1));
    return existedData.getContent().isEmpty()
        ? Optional.empty()
        : Optional.of(existedData.getContent().get(0));
  }

  private String parentIdFromProduct() {
    Page<UnifiedAssetEntity> assetEntities =
        unifiedAssetRepository.search(
            null, PRODUCT.getKind(), null, UnifiedAssetService.getSearchPageRequest(0, 1));
    return assetEntities.getContent().isEmpty()
        ? null
        : assetEntities.getContent().get(0).getParentId();
  }

  private void processBuyerUpdate(UnifiedAssetDto dto, String parentId) {
    // Handle deactivated case and duplicate scenario logic here based on requirements
    dto.setParentId(parentId);
    IngestDataEvent event = new IngestDataEvent();
    event.setParentId(parentId);
    event.setAsset(dto);
    event.setFullPath(
        ResourceLoaderTypeEnum.generatePath(ResourceLoaderTypeEnum.RAW, JsonToolkit.toJson(dto)));
    event.setEnforceSync(syncProperty.isAssetConfigOverwriteFlag());
    dataIngestionJob.ingestData(event);
  }

  private String extractEnvId(UnifiedAssetDto dto) {
    return dto.getMetadata().getLabels().get("envId");
  }

  private String extractBuyerId(UnifiedAssetDto dto) {
    return dto.getMetadata().getLabels().get("buyerId");
  }
}