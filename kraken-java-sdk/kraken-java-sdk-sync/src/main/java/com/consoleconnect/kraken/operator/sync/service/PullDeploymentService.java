package com.consoleconnect.kraken.operator.sync.service;

import com.consoleconnect.kraken.operator.core.client.ClientEvent;
import com.consoleconnect.kraken.operator.core.client.ClientEventTypeEnum;
import com.consoleconnect.kraken.operator.core.client.ClientInstanceDeployment;
import com.consoleconnect.kraken.operator.core.dto.UnifiedAssetDto;
import com.consoleconnect.kraken.operator.core.entity.UnifiedAssetEntity;
import com.consoleconnect.kraken.operator.core.enums.AssetKindEnum;
import com.consoleconnect.kraken.operator.core.enums.DeployStatusEnum;
import com.consoleconnect.kraken.operator.core.event.DeploymentEvent;
import com.consoleconnect.kraken.operator.core.event.IngestDataEvent;
import com.consoleconnect.kraken.operator.core.event.ReportConfigReloadEvent;
import com.consoleconnect.kraken.operator.core.ingestion.DataIngestionJob;
import com.consoleconnect.kraken.operator.core.model.HttpResponse;
import com.consoleconnect.kraken.operator.core.repo.UnifiedAssetRepository;
import com.consoleconnect.kraken.operator.core.toolkit.JsonToolkit;
import com.consoleconnect.kraken.operator.data.entity.AssetReleaseEntity;
import com.consoleconnect.kraken.operator.data.repo.AssetReleaseRepository;
import com.consoleconnect.kraken.operator.sync.model.SyncProperty;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@Slf4j
public class PullDeploymentService extends KrakenServerConnector {
  private final DataIngestionJob dataIngestionJob;
  private final UnifiedAssetRepository assetRepository;
  private final AssetReleaseRepository assetReleaseRepository;
  private final SyncProperty syncProperty;

  public PullDeploymentService(
      SyncProperty appProperty,
      WebClient webClient,
      DataIngestionJob dataIngestionJob,
      UnifiedAssetRepository assetRepository,
      AssetReleaseRepository assetReleaseRepository,
      SyncProperty syncProperty) {
    super(appProperty, webClient);
    this.dataIngestionJob = dataIngestionJob;
    this.assetRepository = assetRepository;
    this.assetReleaseRepository = assetReleaseRepository;
    this.syncProperty = syncProperty;
  }

  private void installDeployment(DeploymentEvent event, HttpResponse<List<UnifiedAssetDto>> res) {
    res.getData().stream()
        .filter(dto -> getAppProperty().getAcceptAssetKinds().contains(dto.getKind()))
        .map(
            asset -> {
              if (asset.getKind().equalsIgnoreCase(AssetKindEnum.COMPONENT_API.getKind())) {
                this.ingestData(asset);
              }
              return asset;
            })
        .forEach(this::ingestData);

    AssetReleaseEntity assetReleaseEntity = new AssetReleaseEntity();
    assetReleaseEntity.setProductKey(event.getProductId());
    assetReleaseEntity.setVersion(event.getProductReleaseId());
    assetReleaseEntity.setPayload(res.getData());
    assetReleaseRepository.save(assetReleaseEntity);
  }

  public void pushDeploymentStatus(ReportConfigReloadEvent event) {

    ClientInstanceDeployment clientInstanceDeployment = new ClientInstanceDeployment();
    clientInstanceDeployment.setProductReleaseId(event.getProductReleaseId());
    clientInstanceDeployment.setStatus(event.getStatus());
    clientInstanceDeployment.setReason(event.getReason());
    clientInstanceDeployment.setInstanceId(CLIENT_ID);

    ClientEvent clientEvent =
        ClientEvent.of(CLIENT_ID, ClientEventTypeEnum.CLIENT_DEPLOYMENT, clientInstanceDeployment);
    pushEvent(clientEvent);
  }

  protected void ingestData(UnifiedAssetDto dto) {
    Optional<UnifiedAssetEntity> unifiedAssetEntityOpt =
        assetRepository.findOneByKey(dto.getMetadata().getKey());
    if (unifiedAssetEntityOpt.isEmpty()) {
      log.info("reload empty asset {}", dto.getMetadata().getKey());
    }
    IngestDataEvent event = new IngestDataEvent();
    if (unifiedAssetEntityOpt.isPresent()) {
      UnifiedAssetEntity entity = unifiedAssetEntityOpt.get();
      event.setParentId(entity.getParentId());
    }
    if (event.getParentId() == null) {
      event.setParentId(dto.getParentId());
    }
    event.setAsset(dto);
    event.setFullPath("raw:" + JsonToolkit.toJson(dto));
    event.setEnforceSync(getAppProperty().isAssetConfigOverwriteFlag());
    dataIngestionJob.ingestData(event);
  }

  @SchedulerLock(
      name = "scheduledCheckLatestProductReleaseLock",
      lockAtMostFor = "${app.cron-job.lock.at-most-for}",
      lockAtLeastFor = "${app.cron-job.lock.at-least-for}")
  @Scheduled(cron = "${app.cron-job.pull-latest-release:-}")
  public void scheduledCheckLatestProductRelease() {

    // step1: retrieve latest deployment id
    HttpResponse<String> res =
        curl(
            HttpMethod.GET,
            getAppProperty().getControlPlane().getLatestDeploymentEndpoint(),
            null,
            new ParameterizedTypeReference<>() {});

    if (res.getCode() != 200 || res.getData() == null) {
      log.error("failed to retrieve latest deployment id,{}", res);
      return;
    }

    String productReleasedId = res.getData();
    Optional<AssetReleaseEntity> releaseEntity =
        assetReleaseRepository.findFirstByOrderByCreatedAtDesc();
    if (releaseEntity.isEmpty()
        || !releaseEntity.get().getVersion().equalsIgnoreCase(productReleasedId)) {
      // deployment release id changed
      // step2:  retrieve product release detail
      DeploymentEvent deploymentEvent = new DeploymentEvent();
      deploymentEvent.setProductId(syncProperty.getControlPlane().getDefaultProductId());
      deploymentEvent.setProductReleaseId(productReleasedId);

      String releaseDetailEndpoint =
          String.format(
              getAppProperty().getControlPlane().getRetrieveProductReleaseDetailEndpoint(),
              productReleasedId);
      HttpResponse<List<UnifiedAssetDto>> releaseDetailsRes =
          curl(HttpMethod.GET, releaseDetailEndpoint, null, new ParameterizedTypeReference<>() {});
      if (releaseDetailsRes.getCode() == 200) {
        // step3: install deployment
        installDeployment(deploymentEvent, releaseDetailsRes);

        // step4: push deployment status
        // notify deployment status
        pushDeploymentStatus(
            new ReportConfigReloadEvent(
                deploymentEvent.getProductReleaseId(), DeployStatusEnum.SUCCESS.name(), ""));
      } else {
        log.error("failed to retrieve product release detail");
      }
    }
  }
}
