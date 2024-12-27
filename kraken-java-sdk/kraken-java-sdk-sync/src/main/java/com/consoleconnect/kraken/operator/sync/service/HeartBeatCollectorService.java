package com.consoleconnect.kraken.operator.sync.service;

import com.consoleconnect.kraken.operator.core.entity.EnvironmentClientEntity;
import com.consoleconnect.kraken.operator.core.entity.MgmtEventEntity;
import com.consoleconnect.kraken.operator.core.enums.ClientReportTypeEnum;
import com.consoleconnect.kraken.operator.core.enums.EventStatusType;
import com.consoleconnect.kraken.operator.core.enums.MgmtEventType;
import com.consoleconnect.kraken.operator.core.enums.PlaneTypeEnum;
import com.consoleconnect.kraken.operator.core.event.HeartBeatUploadEvent;
import com.consoleconnect.kraken.operator.core.repo.EnvironmentClientRepository;
import com.consoleconnect.kraken.operator.core.repo.MgmtEventRepository;
import com.consoleconnect.kraken.operator.core.toolkit.JsonToolkit;
import java.time.ZonedDateTime;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class HeartBeatCollectorService {
  private final MgmtEventRepository mgmtEventRepository;
  private final EnvironmentClientRepository environmentClientRepository;
  private ZonedDateTime lastSyncedAt = null;

  public HeartBeatCollectorService(
      final MgmtEventRepository mgmtEventRepository,
      final EnvironmentClientRepository environmentClientRepository) {
    this.mgmtEventRepository = mgmtEventRepository;
    this.environmentClientRepository = environmentClientRepository;
  }

  @SchedulerLock(
      name = "heartBeatCollectorLock",
      lockAtMostFor = "${app.cron-job.lock.at-most-for}",
      lockAtLeastFor = "${app.cron-job.lock.at-least-for}")
  @Scheduled(cron = "${app.cron-job.push-heartbeat-collector:-}")
  public void runIt() {
    ZonedDateTime now = ZonedDateTime.now();
    if (lastSyncedAt == null) {
      lastSyncedAt = now.minusMinutes(1);
    }
    // 1.Collecting heartbeats from data plane
    collectHeartBeatByKind(ClientReportTypeEnum.HEARTBEAT, lastSyncedAt);
    // 2.Collecting heartbeats from control plane
    collectHeartBeatByKind(ClientReportTypeEnum.HEARTBEAT_CONTROL_PLANE, lastSyncedAt);
    lastSyncedAt = now;
  }

  public void collectHeartBeatByKind(
      ClientReportTypeEnum clientReportType, ZonedDateTime lastSyncedAt) {
    Page<EnvironmentClientEntity> environmentClientEntityPage =
        environmentClientRepository.findAllByKindAndUpdatedAtGreaterThan(
            clientReportType.name(), lastSyncedAt, PageRequest.of(0, 100));
    if (environmentClientEntityPage.isEmpty()) {
      return;
    }
    environmentClientEntityPage.stream()
        .forEach(
            item -> {
              HeartBeatUploadEvent event = new HeartBeatUploadEvent();
              event.setPlaneType(whichPlane(clientReportType).name());
              event.setFqdn(item.getFqdn());
              event.setEnvId(item.getEnvId());
              event.setEnvName(item.getEnvName());
              event.setIpAddress(item.getClientKey());
              event.setRole(item.getRole());
              event.setAppVersion(item.getAppVersion());

              MgmtEventEntity mgmtEventEntity = new MgmtEventEntity();
              mgmtEventEntity.setEventType(MgmtEventType.CLIENT_HEART_BEAT.name());
              mgmtEventEntity.setPayload(JsonToolkit.toJson(event));
              mgmtEventEntity.setStatus(EventStatusType.WAIT_TO_SEND.name());
              mgmtEventRepository.save(mgmtEventEntity);
            });
  }

  private PlaneTypeEnum whichPlane(ClientReportTypeEnum clientReportType) {
    return (ClientReportTypeEnum.HEARTBEAT.equals(clientReportType)
        ? PlaneTypeEnum.DATA_PLANE
        : PlaneTypeEnum.CONTROL_PLANE);
  }
}
