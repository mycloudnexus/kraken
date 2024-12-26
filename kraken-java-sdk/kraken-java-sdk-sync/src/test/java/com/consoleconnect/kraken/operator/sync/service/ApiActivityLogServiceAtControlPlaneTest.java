package com.consoleconnect.kraken.operator.sync.service;

import com.consoleconnect.kraken.operator.core.client.ClientEvent;
import com.consoleconnect.kraken.operator.core.client.ClientEventTypeEnum;
import com.consoleconnect.kraken.operator.core.entity.ApiActivityLogEntity;
import com.consoleconnect.kraken.operator.core.enums.LogKindEnum;
import com.consoleconnect.kraken.operator.core.repo.ApiActivityLogBodyRepository;
import com.consoleconnect.kraken.operator.core.repo.ApiActivityLogRepository;
import com.consoleconnect.kraken.operator.core.service.ApiActivityLogService;
import com.consoleconnect.kraken.operator.core.toolkit.JsonToolkit;
import com.consoleconnect.kraken.operator.sync.CustomConfig;
import com.consoleconnect.kraken.operator.test.AbstractIntegrationTest;
import com.consoleconnect.kraken.operator.test.MockIntegrationTest;
import java.time.ZonedDateTime;
import java.util.UUID;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ContextConfiguration;

@MockIntegrationTest
@ContextConfiguration(classes = {CustomConfig.class})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ApiActivityLogServiceAtControlPlaneTest extends AbstractIntegrationTest {
  @Autowired ApiActivityLogRepository apiActivityLogRepository;
  @SpyBean private DeleteApiActivityLogService deleteLogService;
  @SpyBean private ApiActivityLogService apiActivityLogService;
  @SpyBean private ApiActivityLogBodyRepository apiActivityLogBodyRepository;
  public static final String NOW_WITH_TIMEZONE = "2023-10-24T05:00:00+02:00";
  public static final String REQUEST_ID = "requestId";

  @NoArgsConstructor
  @Getter
  @Setter
  @EqualsAndHashCode(of = "age")
  public static class BodyAge {
    private int age;
  }

  private static ClientEvent createClientEventWithBody() {
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
    apiActivityLogService.receiveClientLog(
        envId, UUID.randomUUID().toString(), createClientEventWithBody());
  }

  public static final String EXISTED_REQUEST_ID = "requestId_existed";

  @Test
  @Order(1)
  void insertLogWithoutSubTable() {
    ApiActivityLogEntity entity = new ApiActivityLogEntity();
    entity.setRequestId(EXISTED_REQUEST_ID);
    entity.setCallSeq(0);
    entity.setMethod("POST");
    entity.setBuyer("buy");
    entity.setUri("uri");
    entity.setPath("path");
    entity.setLogRequest("""
            {
            "age":91
            }
            """);
    entity.setLogResponse("""
            {
            "age":92
            }
            """);
    this.apiActivityLogRepository.save(entity);
  }

  @Test
  @Order(2)
  void receiveClientApiActivityLog() {
    var envId = UUID.randomUUID();
    var now = ZonedDateTime.parse(NOW_WITH_TIMEZONE);
    addApiLogActivity(envId.toString());

    var apiLog = this.apiActivityLogRepository.findAll();
    var apiLogBody = this.apiActivityLogBodyRepository.findAll();

    Assertions.assertEquals(2, apiLog.size());
    Assertions.assertEquals(1, apiLogBody.size());
    var newLog =
        this.apiActivityLogRepository.findByRequestIdAndCallSeq(REQUEST_ID, 0).orElse(null);
    BodyAge requestAge = new BodyAge();
    requestAge.setAge(1);
    Assertions.assertEquals(
        requestAge,
        JsonToolkit.fromJson(
            JsonToolkit.toJson(newLog.getApiLogBodyEntity().getRequest()), BodyAge.class));
    BodyAge responseAge = new BodyAge();
    responseAge.setAge(2);
    Assertions.assertEquals(
        responseAge,
        JsonToolkit.fromJson(
            JsonToolkit.toJson(newLog.getApiLogBodyEntity().getResponse()), BodyAge.class));

    var existedLog =
        this.apiActivityLogRepository.findByRequestIdAndCallSeq(EXISTED_REQUEST_ID, 0).orElse(null);
    requestAge.setAge(91);
    Assertions.assertEquals(
        requestAge,
        JsonToolkit.fromJson(
            JsonToolkit.toJson(existedLog.getApiLogBodyEntity().getRequest()), BodyAge.class));
    responseAge.setAge(92);
    Assertions.assertEquals(
        responseAge,
        JsonToolkit.fromJson(
            JsonToolkit.toJson(existedLog.getApiLogBodyEntity().getResponse()), BodyAge.class));
  }

  // @Test
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
