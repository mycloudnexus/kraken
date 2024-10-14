package com.consoleconnect.kraken.operator.core.client;

import java.util.HashMap;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ClientEventTest {
  @Test
  void testOf() {
    ClientEvent event = ClientEvent.of("clientId", ClientEventTypeEnum.CLIENT_HEARTBEAT, null);
    Assertions.assertEquals("clientId", event.getClientId());
    Assertions.assertEquals(ClientEventTypeEnum.CLIENT_HEARTBEAT, event.getEventType());
    Assertions.assertNull(event.getEventPayload());

    event = ClientEvent.of("clientId", ClientEventTypeEnum.CLIENT_API_AUDIT_LOG, "eventPayload");
    Assertions.assertEquals("clientId", event.getClientId());
    Assertions.assertEquals(ClientEventTypeEnum.CLIENT_API_AUDIT_LOG, event.getEventType());
    Assertions.assertEquals("eventPayload", event.getEventPayload());

    event =
        ClientEvent.of(
            "clientId", ClientEventTypeEnum.CLIENT_API_AUDIT_LOG, new HashMap<String, Object>());
    Assertions.assertEquals("clientId", event.getClientId());
    Assertions.assertEquals(ClientEventTypeEnum.CLIENT_API_AUDIT_LOG, event.getEventType());
    Assertions.assertNotNull(event.getEventPayload());
  }
}
