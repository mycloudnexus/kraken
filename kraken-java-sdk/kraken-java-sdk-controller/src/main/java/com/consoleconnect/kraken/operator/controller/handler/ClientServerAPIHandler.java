package com.consoleconnect.kraken.operator.controller.handler;

import com.consoleconnect.kraken.operator.core.client.ClientEvent;
import com.consoleconnect.kraken.operator.core.client.ClientEventTypeEnum;
import com.consoleconnect.kraken.operator.core.client.ServerAPIDto;
import com.consoleconnect.kraken.operator.core.entity.EnvironmentClientEntity;
import com.consoleconnect.kraken.operator.core.enums.ClientReportTypeEnum;
import com.consoleconnect.kraken.operator.core.model.HttpResponse;
import com.consoleconnect.kraken.operator.core.repo.EnvironmentClientRepository;
import com.consoleconnect.kraken.operator.core.toolkit.JsonToolkit;
import com.fasterxml.jackson.core.type.TypeReference;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class ClientServerAPIHandler extends ClientEventHandler {
  private final EnvironmentClientRepository environmentClientRepository;

  @Override
  public ClientEventTypeEnum getEventType() {
    return ClientEventTypeEnum.CLIENT_SERVER_API;
  }

  @Override
  public HttpResponse<Void> onEvent(String envId, String userId, ClientEvent event) {
    if (event.getEventPayload() == null) {
      return HttpResponse.ok(null);
    }
    List<ServerAPIDto> serverAPIDtos =
        JsonToolkit.fromJson(event.getEventPayload(), new TypeReference<List<ServerAPIDto>>() {});
    // Persist into kraken_mgmt_product_env_client
    if (CollectionUtils.isEmpty(serverAPIDtos)) {
      return HttpResponse.ok(null);
    }
    Map<String, List<ServerAPIDto>> serverKeyMap =
        serverAPIDtos.stream()
            .filter(item -> Objects.nonNull(item.getServerKey()))
            .collect(Collectors.groupingBy(ServerAPIDto::getServerKey));
    serverKeyMap
        .entrySet()
        .forEach(
            entry -> {
              EnvironmentClientEntity environmentClientEntity =
                  environmentClientRepository
                      .findOneByEnvIdAndClientKeyAndKind(
                          envId, entry.getKey(), ClientReportTypeEnum.CLIENT_SERVER_API.name())
                      .orElseGet(
                          () -> {
                            EnvironmentClientEntity entity = new EnvironmentClientEntity();
                            entity.setEnvId(envId);
                            entity.setKind(ClientReportTypeEnum.CLIENT_SERVER_API.name());
                            entity.setClientKey(entry.getKey());
                            entity.setCreatedAt(ZonedDateTime.now());
                            entity.setCreatedBy(userId);
                            return entity;
                          });
              environmentClientEntity.setUpdatedAt(ZonedDateTime.now());
              environmentClientEntity.setUpdatedBy(userId);
              environmentClientEntity.setPayload(entry.getValue());
              environmentClientRepository.save(environmentClientEntity);
            });
    return HttpResponse.ok(null);
  }
}
