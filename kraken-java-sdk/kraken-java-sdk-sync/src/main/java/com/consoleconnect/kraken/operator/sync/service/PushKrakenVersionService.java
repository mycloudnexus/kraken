package com.consoleconnect.kraken.operator.sync.service;

import com.consoleconnect.kraken.operator.core.entity.MgmtEventEntity;
import com.consoleconnect.kraken.operator.core.enums.EventStatusType;
import com.consoleconnect.kraken.operator.core.enums.MgmtEventType;
import com.consoleconnect.kraken.operator.core.repo.MgmtEventRepository;
import com.consoleconnect.kraken.operator.core.repo.SystemInfoRepository;
import lombok.RequiredArgsConstructor;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
@RequiredArgsConstructor
public class PushKrakenVersionService {
  private static final String KEY = "CONTROL_PLANE";
  private final SystemInfoRepository systemInfoRepository;
  private final MgmtEventRepository eventRepository;
  private final MgmtEventRepository mgmtEventRepository;

  @SchedulerLock(
      name = "pushKrakenVersionLock",
      lockAtMostFor = "${app.cron-job.lock.at-most-for}",
      lockAtLeastFor = "${app.cron-job.lock.at-least-for}")
  @Scheduled(cron = "${app.cron-job.sync-system-info-from-control-plane:-}")
  public void runIt() {
    // produce sync kraken info event
    systemInfoRepository
        .findOneByKey(KEY)
        .ifPresent(
            systemInfoEntity -> {
              Page<MgmtEventEntity> mgmtEventEntities =
                  mgmtEventRepository.search(
                      MgmtEventType.CLIENT_SYSTEM_INFO.name(), null, Pageable.ofSize(1));
              if (CollectionUtils.isEmpty(mgmtEventEntities.getContent())) {
                MgmtEventEntity entity = new MgmtEventEntity();
                entity.setStatus(EventStatusType.WAIT_TO_SEND.name());
                entity.setPayload(systemInfoEntity);
                entity.setResourceId(systemInfoEntity.getId().toString());
                entity.setEventType(MgmtEventType.CLIENT_SYSTEM_INFO.name());
                eventRepository.save(entity);
              } else {
                // if sync system info event exists, update state to wait_to_send to re-active the
                // event
                MgmtEventEntity eventEntity = mgmtEventEntities.getContent().get(0);
                eventEntity.setStatus(EventStatusType.WAIT_TO_SEND.name());
                eventEntity.setPayload(systemInfoEntity);
                eventRepository.save(eventEntity);
              }
            });
  }
}
