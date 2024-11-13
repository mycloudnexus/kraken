package com.consoleconnect.kraken.operator.sync.service;

import com.consoleconnect.kraken.operator.core.client.ClientEvent;
import com.consoleconnect.kraken.operator.core.client.ClientEventTypeEnum;
import com.consoleconnect.kraken.operator.core.client.ClientInstanceHeartbeat;
import com.consoleconnect.kraken.operator.data.entity.InstanceHeartbeatEntity;
import com.consoleconnect.kraken.operator.data.repo.HeartbeatRepository;
import com.consoleconnect.kraken.operator.sync.model.SyncProperty;
import java.time.ZonedDateTime;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@Slf4j
public class PushHeartbeatService extends KrakenServerConnector {

  private final HeartbeatRepository heartbeatRepository;
  private ZonedDateTime lastSyncedAt = null;

  public PushHeartbeatService(
      SyncProperty appProperty, WebClient webClient, HeartbeatRepository heartbeatRepository) {
    super(appProperty, webClient);
    this.heartbeatRepository = heartbeatRepository;
  }

  @Scheduled(cron = "${app.cron-job.push-heartbeat:-}")
  public void runIt() {
    ZonedDateTime now = ZonedDateTime.now();
    if (lastSyncedAt == null) {
      lastSyncedAt = now.minusMinutes(1);
    }
    List<InstanceHeartbeatEntity> instances =
        heartbeatRepository.findAllByUpdatedAtGreaterThanEqual(lastSyncedAt);
    lastSyncedAt = now;
    if (instances.isEmpty()) {
      log.info("No instances to push from clientId:{}", CLIENT_ID);
      return;
    }
    log.debug("Pushing instances to kraken server, size: {}", instances.size());
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
                  heartbeat.setStartUpAt(entity.getStartUpTAt());
                  return heartbeat;
                })
            .toList();
    ClientEvent event = ClientEvent.of(CLIENT_ID, ClientEventTypeEnum.CLIENT_HEARTBEAT, heartbeats);
    pushEvent(event);
  }
}
