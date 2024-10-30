package com.consoleconnect.kraken.operator.sync.service;

import com.consoleconnect.kraken.operator.core.client.ClientEvent;
import com.consoleconnect.kraken.operator.core.client.ClientEventTypeEnum;
import com.consoleconnect.kraken.operator.core.client.ClientMapperVersion;
import com.consoleconnect.kraken.operator.core.client.ServerAPIDto;
import com.consoleconnect.kraken.operator.core.dto.UnifiedAssetDto;
import com.consoleconnect.kraken.operator.core.enums.AssetKindEnum;
import com.consoleconnect.kraken.operator.core.model.UnifiedAsset;
import com.consoleconnect.kraken.operator.core.model.facet.ComponentAPITargetFacets;
import com.consoleconnect.kraken.operator.core.service.UnifiedAssetService;
import com.consoleconnect.kraken.operator.core.toolkit.LabelConstants;
import com.consoleconnect.kraken.operator.sync.model.SyncProperty;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@Slf4j
public class PushRunningMapperInfoService extends KrakenServerConnector {

  private final UnifiedAssetService unifiedAssetService;

  private ZonedDateTime lastSyncedAt = null;

  public PushRunningMapperInfoService(
      SyncProperty appProperty, WebClient webClient, UnifiedAssetService unifiedAssetService) {
    super(appProperty, webClient);
    this.unifiedAssetService = unifiedAssetService;
  }

  @Transactional
  @Scheduled(cron = "${app.cron-job.push-running-mapper:-}")
  public void runIt() {
    ZonedDateTime now = ZonedDateTime.now();
    if (lastSyncedAt == null) {
      lastSyncedAt = now.minusMinutes(1);
    }
    List<UnifiedAssetDto> assetDtos =
        unifiedAssetService.findByKind(AssetKindEnum.COMPONENT_API_TARGET_MAPPER.getKind());
    reportMapperApiServer(assetDtos);
    reportMapperVersion(assetDtos);
  }

  private void reportMapperVersion(List<UnifiedAssetDto> mapperAssets) {
    if (CollectionUtils.isEmpty(mapperAssets)) {
      return;
    }
    List<ClientMapperVersion> mapperVersionList =
        mapperAssets.stream()
            .map(
                mapper -> {
                  Map<String, String> labels = mapper.getMetadata().getLabels();
                  String version = labels.get(LabelConstants.LABEL_VERSION_NAME);
                  String subVersion = labels.get(LabelConstants.LABEL_SUB_VERSION_NAME);
                  ClientMapperVersion clientMapperVersion = new ClientMapperVersion();
                  clientMapperVersion.setVersion(version);
                  clientMapperVersion.setSubVersion(subVersion);
                  clientMapperVersion.setMapperKey(mapper.getMetadata().getKey());
                  return clientMapperVersion;
                })
            .toList();

    ClientEvent event =
        ClientEvent.of(CLIENT_ID, ClientEventTypeEnum.CLIENT_MAPPER_VERSION, mapperVersionList);
    pushEvent(event);
  }

  private void reportMapperApiServer(List<UnifiedAssetDto> mapperAssets) {
    if (CollectionUtils.isEmpty(mapperAssets)) {
      return;
    }
    List<ServerAPIDto> serverAPIDtoList =
        mapperAssets.stream()
            .map(
                assetDto -> {
                  ComponentAPITargetFacets existFacets =
                      UnifiedAsset.getFacets(assetDto, ComponentAPITargetFacets.class);
                  ComponentAPITargetFacets.Endpoint existEndpoints =
                      existFacets.getEndpoints().get(0);

                  ServerAPIDto serverAPIDto = new ServerAPIDto();
                  serverAPIDto.setMapperKey(assetDto.getMetadata().getKey());
                  serverAPIDto.setServerKey(existEndpoints.getServerKey());
                  serverAPIDto.setMethod(existEndpoints.getMethod());
                  serverAPIDto.setPath(existEndpoints.getPath());
                  return serverAPIDto;
                })
            .toList();

    ClientEvent event =
        ClientEvent.of(CLIENT_ID, ClientEventTypeEnum.CLIENT_SERVER_API, serverAPIDtoList);
    pushEvent(event);
  }
}
