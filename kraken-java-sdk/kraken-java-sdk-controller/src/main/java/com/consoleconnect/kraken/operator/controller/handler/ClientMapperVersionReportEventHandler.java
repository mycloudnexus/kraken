package com.consoleconnect.kraken.operator.controller.handler;

import com.consoleconnect.kraken.operator.controller.event.SingleMapperReportEvent;
import com.consoleconnect.kraken.operator.core.client.ClientEvent;
import com.consoleconnect.kraken.operator.core.client.ClientEventTypeEnum;
import com.consoleconnect.kraken.operator.core.client.ClientMapperVersion;
import com.consoleconnect.kraken.operator.core.model.HttpResponse;
import com.consoleconnect.kraken.operator.core.toolkit.JsonToolkit;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Slf4j
@Service
@AllArgsConstructor
public class ClientMapperVersionReportEventHandler extends ClientEventHandler {

  private final ApplicationEventPublisher applicationEventPublisher;

  @Override
  public ClientEventTypeEnum getEventType() {
    return ClientEventTypeEnum.CLIENT_MAPPER_VERSION;
  }

  @Override
  public HttpResponse<Void> onEvent(String envId, String userId, ClientEvent event) {
    if (event.getEventPayload() == null) {
      return HttpResponse.ok(null);
    }
    List<ClientMapperVersion> instances =
        JsonToolkit.fromJson(event.getEventPayload(), new TypeReference<>() {});
    if (CollectionUtils.isEmpty(instances)) {
      return HttpResponse.ok(null);
    }
    for (ClientMapperVersion instance : instances) {
      applicationEventPublisher.publishEvent(
          new SingleMapperReportEvent(
              envId, instance.getMapperKey(), instance.getVersion(), instance.getSubVersion()));
    }
    return HttpResponse.ok(null);
  }
}
