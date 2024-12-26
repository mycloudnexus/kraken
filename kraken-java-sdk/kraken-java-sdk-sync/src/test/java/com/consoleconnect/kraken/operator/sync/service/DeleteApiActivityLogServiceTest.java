package com.consoleconnect.kraken.operator.sync.service;

import com.consoleconnect.kraken.operator.core.client.ClientEvent;
import com.consoleconnect.kraken.operator.core.client.ClientEventTypeEnum;
import com.consoleconnect.kraken.operator.core.enums.LogKindEnum;
import com.consoleconnect.kraken.operator.core.repo.ApiActivityLogBodyRepository;
import com.consoleconnect.kraken.operator.core.repo.ApiActivityLogRepository;
import com.consoleconnect.kraken.operator.core.service.ApiActivityLogService;
import com.consoleconnect.kraken.operator.sync.CustomConfig;
import com.consoleconnect.kraken.operator.test.AbstractIntegrationTest;
import com.consoleconnect.kraken.operator.test.MockIntegrationTest;
import java.time.ZonedDateTime;
import java.util.UUID;
import org.junit.jupiter.api.*;
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
  @SpyBean private ApiActivityLogBodyRepository apiActivityLogBodyRepository;
  public static final String NOW_WITH_TIMEZONE = "2023-10-24T05:00:00+02:00";

  @BeforeEach
  void init() {
    this.apiActivityLogRepository.deleteAll();
    this.apiActivityLogBodyRepository.deleteAll();
  }

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

    apiActivityLogService.receiveClientLog(envId, UUID.randomUUID().toString(), createEvent());
  }

  @Test
  void deleteApiActivityLogInDataPlane() {

    var envId = UUID.randomUUID();
    var now = ZonedDateTime.parse(NOW_WITH_TIMEZONE);
    addApiLogActivity(envId.toString());

    Assertions.assertEquals(1, this.apiActivityLogRepository.findAll().size());
    Assertions.assertEquals(1, this.apiActivityLogBodyRepository.findAll().size());

    // when run it again
    apiActivityLogService.deleteApiLogAtDataPlane(LogKindEnum.DATA_PLANE, ZonedDateTime.now());

    Assertions.assertEquals(0, this.apiActivityLogRepository.findAll().size());
    Assertions.assertEquals(0, this.apiActivityLogBodyRepository.findAll().size());
  }

  @Test
  void deleteApiActivityLogInControlPlane() {

    var envId = UUID.randomUUID();
    var now = ZonedDateTime.parse(NOW_WITH_TIMEZONE);
    addApiLogActivity(envId.toString());
    var result = this.apiActivityLogRepository.findAll();
    Assertions.assertEquals(1, this.apiActivityLogRepository.findAll().size());
    Assertions.assertEquals(1, this.apiActivityLogBodyRepository.findAll().size());

    // when run it again
    apiActivityLogService.deleteApiLogAtDataPlane(LogKindEnum.CONTROL_PLANE, ZonedDateTime.now());

    Assertions.assertEquals(1, this.apiActivityLogRepository.findAll().size());
    Assertions.assertEquals(0, this.apiActivityLogBodyRepository.findAll().size());
  }
}
