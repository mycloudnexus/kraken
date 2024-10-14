package com.consoleconnect.kraken.operator.controller.handler;

import com.consoleconnect.kraken.operator.core.client.ClientEvent;
import com.consoleconnect.kraken.operator.core.client.ClientEventTypeEnum;
import com.consoleconnect.kraken.operator.core.model.HttpResponse;

public abstract class ClientEventHandler {
  public abstract ClientEventTypeEnum getEventType();

  public abstract HttpResponse<Void> onEvent(String envId, String userId, ClientEvent event);
}
