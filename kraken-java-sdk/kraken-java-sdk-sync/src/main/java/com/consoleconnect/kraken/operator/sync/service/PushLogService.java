package com.consoleconnect.kraken.operator.sync.service;

import static com.consoleconnect.kraken.operator.core.service.ApiActivityLogService.CREATED_AT;

import com.consoleconnect.kraken.operator.core.client.ClientEvent;
import com.consoleconnect.kraken.operator.core.client.ClientEventTypeEnum;
import com.consoleconnect.kraken.operator.core.entity.ApiActivityLogEntity;
import com.consoleconnect.kraken.operator.core.enums.SyncStatusEnum;
import com.consoleconnect.kraken.operator.core.mapper.ApiActivityLogMapper;
import com.consoleconnect.kraken.operator.core.model.HttpResponse;
import com.consoleconnect.kraken.operator.core.repo.ApiActivityLogRepository;
import com.consoleconnect.kraken.operator.core.service.UnifiedAssetService;
import com.consoleconnect.kraken.operator.core.toolkit.DateTime;
import com.consoleconnect.kraken.operator.sync.model.SyncProperty;
import java.time.ZonedDateTime;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@Slf4j
public class PushLogService extends KrakenServerConnector {

  private final ApiActivityLogRepository apiActivityLogRepository;

  public PushLogService(
      SyncProperty appProperty,
      WebClient webClient,
      ApiActivityLogRepository apiActivityLogRepository) {
    super(appProperty, webClient);
    this.apiActivityLogRepository = apiActivityLogRepository;
  }

  @Scheduled(cron = "${app.cron-job.push-log:-}")
  public void runIt() {
    ZonedDateTime createdAt = ZonedDateTime.now().minusSeconds(10);
    List<ApiActivityLogEntity> logEntities =
        apiActivityLogRepository
            .findAllBySyncStatusAndCreatedAtBefore(
                SyncStatusEnum.UNDEFINED,
                createdAt,
                UnifiedAssetService.getSearchPageRequest(0, 50, Sort.Direction.ASC, CREATED_AT))
            .getContent();

    if (logEntities.isEmpty()) {
      log.info("No logs to push from clientId:{}", CLIENT_ID);
      return;
    }

    log.info("Pushing logs to kraken server, size: {}", logEntities.size());

    ClientEvent event =
        ClientEvent.of(
            CLIENT_ID,
            ClientEventTypeEnum.CLIENT_API_AUDIT_LOG,
            logEntities.stream().map(ApiActivityLogMapper.INSTANCE::map).toList());

    HttpResponse<Void> res = pushEvent(event);
    if (res.getCode() == 200) {
      ZonedDateTime now = DateTime.nowInUTC();
      logEntities.forEach(
          logEntity -> {
            logEntity.setSyncStatus(SyncStatusEnum.SYNCED);
            logEntity.setSyncedAt(now);
          });
      apiActivityLogRepository.saveAll(logEntities);
    }
  }
}
