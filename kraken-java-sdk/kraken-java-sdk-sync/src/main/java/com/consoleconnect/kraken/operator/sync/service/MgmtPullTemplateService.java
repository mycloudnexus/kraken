package com.consoleconnect.kraken.operator.sync.service;

import com.consoleconnect.kraken.operator.core.dto.Tuple2;
import com.consoleconnect.kraken.operator.core.dto.UnifiedAssetDto;
import com.consoleconnect.kraken.operator.core.enums.*;
import com.consoleconnect.kraken.operator.core.enums.DeployStatusEnum;
import com.consoleconnect.kraken.operator.core.enums.UpgradeSourceEnum;
import com.consoleconnect.kraken.operator.core.event.IngestDataEvent;
import com.consoleconnect.kraken.operator.core.event.IngestionDataResult;
import com.consoleconnect.kraken.operator.core.ingestion.DataIngestionJob;
import com.consoleconnect.kraken.operator.core.ingestion.ResourceCacheHolder;
import com.consoleconnect.kraken.operator.core.model.HttpResponse;
import com.consoleconnect.kraken.operator.core.model.SyncMetadata;
import com.consoleconnect.kraken.operator.core.model.UnifiedAsset;
import com.consoleconnect.kraken.operator.core.model.facet.ProductReleaseDownloadFacets;
import com.consoleconnect.kraken.operator.core.service.EventSinkService;
import com.consoleconnect.kraken.operator.core.service.UnifiedAssetService;
import com.consoleconnect.kraken.operator.core.toolkit.*;
import com.consoleconnect.kraken.operator.core.toolkit.AssetsConstants;
import com.consoleconnect.kraken.operator.core.toolkit.DateTime;
import com.consoleconnect.kraken.operator.core.toolkit.LabelConstants;
import com.consoleconnect.kraken.operator.sync.model.SyncProperty;
import com.consoleconnect.kraken.operator.sync.service.security.ExternalSystemTokenProvider;
import java.time.ZonedDateTime;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@Slf4j
public class MgmtPullTemplateService extends KrakenServerConnector {
  private final UnifiedAssetService unifiedAssetService;
  private final SyncProperty appProperty;
  private final ResourceCacheHolder resourceCacheHolder;
  private final DataIngestionJob dataIngestionJob;
  private final EventSinkService eventSinkService;

  public MgmtPullTemplateService(
      SyncProperty appProperty,
      WebClient webClient,
      ExternalSystemTokenProvider externalSystemTokenProvider,
      UnifiedAssetService unifiedAssetService,
      ResourceCacheHolder resourceCacheHolder,
      EventSinkService eventSinkService,
      DataIngestionJob dataIngestionJob) {
    super(appProperty, webClient, externalSystemTokenProvider);
    this.unifiedAssetService = unifiedAssetService;
    this.appProperty = appProperty;
    this.resourceCacheHolder = resourceCacheHolder;
    this.dataIngestionJob = dataIngestionJob;
    this.eventSinkService = eventSinkService;
  }

  @SchedulerLock(
      name = "pullMappingTemplateDetailsLock",
      lockAtMostFor = "${app.cron-job.lock.at-most-for}",
      lockAtLeastFor = "${app.cron-job.lock.at-least-for}")
  @Scheduled(cron = "${app.cron-job.download-mapping-template-content-from-mgmt:-}")
  public void pullMappingTemplateDetails() {
    Paging<UnifiedAssetDto> assetDtoPaging =
        unifiedAssetService.findBySpecification(
            Tuple2.ofList(
                AssetsConstants.FIELD_KIND,
                AssetKindEnum.PRODUCT_TEMPLATE_UPGRADE.getKind(),
                AssetsConstants.FIELD_STATUS,
                DeployStatusEnum.WAIT_TO_DOWNLOAD_CONTENT.name()),
            null,
            null,
            null,
            null);
    if (CollectionUtils.isEmpty(assetDtoPaging.getData())) {
      return;
    }
    UnifiedAssetDto unifiedAssetDto = assetDtoPaging.getData().get(0);
    String productReleaseId =
        unifiedAssetDto.getMetadata().getLabels().get(LabelConstants.LABEL_RELEASE_ID);

    HttpResponse<UnifiedAssetDto> httpResponse =
        blockCurl(
            HttpMethod.GET,
            uriBuilder ->
                uriBuilder
                    .path(appProperty.getMgmtPlane().getDownloadMappingTemplateEndpoint())
                    .queryParam("productReleaseId", productReleaseId)
                    .build(),
            null,
            new ParameterizedTypeReference<HttpResponse<UnifiedAssetDto>>() {});
    UnifiedAssetDto data = httpResponse.getData();
    if (data == null) {
      return;
    }
    if (unifiedAssetService.existed(data.getMetadata().getKey())) {
      log.info("existed published asset {}", data.getMetadata().getKey());
      return;
    }
    data.setId(null);
    data.getMetadata()
        .getLabels()
        .put(LabelConstants.LABEL_APP_TEMPLATE_UPGRADE_ID, unifiedAssetDto.getId());
    unifiedAssetService.syncAsset(
        unifiedAssetDto.getId(), data, new SyncMetadata("", "", DateTime.nowInUTCString()), true);
    // update template upgrade status
    unifiedAssetDto.getMetadata().setStatus(DeployStatusEnum.WAIT_TO_PUBLISH.name());
    IngestionDataResult ingestionDataResult =
        unifiedAssetService.syncAsset(
            unifiedAssetDto.getParentId(),
            unifiedAssetDto,
            new SyncMetadata("", "", DateTime.nowInUTCString()),
            true);
    if (ingestionDataResult.getCode() != HttpStatus.OK.value()) {
      return;
    }
    // first startup
    checkFirstStartUpAndExecuteInstalling(unifiedAssetDto.getId());
  }

  public void checkFirstStartUpAndExecuteInstalling(String id) {
    Paging<UnifiedAssetDto> targetPaging =
        unifiedAssetService.findBySpecification(
            Tuple2.ofList(AssetsConstants.FIELD_KIND, AssetKindEnum.COMPONENT_API_TARGET.getKind()),
            null,
            null,
            null,
            null);
    if (CollectionUtils.isNotEmpty(targetPaging.getData())) {
      return;
    }
    installMappingTemplateViaMgmt(id);
  }

  @SchedulerLock(
      name = "queryLatestProductReleaseLock",
      lockAtMostFor = "${app.cron-job.lock.at-most-for}",
      lockAtLeastFor = "${app.cron-job.lock.at-least-for}")
  @Scheduled(cron = "${app.cron-job.pull-latest-release-from-mgmt:-}")
  public void queryLatestProductRelease() {
    Paging<UnifiedAssetDto> assetDtoPaging =
        unifiedAssetService.findBySpecification(
            Tuple2.ofList(AssetsConstants.FIELD_KIND, AssetKindEnum.PRODUCT.getKind()),
            null,
            null,
            null,
            null);
    UnifiedAssetDto productAsset = assetDtoPaging.getData().get(0);
    Paging<UnifiedAssetDto> targetPaging =
        unifiedAssetService.findBySpecification(
            Tuple2.ofList(AssetsConstants.FIELD_KIND, AssetKindEnum.COMPONENT_API_TARGET.getKind()),
            null,
            null,
            null,
            null);
    HttpResponse<UnifiedAssetDto> httpResponse = null;
    if (CollectionUtils.isEmpty(targetPaging.getData())) {
      httpResponse = queryLatestRelease(Map.of());
    } else {
      UnifiedAssetDto unifiedAssetDto = assetDtoPaging.getData().get(0);
      String productKey = unifiedAssetDto.getMetadata().getKey();
      httpResponse = queryLatestRelease(Map.of("productKey", productKey));
    }
    if (httpResponse == null || httpResponse.getData() == null) {
      return;
    }
    UnifiedAssetDto data = httpResponse.getData();
    if (unifiedAssetService.existed(data.getMetadata().getKey())) {
      log.info("existed  release  asset {}", data.getMetadata().getKey());
      return;
    }
    data.getMetadata()
        .getLabels()
        .put(LabelConstants.LABEL_UPGRADE_SOURCE, UpgradeSourceEnum.MGMT.name());
    unifiedAssetService.syncAsset(
        productAsset.getId(), data, new SyncMetadata("", "", DateTime.nowInUTCString()), true);
    eventSinkService.reportTemplateUpgradeResult(
        data, UpgradeResultEventEnum.RECEIVED, event -> event.setReceivedAt(ZonedDateTime.now()));
  }

  private HttpResponse<UnifiedAssetDto> queryLatestRelease(Map<String, String> params) {
    return blockCurl(
        HttpMethod.GET,
        uriBuilder -> {
          uriBuilder.path(appProperty.getMgmtPlane().getRetrieveProductReleaseEndpoint());
          params.forEach(uriBuilder::queryParam);
          return uriBuilder.build();
        },
        null,
        new ParameterizedTypeReference<HttpResponse<UnifiedAssetDto>>() {});
  }

  public void installMappingTemplateViaMgmt(String appTemplateUpgradeId) {
    UnifiedAssetDto assetDto = unifiedAssetService.findOne(appTemplateUpgradeId);
    if (!DeployStatusEnum.WAIT_TO_PUBLISH
        .name()
        .equalsIgnoreCase(assetDto.getMetadata().getStatus())) {
      return;
    }
    // upgrade
    Paging<UnifiedAssetDto> downloadAssetPaging =
        unifiedAssetService.findBySpecification(
            Tuple2.ofList(
                AssetsConstants.FIELD_PARENT_ID,
                appTemplateUpgradeId,
                AssetsConstants.FIELD_KIND,
                AssetKindEnum.PRODUCT_RELEASE_DOWNLOAD.getKind()),
            null,
            null,
            null,
            null);
    if (CollectionUtils.isEmpty(downloadAssetPaging.getData())) {
      return;
    }
    UnifiedAssetDto downloadAsset = downloadAssetPaging.getData().get(0);
    ProductReleaseDownloadFacets downloadFacets =
        UnifiedAsset.getFacets(downloadAsset, ProductReleaseDownloadFacets.class);
    UnifiedAsset sourceProduct = resourceCacheHolder.findSourceProduct(downloadFacets);
    dataIngestionJob.ingestData(
        new IngestDataEvent(null, ResourceLoaderTypeEnum.RAW + JsonToolkit.toJson(sourceProduct)));
    eventSinkService.reportTemplateUpgradeResult(
        assetDto,
        UpgradeResultEventEnum.INSTALLED,
        event -> event.setInstalledAt(ZonedDateTime.now()));
  }
}
