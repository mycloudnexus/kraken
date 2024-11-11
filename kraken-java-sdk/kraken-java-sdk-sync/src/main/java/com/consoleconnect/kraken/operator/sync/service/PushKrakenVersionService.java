package com.consoleconnect.kraken.operator.sync.service;

import com.consoleconnect.kraken.operator.core.entity.MgmtEventEntity;
import com.consoleconnect.kraken.operator.core.enums.EventStatusType;
import com.consoleconnect.kraken.operator.core.enums.MgmtEventType;
import com.consoleconnect.kraken.operator.core.repo.MgmtEventRepository;
import com.consoleconnect.kraken.operator.core.repo.SystemInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PushKrakenVersionService {
  private static final String KEY = "CONTROL_PLANE";
  private final SystemInfoRepository systemInfoRepository;
  private final MgmtEventRepository eventRepository;

  @Scheduled(cron = "${app.cron-job.sync-system-info-from-control-plane:-}")
  public void runIt() {
    // produce sync kraken info event
    systemInfoRepository
        .findOneByKey(KEY)
        .ifPresent(
            systemInfoEntity -> {
              MgmtEventEntity entity = new MgmtEventEntity();
              entity.setStatus(EventStatusType.WAIT_TO_SEND.name());
              entity.setPayload(systemInfoEntity);
              entity.setResourceId(systemInfoEntity.getId().toString());
              entity.setEventType(MgmtEventType.CLIENT_SYSTEM_INFO.name());
              eventRepository.save(entity);
            });
  }
}
