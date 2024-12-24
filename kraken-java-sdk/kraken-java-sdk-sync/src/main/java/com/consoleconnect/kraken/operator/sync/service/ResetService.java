package com.consoleconnect.kraken.operator.sync.service;

import com.consoleconnect.kraken.operator.core.dto.UnifiedAssetDto;
import com.consoleconnect.kraken.operator.core.dto.UpdateStatusDto;
import com.consoleconnect.kraken.operator.core.enums.EventStatusType;
import com.consoleconnect.kraken.operator.core.enums.MgmtEventType;
import com.consoleconnect.kraken.operator.core.model.HttpResponse;
import com.consoleconnect.kraken.operator.core.service.UnifiedAssetService;
import com.consoleconnect.kraken.operator.core.toolkit.JsonToolkit;
import com.consoleconnect.kraken.operator.core.toolkit.Paging;
import com.consoleconnect.kraken.operator.sync.model.MgmtEvent;
import com.consoleconnect.kraken.operator.sync.model.SyncProperty;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@Slf4j
public class ResetService extends KrakenServerConnector {

  private final SyncProperty syncProperty;
  private final UnifiedAssetService unifiedAssetService;

  public ResetService(
      SyncProperty syncProperty, WebClient webClient, UnifiedAssetService unifiedAssetService) {
    super(syncProperty, webClient);
    this.syncProperty = syncProperty;
    this.unifiedAssetService = unifiedAssetService;
  }

  @SchedulerLock(
      name = "scanEventLock",
      lockAtMostFor = "${app.cron-job.lock.at-most-for}",
      lockAtLeastFor = "${app.cron-job.lock.at-least-for}")
  @Scheduled(cron = "${app.cron-job.pull-reset-event:-}")
  public void scanEvent() {
    // search event
    HttpResponse<Paging<MgmtEvent>> result = searchEvent();
    if (result != null && result.getCode() != HttpStatus.OK.value()) {
      log.error("query event failed: {}", JsonToolkit.toJson(result));
      return;
    }
    List<MgmtEvent> data = result.getData().getData();
    List<MgmtEvent> list =
        data.stream()
            .filter(
                v ->
                    v.getEventType().name().equals(MgmtEventType.RESET.name())
                        && EventStatusType.ACK.name().equals(v.getStatus().name()))
            .toList();
    if (CollectionUtils.isEmpty(list)) {
      log.info("event list is empty");
      return;
    }
    List<String> assetKeyList = list.stream().map(MgmtEvent::getResourceId).toList();
    // clear mapper
    List<UnifiedAssetDto> assetDtos = unifiedAssetService.findAll();
    if (assetDtos == null) {
      log.info("No asset exists in data plane!");
      updateEventStatus(list, EventStatusType.DONE);
      return;
    }
    try {
      assetDtos.forEach(
          asset -> {
            if (!assetKeyList.contains(asset.getMetadata().getKey())) {
              return;
            }
            // delete mapper file
            unifiedAssetService.deleteOne(asset.getId());
            // delete target file
            unifiedAssetService.deleteOne(
                asset.getMetadata().getKey().replace("-mapper", StringUtils.EMPTY));
          });
    } catch (Exception e) {
      updateEventStatus(list, EventStatusType.FAILED);
    }
    // update event status
    updateEventStatus(list, EventStatusType.DONE);
  }

  public HttpResponse<Paging<MgmtEvent>> searchEvent() {
    return blockCurl(
        HttpMethod.GET,
        uriBuilder ->
            uriBuilder
                .path(syncProperty.getControlPlane().getScanEventEndpoint())
                .queryParam("page", 0)
                .queryParam("size", 100)
                .queryParam("status", "ACK")
                .build(),
        null,
        new ParameterizedTypeReference<>() {});
  }

  public void updateEventStatus(List<MgmtEvent> list, EventStatusType status) {
    List<String> idList = list.stream().map(MgmtEvent::getId).toList();
    UpdateStatusDto request = new UpdateStatusDto();
    request.setIds(idList);
    request.setStatus(status.name());

    curl(HttpMethod.PATCH, syncProperty.getControlPlane().getScanEventEndpoint(), request);
  }
}
