package com.consoleconnect.kraken.operator.sync.service;

import static com.consoleconnect.kraken.operator.core.service.ApiActivityLogService.TRIGGERED_AT;

import com.consoleconnect.kraken.operator.core.client.ClientEvent;
import com.consoleconnect.kraken.operator.core.client.ClientEventTypeEnum;
import com.consoleconnect.kraken.operator.core.dto.ApiActivityLog;
import com.consoleconnect.kraken.operator.core.entity.ApiActivityLogEntity;
import com.consoleconnect.kraken.operator.core.enums.LifeStatusEnum;
import com.consoleconnect.kraken.operator.core.enums.SyncStatusEnum;
import com.consoleconnect.kraken.operator.core.mapper.ApiActivityLogMapper;
import com.consoleconnect.kraken.operator.core.model.HttpResponse;
import com.consoleconnect.kraken.operator.core.repo.ApiActivityLogRepository;
import com.consoleconnect.kraken.operator.core.service.ApiActivityLogService;
import com.consoleconnect.kraken.operator.core.service.UnifiedAssetService;
import com.consoleconnect.kraken.operator.core.toolkit.DateTime;
import com.consoleconnect.kraken.operator.sync.model.SyncProperty;
import java.time.ZonedDateTime;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@Slf4j
public class PushLogService extends KrakenServerConnector {

  private final ApiActivityLogRepository apiActivityLogRepository;
  private final ApiActivityLogService apiActivityLogService;

  public PushLogService(
      SyncProperty appProperty,
      WebClient webClient,
      ApiActivityLogRepository apiActivityLogRepository,
      ApiActivityLogService apiActivityLogService) {
    super(appProperty, webClient);
    this.apiActivityLogRepository = apiActivityLogRepository;
    this.apiActivityLogService = apiActivityLogService;
  }

  @SchedulerLock(
      name = "pushLogLock",
      lockAtMostFor = "${app.cron-job.lock.at-most-for}",
      lockAtLeastFor = "${app.cron-job.lock.at-least-for}")
  @Scheduled(cron = "${app.cron-job.push-log:-}")
  public void runIt() {
    ZonedDateTime createdAt =
        ZonedDateTime.now().minusSeconds(getAppProperty().getSynDelaySeconds());
    List<ApiActivityLogEntity> logEntities =
        apiActivityLogRepository
            .findAllBySyncStatusAndLifeStatusAndCreatedAtBefore(
                SyncStatusEnum.UNDEFINED,
                LifeStatusEnum.LIVE,
                createdAt,
                UnifiedAssetService.getSearchPageRequest(0, 50, Sort.Direction.ASC, TRIGGERED_AT))
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
            logEntities.stream().map(this::map).toList());

    HttpResponse<Void> res = pushEvent(event);
    if (res.getCode() == 200) {
      ZonedDateTime now = DateTime.nowInUTC();
      this.apiActivityLogService.setSynced(logEntities, now);
    }
  }

  private ApiActivityLog map(ApiActivityLogEntity entity) {
    ApiActivityLog dto = ApiActivityLogMapper.INSTANCE.map(entity);
    dto.setTriggeredAt(entity.getCreatedAt());
    return dto;
  }
}
