package com.consoleconnect.kraken.operator.controller.handler;

import com.consoleconnect.kraken.operator.controller.entity.EnvironmentClientEntity;
import com.consoleconnect.kraken.operator.controller.repo.EnvironmentClientRepository;
import com.consoleconnect.kraken.operator.core.client.ClientEvent;
import com.consoleconnect.kraken.operator.core.client.ClientEventTypeEnum;
import com.consoleconnect.kraken.operator.core.client.ClientInstanceHeartbeat;
import com.consoleconnect.kraken.operator.core.enums.ClientReportTypeEnum;
import com.consoleconnect.kraken.operator.core.model.HttpResponse;
import com.consoleconnect.kraken.operator.core.toolkit.JsonToolkit;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

@Service
@AllArgsConstructor
public class ClientHeartbeatEventHandler extends ClientEventHandler {

  private final EnvironmentClientRepository environmentClientRepository;

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
    for (ClientInstanceHeartbeat instance : instances) {
      EnvironmentClientEntity environmentClientEntity =
          environmentClientRepository
              .findOneByEnvIdAndAndClientIpAndKind(
                  envId, instance.getInstanceId(), ClientReportTypeEnum.HEARTBEAT.name())
              .orElseGet(
                  () -> {
                    EnvironmentClientEntity entity = new EnvironmentClientEntity();
                    entity.setEnvId(envId);
                    entity.setKind(ClientReportTypeEnum.HEARTBEAT.name());
                    entity.setClientIp(instance.getInstanceId());
                    entity.setCreatedAt(instance.getUpdatedAt());
                    entity.setCreatedBy(userId);
                    return entity;
                  });

      environmentClientEntity.setStatus(HttpStatus.OK.name());
      environmentClientEntity.setUpdatedAt(instance.getUpdatedAt());
      environmentClientEntity.setUpdatedBy(userId);
      environmentClientRepository.save(environmentClientEntity);
    }
    return HttpResponse.ok(null);
  }
}
