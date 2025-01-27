package com.consoleconnect.kraken.operator.sync.service;

import static com.consoleconnect.kraken.operator.core.toolkit.AssetsConstants.FIELD_KIND;
import static com.consoleconnect.kraken.operator.core.toolkit.AssetsConstants.FIELD_UPDATED_AT;

import com.consoleconnect.kraken.operator.core.dto.Tuple2;
import com.consoleconnect.kraken.operator.core.dto.UnifiedAssetDto;
import com.consoleconnect.kraken.operator.core.enums.AssetKindEnum;
import com.consoleconnect.kraken.operator.core.model.HttpResponse;
import com.consoleconnect.kraken.operator.core.service.UnifiedAssetService;
import com.consoleconnect.kraken.operator.core.toolkit.DateTime;
import com.consoleconnect.kraken.operator.core.toolkit.JsonToolkit;
import com.consoleconnect.kraken.operator.sync.model.SyncProperty;
import com.consoleconnect.kraken.operator.sync.service.security.ExternalSystemTokenProvider;
import com.fasterxml.jackson.core.type.TypeReference;
import java.net.URI;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpMethod;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;

@Slf4j
@Service
public class GeneralSyncService extends KrakenServerConnector {

  private final SyncProperty syncProperty;
  private final UnifiedAssetService unifiedAssetService;
  private final List<ClientSyncHandler> clientSyncHandlers;

  public GeneralSyncService(
      SyncProperty syncProperty,
      WebClient webClient,
      ExternalSystemTokenProvider externalSystemTokenProvider,
      UnifiedAssetService unifiedAssetService,
      List<ClientSyncHandler> clientSyncHandlers) {
    super(syncProperty, webClient, externalSystemTokenProvider);
    this.syncProperty = syncProperty;
    this.unifiedAssetService = unifiedAssetService;
    this.clientSyncHandlers = clientSyncHandlers;
  }

  @SchedulerLock(
      name = "syncServerAssetsLock",
      lockAtMostFor = "${app.cron-job.lock.at-most-for}",
      lockAtLeastFor = "${app.cron-job.lock.at-least-for}")
  @Scheduled(cron = "${app.cron-job.pull-server-assets:-}")
  public void syncServerAssets() {
    clientSyncHandlers.forEach(
        handler -> {
          AssetKindEnum kind = handler.getKind();
          // 1. Query from client db to determine the latest updated time

          final ZonedDateTime lastUpdatedAt = findLastUpdatedAt(kind);
          Function<UriBuilder, URI> uriFunction = builderURIFunction(kind, lastUpdatedAt);
          // 2. Sending Http Get request to fetch the remote data sorted by updated_at in ascend
          // order.
          HttpResponse<Object> res =
              blockCurl(HttpMethod.GET, uriFunction, null, new ParameterizedTypeReference<>() {});
          if (res.getCode() == 200) {
            Optional.ofNullable(res.getData())
                .map(this::toAssetDtoList)
                .ifPresent(handler::handleAssets);
          }
        });
  }

  private List<UnifiedAssetDto> toAssetDtoList(Object data) {
    return JsonToolkit.fromJson(
        JsonToolkit.toJson(data), new TypeReference<List<UnifiedAssetDto>>() {});
  }

  private ZonedDateTime findLastUpdatedAt(AssetKindEnum kindEnum) {
    return unifiedAssetService
        .findBySpecification(
            Tuple2.ofList(FIELD_KIND, kindEnum.getKind()),
            null,
            null,
            PageRequest.of(0, 1, Sort.Direction.DESC, FIELD_UPDATED_AT),
            null)
        .getData()
        .stream()
        .findFirst()
        .map(UnifiedAssetDto::getUpdatedAt)
        .map(DateTime::of)
        .orElse(null);
  }

  private Function<UriBuilder, URI> builderURIFunction(
      AssetKindEnum kind, ZonedDateTime lastUpdatedAt) {
    return uriBuilder ->
        uriBuilder
            .path(syncProperty.getControlPlane().getSyncFromServerEndpoint())
            .queryParam(FIELD_KIND, kind.getKind())
            .queryParam(FIELD_UPDATED_AT, lastUpdatedAt)
            .build();
  }
}
