package com.consoleconnect.kraken.operator.gateway.service;

import com.consoleconnect.kraken.operator.core.toolkit.DateTime;
import com.consoleconnect.kraken.operator.core.toolkit.IpUtils;
import com.consoleconnect.kraken.operator.data.entity.InstanceHeartbeatEntity;
import com.consoleconnect.kraken.operator.data.repo.HeartbeatRepository;
import java.time.ZonedDateTime;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@AllArgsConstructor
@Slf4j
@Service
public class HeartBeatService {

  private static final String HOST_ADDRESS = IpUtils.getHostAddress();

  private final HeartbeatRepository heartbeatRepository;

  @Scheduled(cron = "${app.cron-job.push-heartbeat:-}")
  @Transactional
  public void heartBeat() {
    ZonedDateTime now = DateTime.nowInUTC();
    log.debug("Heartbeat at {}", now);
    InstanceHeartbeatEntity entity =
        heartbeatRepository
            .findOneByInstanceId(HOST_ADDRESS)
            .orElseGet(
                () -> {
                  InstanceHeartbeatEntity instanceHeartbeatEntity = new InstanceHeartbeatEntity();
                  instanceHeartbeatEntity.setInstanceId(HOST_ADDRESS);
                  instanceHeartbeatEntity.setCreatedAt(now);
                  return instanceHeartbeatEntity;
                });
    entity.setUpdatedAt(now);
    heartbeatRepository.save(entity);
  }
}
