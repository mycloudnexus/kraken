package com.consoleconnect.kraken.operator.sync.service;

import static com.consoleconnect.kraken.operator.core.enums.AssetKindEnum.PRODUCT;
import static com.consoleconnect.kraken.operator.core.enums.AssetKindEnum.PRODUCT_BUYER;
import static com.consoleconnect.kraken.operator.core.toolkit.AssetsConstants.FIELD_KIND;
import static com.consoleconnect.kraken.operator.core.toolkit.AssetsConstants.FIELD_UPDATED_AT;
import static com.consoleconnect.kraken.operator.core.toolkit.AssetsConstants.FIELD_UPDATE_AT_ORIGINAL;

import com.consoleconnect.kraken.operator.core.dto.UnifiedAssetDto;
import com.consoleconnect.kraken.operator.core.entity.UnifiedAssetEntity;
import com.consoleconnect.kraken.operator.core.enums.ResourceLoaderTypeEnum;
import com.consoleconnect.kraken.operator.core.event.IngestDataEvent;
import com.consoleconnect.kraken.operator.core.ingestion.DataIngestionJob;
import com.consoleconnect.kraken.operator.core.model.HttpResponse;
import com.consoleconnect.kraken.operator.core.model.facet.BuyerOnboardFacets;
import com.consoleconnect.kraken.operator.core.repo.UnifiedAssetRepository;
import com.consoleconnect.kraken.operator.core.service.UnifiedAssetService;
import com.consoleconnect.kraken.operator.core.toolkit.JsonToolkit;
import com.consoleconnect.kraken.operator.sync.model.SyncProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import java.net.URI;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpMethod;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;

@Slf4j
@Service
public class GeneralSyncService extends KrakenServerConnector {

  private final SyncProperty syncProperty;
  private final DataIngestionJob dataIngestionJob;
  private final UnifiedAssetRepository unifiedAssetRepository;
  private final ApplicationContext applicationContext;

  public GeneralSyncService(
      SyncProperty syncProperty,
      WebClient webClient,
      DataIngestionJob dataIngestionJob,
      UnifiedAssetRepository unifiedAssetRepository,
      ApplicationContext applicationContext) {
    super(syncProperty, webClient);
    this.syncProperty = syncProperty;
    this.dataIngestionJob = dataIngestionJob;
    this.unifiedAssetRepository = unifiedAssetRepository;
    this.applicationContext = applicationContext;
  }

  @Scheduled(cron = "${app.cron-job.pull-server-assets:-}")
  public void syncServerAssets() {
    // 1. Query from client db to determine the latest updated time
    final ZonedDateTime lastUpdatedAt = findLastUpdatedAt();

    // 2. Sending Http Get request to fetch the remote data sorted by updated_at in ascend order.

    Function<UriBuilder, URI> uriFunction = builderURIFunction(lastUpdatedAt);
    HttpResponse<Object> res =
        blockCurl(HttpMethod.GET, uriFunction, null, new ParameterizedTypeReference<>() {});

    if (res.getCode() == 200) {
      applicationContext.getBean(GeneralSyncService.class).ingestData(res, PRODUCT_BUYER.getKind());
    } else {
      log.error("Failed to sync assets from server");
    }
  }

  @Transactional
  public void ingestData(HttpResponse<Object> response, String kind) {
    Object data = response.getData();
    if (Objects.isNull(data)) {
      log.warn("No data returned");
      return;
    }
    // If data existed, iterating each of the items, query by buyerId and envId, do update or
    // create operation.
    if (PRODUCT_BUYER.getKind().equals(kind)) {
      handleBuyers(data);
    } else {
      log.warn("Not supported this kind asset:{}", kind);
    }
  }

  public void handleBuyers(Object data) {
    List<UnifiedAssetDto> receivedBuyers = extractUnifiedAssetDto(data);
    if (CollectionUtils.isEmpty(receivedBuyers)) {
      log.warn("handleBuyers No receivedBuyers returned");
      return;
    }
    for (UnifiedAssetDto dto : receivedBuyers) {
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

  private Function<UriBuilder, URI> builderURIFunction(ZonedDateTime lastUpdatedAt) {
    return uriBuilder ->
        uriBuilder
            .path(syncProperty.getControlPlane().getSyncFromServerEndpoint())
            .queryParam(FIELD_KIND, PRODUCT_BUYER.getKind())
            .queryParam(FIELD_UPDATED_AT, lastUpdatedAt)
            .build();
  }

  private ZonedDateTime findLastUpdatedAt() {
    Page<UnifiedAssetEntity> pages =
        unifiedAssetRepository.findBuyers(
            null,
            PRODUCT_BUYER.getKind(),
            null,
            null,
            null,
            null,
            PageRequest.of(0, 1, Sort.Direction.DESC, FIELD_UPDATE_AT_ORIGINAL));
    return (CollectionUtils.isNotEmpty(pages.getContent())
        ? pages.getContent().stream().map(UnifiedAssetEntity::getUpdatedAt).findFirst().orElse(null)
        : null);
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
    event.setEnforceSync(getAppProperty().isAssetConfigOverwriteFlag());
    dataIngestionJob.ingestData(event);
  }

  private List<UnifiedAssetDto> extractUnifiedAssetDto(Object data) {
    return JsonToolkit.fromJson(
        JsonToolkit.toJson(data), new TypeReference<List<UnifiedAssetDto>>() {});
  }

  private String extractEnvId(UnifiedAssetDto dto) {
    return dto.getMetadata().getLabels().get("envId");
  }

  private String extractBuyerId(UnifiedAssetDto dto) {
    return dto.getMetadata().getLabels().get("buyerId");
  }
}
