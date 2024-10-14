package com.consoleconnect.kraken.operator.controller.service;

import com.consoleconnect.kraken.operator.controller.handler.ClientEventHandler;
import com.consoleconnect.kraken.operator.core.client.ClientEvent;
import com.consoleconnect.kraken.operator.core.model.HttpResponse;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class ClientEventService {

  private final List<ClientEventHandler> eventHandlers;

  public HttpResponse<Void> onEvent(String envId, String userId, ClientEvent event) {
    log.info(
        "Received event: envId:{},userId:{},eventType:{},clientId:{}",
        envId,
        userId,
        event.getEventType(),
        event.getClientId());
    Optional<ClientEventHandler> clientEventHandlerOptional =
        eventHandlers.stream()
            .filter(handler -> handler.getEventType() == event.getEventType())
            .findFirst();
    if (clientEventHandlerOptional.isPresent()) {
      return clientEventHandlerOptional.get().onEvent(envId, userId, event);
    }
    return HttpResponse.of(404, "No handler found for event type: " + event.getEventType(), null);
  }
}
