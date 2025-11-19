package com.consoleconnect.kraken.operator.gateway.service;

import com.consoleconnect.kraken.operator.core.enums.ResourceRoleEnum;
import com.consoleconnect.kraken.operator.core.toolkit.DateTime;
import com.consoleconnect.kraken.operator.core.toolkit.IpUtils;
import com.consoleconnect.kraken.operator.data.entity.InstanceHeartbeatEntity;
import com.consoleconnect.kraken.operator.data.repo.HeartbeatRepository;
import java.time.ZonedDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class HeartBeatService {

  private static final String HOST_ADDRESS = IpUtils.getHostAddress();
  private static final String FQDN = IpUtils.getFQDN();

  private final HeartbeatRepository heartbeatRepository;

  public HeartBeatService(final HeartbeatRepository heartbeatRepository) {
    this.heartbeatRepository = heartbeatRepository;
  }

  @Value("${spring.build.version}")
  private String buildVersion;

  @Scheduled(cron = "${app.cron-job.push-heartbeat:-}")
  @Transactional
  public void heartBeat() {
    InstanceHeartbeatEntity entity = builInstanceHeartbeatEntity();
    heartbeatRepository.save(entity);
  }

  private InstanceHeartbeatEntity builInstanceHeartbeatEntity() {
    ZonedDateTime now = DateTime.nowInUTC();
    log.debug("Heartbeat at {}", now);
    InstanceHeartbeatEntity entity =
        heartbeatRepository
            .findOneByInstanceId(HOST_ADDRESS)
            .orElseGet(
                () -> {
                  InstanceHeartbeatEntity instanceHeartbeatEntity = new InstanceHeartbeatEntity();
                  instanceHeartbeatEntity.setInstanceId(HOST_ADDRESS);
                  instanceHeartbeatEntity.setFqdn(FQDN);
                  instanceHeartbeatEntity.setRole(ResourceRoleEnum.WORKER.name());
                  instanceHeartbeatEntity.setCreatedAt(now);
                  return instanceHeartbeatEntity;
                });
    entity.setAppVersion(buildVersion);
    entity.setUpdatedAt(now);
    return entity;
  }

  @EventListener(classes = ApplicationReadyEvent.class)
  public void onPlatformBootUp(Object event) {
    log.info("Application started up, report start up event");

    log.info("Deleting the heartbeat records");
    heartbeatRepository.deleteAll();
    log.info("Done");

    log.info("Creating new heartbeat record");
    InstanceHeartbeatEntity instanceHeartbeatEntity = builInstanceHeartbeatEntity();
    instanceHeartbeatEntity.setStartUpAt(ZonedDateTime.now());
    heartbeatRepository.save(instanceHeartbeatEntity);
    log.info("Done");
  }
}
