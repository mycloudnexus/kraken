package com.consoleconnect.kraken.operator.sync.service;

import com.consoleconnect.kraken.operator.core.client.ClientEvent;
import com.consoleconnect.kraken.operator.core.client.ClientEventTypeEnum;
import com.consoleconnect.kraken.operator.core.client.ClientInstanceHeartbeat;
import com.consoleconnect.kraken.operator.core.toolkit.Constants;
import com.consoleconnect.kraken.operator.data.entity.InstanceHeartbeatEntity;
import com.consoleconnect.kraken.operator.data.repo.HeartbeatRepository;
import com.consoleconnect.kraken.operator.sync.model.SyncProperty;
import com.consoleconnect.kraken.operator.sync.service.security.ExternalSystemTokenProvider;
import java.time.ZonedDateTime;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@Slf4j
public class PushHeartbeatService extends KrakenServerConnector {

  private final HeartbeatRepository heartbeatRepository;
  private ZonedDateTime lastSyncedAt = null;

  public PushHeartbeatService(
      SyncProperty appProperty,
      WebClient webClient,
      ExternalSystemTokenProvider externalSystemTokenProvider,
      HeartbeatRepository heartbeatRepository) {
    super(appProperty, webClient, externalSystemTokenProvider);
    this.heartbeatRepository = heartbeatRepository;
  }

  @SchedulerLock(
      name = "pushHeartbeatLock",
      lockAtMostFor = "${app.cron-job.lock.at-most-for}",
      lockAtLeastFor = "${app.cron-job.lock.at-least-for}")
  @Scheduled(cron = "${app.cron-job.push-heartbeat:-}")
  public void runIt() {
    ZonedDateTime now = ZonedDateTime.now();
    if (lastSyncedAt == null) {
      lastSyncedAt = now.minusMinutes(1);
    }
    List<InstanceHeartbeatEntity> instances =
        heartbeatRepository.findAllByUpdatedAtGreaterThanEqual(lastSyncedAt);
    lastSyncedAt = now;
    log.debug(
        "[{}][{}][{}] Puh client heartbeat event received, client id: {}",
        Constants.LOG_FIELD_CRON_JOB,
        Constants.LOG_FIELD_SYNC_EVENT,
        Constants.LOG_FIELD_HEARTBEAT,
        CLIENT_ID);
    if (instances.isEmpty()) {
      log.debug(
          "[{}][{}][{}] No instances to push from clientId: {}",
          Constants.LOG_FIELD_CRON_JOB,
          Constants.LOG_FIELD_SYNC_EVENT,
          Constants.LOG_FIELD_HEARTBEAT,
          CLIENT_ID);
      return;
    }
    log.debug(
        "[{}][{}][{}] Pushing instances to kraken server, size: {}",
        Constants.LOG_FIELD_CRON_JOB,
        Constants.LOG_FIELD_SYNC_EVENT,
        Constants.LOG_FIELD_HEARTBEAT,
        instances.size());
    List<ClientInstanceHeartbeat> heartbeats =
        instances.stream()
            .map(
                entity -> {
                  ClientInstanceHeartbeat heartbeat = new ClientInstanceHeartbeat();
                  heartbeat.setInstanceId(entity.getInstanceId());
                  heartbeat.setFqdn(entity.getFqdn());
                  heartbeat.setRole(entity.getRole());
                  heartbeat.setAppVersion(entity.getAppVersion());
                  heartbeat.setUpdatedAt(entity.getUpdatedAt());
                  heartbeat.setStartUpAt(entity.getStartUpAt());
                  log.debug(
                      "[{}][{}][{}] Heartbeat appVersion: {}, instanceId: {}",
                      Constants.LOG_FIELD_CRON_JOB,
                      Constants.LOG_FIELD_SYNC_EVENT,
                      Constants.LOG_FIELD_HEARTBEAT,
                      entity.getAppVersion(),
                      entity.getInstanceId());
                  return heartbeat;
                })
            .toList();
    ClientEvent event = ClientEvent.of(CLIENT_ID, ClientEventTypeEnum.CLIENT_HEARTBEAT, heartbeats);
    pushEvent(event);
  }
}
