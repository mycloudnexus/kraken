package com.consoleconnect.kraken.operator.controller.service;

import com.consoleconnect.kraken.operator.core.entity.EnvironmentClientEntity;
import com.consoleconnect.kraken.operator.core.enums.ClientReportTypeEnum;
import com.consoleconnect.kraken.operator.core.enums.EnvNameEnum;
import com.consoleconnect.kraken.operator.core.enums.ResourceRoleEnum;
import com.consoleconnect.kraken.operator.core.repo.EnvironmentClientRepository;
import com.consoleconnect.kraken.operator.core.service.BuildVersionService;
import com.consoleconnect.kraken.operator.core.toolkit.Constants;
import com.consoleconnect.kraken.operator.core.toolkit.DateTime;
import com.consoleconnect.kraken.operator.core.toolkit.IpUtils;
import java.time.ZonedDateTime;
import lombok.extern.slf4j.Slf4j;
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

  private BuildVersionService buildVersionService;

  public HeartBeatService(
      final EnvironmentClientRepository environmentClientRepository,
      final BuildVersionService buildVersionService) {
    this.environmentClientRepository = environmentClientRepository;
    this.buildVersionService = buildVersionService;
  }

  @Scheduled(cron = "${app.cron-job.push-heartbeat:-}")
  @Transactional
  public void heartBeat() {
    ZonedDateTime now = DateTime.nowInUTC();
    log.debug(
        "[{}][{}] Heartbeat at {}",
        Constants.LOG_FIELD_CRON_JOB,
        Constants.LOG_FIELD_HEARTBEAT,
        now);
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
    environmentClientEntity.setAppVersion(buildVersionService.getAppVersion());
    environmentClientEntity.setStatus(HttpStatus.OK.name());
    environmentClientEntity.setUpdatedAt(now);
    environmentClientRepository.save(environmentClientEntity);
  }
}
