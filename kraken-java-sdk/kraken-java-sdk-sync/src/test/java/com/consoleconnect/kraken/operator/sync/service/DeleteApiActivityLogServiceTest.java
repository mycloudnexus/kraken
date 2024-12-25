package com.consoleconnect.kraken.operator.sync.service;

import static org.mockito.Mockito.*;

import com.consoleconnect.kraken.operator.core.client.ClientEvent;
import com.consoleconnect.kraken.operator.core.client.ClientEventTypeEnum;
import com.consoleconnect.kraken.operator.core.repo.ApiActivityLogRepository;
import com.consoleconnect.kraken.operator.core.service.ApiActivityLogService;
import com.consoleconnect.kraken.operator.sync.CustomConfig;
import com.consoleconnect.kraken.operator.test.AbstractIntegrationTest;
import com.consoleconnect.kraken.operator.test.MockIntegrationTest;
import java.time.ZonedDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ContextConfiguration;

@MockIntegrationTest
@ContextConfiguration(classes = {CustomConfig.class})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DeleteApiActivityLogServiceTest extends AbstractIntegrationTest {
  @Autowired ApiActivityLogRepository apiActivityLogRepository;
  @SpyBean private DeleteLogService deleteLogService;
  @SpyBean private ApiActivityLogService apiActivityLogService;
  public static final String NOW_WITH_TIMEZONE = "2023-10-24T05:00:00+02:00";

  private static ClientEvent createEvent() {
    var clientEvent = new ClientEvent();
    clientEvent.setEventType(ClientEventTypeEnum.CLIENT_API_AUDIT_LOG);
    clientEvent.setClientId("127.0.1.1");

    clientEvent.setEventPayload(
        """
            [
                {
                  "requestId": "requestId",
                  "callSeq": 0,
                  "method": "GET",
                  "buyer": "buyerId2",
                  "request_id": "request_id",
                  "uri": "uri",
                  "path": "path",
                  "request": {
                    "age": 1
                  },
                  "response": {
                    "age": 2
                  }
                }
              ]
            """);
    return clientEvent;
  }

  private void addApiLogActivity(String envId) {

    apiActivityLogService.onEvent(envId, UUID.randomUUID().toString(), createEvent());
  }

  @Test
  void saveApiActivityLog() {

    var envId = UUID.randomUUID();
    var now = ZonedDateTime.parse(NOW_WITH_TIMEZONE);
    addApiLogActivity(envId.toString());
    var result = this.apiActivityLogRepository.findAll();
    Assertions.assertEquals(result.size(), 1);

    // when run it again
    deleteLogService.runIt();

    result = this.apiActivityLogRepository.findAll();
    Assertions.assertEquals(result.size(), 0);
  }
}
