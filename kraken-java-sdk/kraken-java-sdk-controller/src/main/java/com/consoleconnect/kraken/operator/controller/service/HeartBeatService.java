package com.consoleconnect.kraken.operator.controller.service;

import com.consoleconnect.kraken.operator.core.entity.EnvironmentClientEntity;
import com.consoleconnect.kraken.operator.core.enums.ClientReportTypeEnum;
import com.consoleconnect.kraken.operator.core.enums.EnvNameEnum;
import com.consoleconnect.kraken.operator.core.enums.ResourceRoleEnum;
import com.consoleconnect.kraken.operator.core.repo.EnvironmentClientRepository;
import com.consoleconnect.kraken.operator.core.toolkit.DateTime;
import com.consoleconnect.kraken.operator.core.toolkit.IpUtils;
import java.time.ZonedDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class HeartBeatService {

  private static final String HOST_ADDRESS = IpUtils.getHostAddress();
  private static final String FQDN = IpUtils.getFQDN();

  private final EnvironmentClientRepository environmentClientRepository;

  @Value("${spring.build.version}")
  private String buildVersion;

  public HeartBeatService(final EnvironmentClientRepository environmentClientRepository) {
    this.environmentClientRepository = environmentClientRepository;
  }

  @Scheduled(cron = "${app.cron-job.push-heartbeat:-}")
  @Transactional
  public void heartBeat() {
    ZonedDateTime now = DateTime.nowInUTC();
    log.debug("Heartbeat at {}", now);
    EnvironmentClientEntity environmentClientEntity =
        environmentClientRepository
            .findOneByEnvIdAndClientKeyAndKind(
                EnvNameEnum.UNDEFINED.name(),
                HOST_ADDRESS,
                ClientReportTypeEnum.HEARTBEAT_CONTROL_PLANE.name())
            .orElseGet(
                () -> {
                  EnvironmentClientEntity entity = new EnvironmentClientEntity();
                  entity.setEnvId(EnvNameEnum.UNDEFINED.name());
                  entity.setEnvName(EnvNameEnum.UNDEFINED.name());
                  entity.setKind(ClientReportTypeEnum.HEARTBEAT_CONTROL_PLANE.name());
                  entity.setClientKey(HOST_ADDRESS);
                  entity.setFqdn(FQDN);
                  entity.setRole(ResourceRoleEnum.WORKER.name());
                  entity.setCreatedAt(now);
                  return entity;
                });
    environmentClientEntity.setAppVersion(buildVersion);
    environmentClientEntity.setStatus(HttpStatus.OK.name());
    environmentClientEntity.setUpdatedAt(now);
    environmentClientRepository.save(environmentClientEntity);
  }
}
