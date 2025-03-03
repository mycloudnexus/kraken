package com.consoleconnect.kraken.operator.controller.handler;

import com.consoleconnect.kraken.operator.controller.entity.EnvironmentEntity;
import com.consoleconnect.kraken.operator.controller.model.SystemInfo;
import com.consoleconnect.kraken.operator.controller.repo.EnvironmentRepository;
import com.consoleconnect.kraken.operator.controller.service.SystemInfoService;
import com.consoleconnect.kraken.operator.core.client.ClientEvent;
import com.consoleconnect.kraken.operator.core.client.ClientEventTypeEnum;
import com.consoleconnect.kraken.operator.core.client.ClientInstanceHeartbeat;
import com.consoleconnect.kraken.operator.core.entity.EnvironmentClientEntity;
import com.consoleconnect.kraken.operator.core.enums.ClientReportTypeEnum;
import com.consoleconnect.kraken.operator.core.enums.EnvNameEnum;
import com.consoleconnect.kraken.operator.core.model.HttpResponse;
import com.consoleconnect.kraken.operator.core.repo.EnvironmentClientRepository;
import com.consoleconnect.kraken.operator.core.service.EventSinkService;
import com.consoleconnect.kraken.operator.core.toolkit.JsonToolkit;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

@Slf4j
@Service
@AllArgsConstructor
public class ClientHeartbeatEventHandler extends ClientEventHandler {

  private final EnvironmentClientRepository environmentClientRepository;

  private final EnvironmentRepository environmentRepository;

  private final SystemInfoService systemInfoService;
  private final EventSinkService eventSinkService;

  @Override
  public ClientEventTypeEnum getEventType() {
    return ClientEventTypeEnum.CLIENT_HEARTBEAT;
  }

  @Transactional
  @Override
  public HttpResponse<Void> onEvent(String envId, String userId, ClientEvent event) {
    if (event.getEventPayload() == null) {
      return HttpResponse.ok(null);
    }

    List<ClientInstanceHeartbeat> instances =
        JsonToolkit.fromJson(event.getEventPayload(), new TypeReference<>() {});
    if (CollectionUtils.isEmpty(instances)) {
      return HttpResponse.ok(null);
    }
    Optional<EnvironmentEntity> envOptional = queryEnvName(envId);
    for (ClientInstanceHeartbeat instance : instances) {
      EnvironmentClientEntity environmentClientEntity =
          environmentClientRepository
              .findOneByEnvIdAndClientKeyAndKind(
                  envId, instance.getInstanceId(), ClientReportTypeEnum.HEARTBEAT.name())
              .orElseGet(
                  () -> {
                    EnvironmentClientEntity entity = new EnvironmentClientEntity();
                    entity.setEnvId(envId);
                    envOptional.ifPresent(item -> entity.setEnvName(item.getName()));
                    entity.setKind(ClientReportTypeEnum.HEARTBEAT.name());
                    entity.setClientKey(instance.getInstanceId());
                    entity.setFqdn(instance.getFqdn());
                    entity.setAppVersion(instance.getAppVersion());
                    entity.setCreatedAt(instance.getUpdatedAt());
                    entity.setCreatedBy(userId);
                    return entity;
                  });

      environmentClientEntity.setStatus(HttpStatus.OK.name());
      environmentClientEntity.setUpdatedAt(instance.getUpdatedAt());
      environmentClientEntity.setUpdatedBy(userId);
      environmentClientEntity.setAppVersion(instance.getAppVersion());
      environmentClientRepository.save(environmentClientEntity);
      updateAppVersion(envId, instance);
    }
    return HttpResponse.ok(null);
  }

  public Optional<EnvironmentEntity> queryEnvName(String envId) {
    try {
      return environmentRepository.findById(UUID.fromString(envId));
    } catch (Exception e) {
      log.error("Failed to query env by id:{}", envId);
    }
    return Optional.empty();
  }

  private void updateAppVersion(String envId, ClientInstanceHeartbeat heartbeat) {
    SystemInfo systemInfo = systemInfoService.find();
    environmentRepository
        .findById(UUID.fromString(envId))
        .ifPresent(
            env -> {
              if (EnvNameEnum.STAGE.name().equalsIgnoreCase(env.getName())
                  && !StringUtils.equalsIgnoreCase(
                      heartbeat.getAppVersion(), systemInfo.getStageAppVersion())) {
                reportKrakenVersionUpgrade(EnvNameEnum.STAGE, heartbeat);
                systemInfoService.updateAppVersion(null, heartbeat.getAppVersion(), null);
              }

              if (EnvNameEnum.PRODUCTION.name().equalsIgnoreCase(env.getName())
                  && !StringUtils.equalsIgnoreCase(
                      heartbeat.getAppVersion(), systemInfo.getProductionAppVersion())) {
                reportKrakenVersionUpgrade(EnvNameEnum.PRODUCTION, heartbeat);
                systemInfoService.updateAppVersion(null, null, heartbeat.getAppVersion());
              }
            });
  }

  private void reportKrakenVersionUpgrade(EnvNameEnum envName, ClientInstanceHeartbeat heartbeat) {
    eventSinkService.reportKrakenVersionUpgradeResult(
        envName,
        heartbeat.getAppVersion(),
        Optional.ofNullable(heartbeat.getStartUpAt()).orElse(heartbeat.getUpdatedAt()));
  }
}
