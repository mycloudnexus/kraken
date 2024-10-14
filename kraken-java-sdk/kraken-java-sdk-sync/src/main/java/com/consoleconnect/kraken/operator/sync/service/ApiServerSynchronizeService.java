package com.consoleconnect.kraken.operator.sync.service;

import static com.consoleconnect.kraken.operator.core.enums.AssetKindEnum.*;

import com.consoleconnect.kraken.operator.core.dto.SimpleApiServerDto;
import com.consoleconnect.kraken.operator.core.dto.UnifiedAssetDto;
import com.consoleconnect.kraken.operator.core.entity.UnifiedAssetEntity;
import com.consoleconnect.kraken.operator.core.enums.ResourceLoaderTypeEnum;
import com.consoleconnect.kraken.operator.core.event.IngestDataEvent;
import com.consoleconnect.kraken.operator.core.ingestion.DataIngestionJob;
import com.consoleconnect.kraken.operator.core.model.HttpResponse;
import com.consoleconnect.kraken.operator.core.model.Metadata;
import com.consoleconnect.kraken.operator.core.model.SyncMetadata;
import com.consoleconnect.kraken.operator.core.repo.UnifiedAssetRepository;
import com.consoleconnect.kraken.operator.core.service.UnifiedAssetService;
import com.consoleconnect.kraken.operator.core.toolkit.DateTime;
import com.consoleconnect.kraken.operator.core.toolkit.JsonToolkit;
import com.consoleconnect.kraken.operator.sync.model.SyncProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpMethod;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Component
public class ApiServerSynchronizeService extends KrakenServerConnector {
  public static final String URLS = "urls";
  private final SyncProperty syncProperty;
  private final DataIngestionJob dataIngestionJob;
  private final UnifiedAssetRepository unifiedAssetRepository;
  private final ApplicationContext applicationContext;

  public ApiServerSynchronizeService(
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

  @Scheduled(cron = "${app.cron-job.pull-api-server-info:-}")
  public void synApiServerInfo() {
    HttpResponse<List<SimpleApiServerDto>> res =
        curl(
            HttpMethod.GET,
            syncProperty.getControlPlane().getApiServerEndpoint(),
            null,
            new ParameterizedTypeReference<>() {});
    if (res.getCode() == 200) {
      applicationContext.getBean(ApiServerSynchronizeService.class).ingestData(res);
    }
  }

  @Transactional
  public void ingestData(HttpResponse<List<SimpleApiServerDto>> response) {
    List<SimpleApiServerDto> newList = response.getData();
    if (CollectionUtils.isEmpty(response.getData())) {
      return;
    }

    Optional<UnifiedAssetEntity> optionalUnifiedAsset =
        unifiedAssetRepository.findOneByKey(COMPONENT_API_SERVER.getKind());
    if (optionalUnifiedAsset.isPresent()) {
      UnifiedAssetDto assetDto = UnifiedAssetService.toAsset(optionalUnifiedAsset.get(), true);
      List<SimpleApiServerDto> dbList =
          JsonToolkit.fromJson(
              JsonToolkit.toJson(assetDto.getFacets().get(URLS)),
              new TypeReference<List<SimpleApiServerDto>>() {});
      if (equals(newList, dbList)) {
        log.info("since the api server info has not changed,pass");
        return;
      }
    }

    UnifiedAssetDto unifiedAssetDto = new UnifiedAssetDto();
    unifiedAssetDto.setApiVersion("v1");
    unifiedAssetDto.setKind(COMPONENT_API_SERVER.getKind());
    Metadata metadata = new Metadata();
    metadata.setKey(COMPONENT_API_SERVER.getKind());
    unifiedAssetDto.setMetadata(metadata);
    Map<String, Object> facets = new HashMap<>();
    facets.put(URLS, newList);
    unifiedAssetDto.setFacets(facets);
    metadata.setDescription("api server url info");
    if (optionalUnifiedAsset.isPresent()) {
      UnifiedAssetEntity unifiedAssetEntity = optionalUnifiedAsset.get();
      metadata.setVersion(unifiedAssetEntity.getVersion() + 1);
    } else {
      metadata.setVersion(1);
    }
    Page<UnifiedAssetEntity> assetEntities =
        unifiedAssetRepository.search(
            null, PRODUCT.getKind(), null, UnifiedAssetService.getSearchPageRequest(0, 1));
    if (assetEntities.isEmpty()) {
      log.error("no product found");
      return;
    }
    UnifiedAssetEntity product = assetEntities.getContent().get(0);
    SyncMetadata syncMetadata = new SyncMetadata();
    syncMetadata.setSyncedAt(DateTime.nowInUTCString());
    unifiedAssetDto.setSyncMetadata(syncMetadata);
    IngestDataEvent event = new IngestDataEvent();
    event.setParentId(product.getId().toString());
    event.setFullPath(
        ResourceLoaderTypeEnum.generatePath(
            ResourceLoaderTypeEnum.RAW, JsonToolkit.toJson(unifiedAssetDto)));
    dataIngestionJob.ingestData(event);
  }

  private boolean equals(List<SimpleApiServerDto> newList, List<SimpleApiServerDto> oldList) {
    Map<String, String> newMap =
        newList.stream()
            .collect(
                Collectors.toMap(SimpleApiServerDto::getApiServerKey, SimpleApiServerDto::getUrl));
    Map<String, String> oldMap =
        oldList.stream()
            .collect(
                Collectors.toMap(SimpleApiServerDto::getApiServerKey, SimpleApiServerDto::getUrl));
    for (Map.Entry<String, String> entry : newMap.entrySet()) {
      if (oldMap.containsKey(entry.getKey())
          && entry.getValue().equals(oldMap.get(entry.getKey()))) {
        continue;
      }
      return false;
    }
    return true;
  }
}
