package com.consoleconnect.kraken.operator.core.client;

import com.consoleconnect.kraken.operator.core.toolkit.JsonToolkit;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClientEvent {
  private String clientId;
  private ClientEventTypeEnum eventType;
  private String eventPayload;

  public static ClientEvent of(
      String clientId, ClientEventTypeEnum eventType, Object eventPayload) {
    String jsonPayload = null;
    if (eventPayload instanceof String json) {
      jsonPayload = json;
    } else if (eventPayload != null) {
      jsonPayload = JsonToolkit.toJson(eventPayload);
    }
    return new ClientEvent(clientId, eventType, jsonPayload);
  }

  public static ClientEvent of(String clientId, ClientEventTypeEnum eventType) {
    return new ClientEvent(clientId, eventType, null);
  }
}
