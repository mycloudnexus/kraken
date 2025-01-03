package com.consoleconnect.kraken.operator.core.service;

import com.consoleconnect.kraken.operator.core.CustomConfig;
import com.consoleconnect.kraken.operator.core.client.ClientEvent;
import com.consoleconnect.kraken.operator.core.client.ClientEventTypeEnum;
import com.consoleconnect.kraken.operator.core.config.AppConfig;
import com.consoleconnect.kraken.operator.core.entity.ApiActivityLogEntity;
import com.consoleconnect.kraken.operator.core.enums.AchieveScopeEnum;
import com.consoleconnect.kraken.operator.core.enums.LifeStatusEnum;
import com.consoleconnect.kraken.operator.core.enums.LogKindEnum;
import com.consoleconnect.kraken.operator.core.repo.ApiActivityLogBodyRepository;
import com.consoleconnect.kraken.operator.core.repo.ApiActivityLogRepository;
import com.consoleconnect.kraken.operator.core.toolkit.JsonToolkit;
import com.consoleconnect.kraken.operator.test.AbstractIntegrationTest;
import com.consoleconnect.kraken.operator.test.MockIntegrationTest;
import java.time.ZonedDateTime;
import java.util.UUID;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ContextConfiguration;

@Slf4j
@MockIntegrationTest
@ContextConfiguration(classes = {CustomConfig.class})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ApiActivityLogServiceAtDataPlaneTest extends AbstractIntegrationTest {
  @Autowired ApiActivityLogRepository apiActivityLogRepository;

  @SpyBean private ApiActivityLogService apiActivityLogService;
  @SpyBean private ApiActivityLogBodyRepository apiActivityLogBodyRepository;
  public static final String NOW_WITH_TIMEZONE = "2023-10-24T05:00:00+02:00";
  public static final String REQUEST_ID = "requestId";

  @BeforeEach
  void clearDb() {
    var list = this.apiActivityLogRepository.findAll();
    list.forEach(
        x -> {
          x.setApiLogBodyEntity(null);
        });
    this.apiActivityLogRepository.saveAll(list);
    this.apiActivityLogBodyRepository.deleteAll();
    this.apiActivityLogRepository.deleteAll();
  }

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
  void insertLogWithoutSubTable() {

    ApiActivityLogEntity entity = new ApiActivityLogEntity();
    entity.setRequestId(EXISTED_REQUEST_ID);
    entity.setCallSeq(0);
    entity.setMethod("POST");
    entity.setBuyer("buy");
    entity.setUri("uri");
    entity.setPath("path");
    entity.setRawRequest("""
            {
            "age":91
            }
            """);
    entity.setRawResponse("""
            {
            "age":92
            }
            """);
    this.apiActivityLogRepository.save(entity);

    var toMigrate =
        this.apiActivityLogRepository
            .findAllByMigrateStatus(PageRequest.of(0, Integer.MAX_VALUE))
            .getContent();
    Assertions.assertEquals(1, toMigrate.size());
  }

  private void assertRequestAndResponse() {
    BodyAge requestAge = new BodyAge();
    requestAge.setAge(1);
    BodyAge responseAge = new BodyAge();
    responseAge.setAge(2);

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

  @Test
  void migrateExistedData() {
    this.insertLogWithoutSubTable();
    AppConfig.AchieveApiActivityLogConf achieveApiActivityLogConf =
        new AppConfig.AchieveApiActivityLogConf();
    achieveApiActivityLogConf.setLogKind(LogKindEnum.DATA_PLANE);

    this.apiActivityLogService.migrateApiLog(achieveApiActivityLogConf);
    var apiLog = this.apiActivityLogRepository.findAll();
    var apiLogBody = this.apiActivityLogBodyRepository.findAll();
    var toMigrate =
        this.apiActivityLogRepository
            .findAllByMigrateStatus(PageRequest.of(0, Integer.MAX_VALUE))
            .getContent();
    Assertions.assertEquals(1, apiLog.size());
    Assertions.assertEquals(1, apiLogBody.size());

    Assertions.assertEquals(0, toMigrate.size());
    assertRequestAndResponse();
  }

  @Test
  void achieveApiActivityLog() {
    this.migrateExistedData();
    ZonedDateTime toAchieve = ZonedDateTime.now().plusYears(100);

    AppConfig.AchieveApiActivityLogConf achieveApiActivityLogConf =
        new AppConfig.AchieveApiActivityLogConf();
    achieveApiActivityLogConf.setAchieveScope(AchieveScopeEnum.BASIC);
    achieveApiActivityLogConf.setMonth(-1);
    achieveApiActivityLogConf.setLogKind(LogKindEnum.DATA_PLANE);
    apiActivityLogService.achieveApiActivityLog(achieveApiActivityLogConf);

    Assertions.assertEquals(
        0,
        this.apiActivityLogRepository
            .listExpiredApiLog(
                toAchieve,
                LifeStatusEnum.LIVE,
                achieveApiActivityLogConf.getProtocol(),
                PageRequest.of(0, Integer.MAX_VALUE))
            .getContent()
            .size());

    var list = this.apiActivityLogRepository.findAll();
    list.forEach(
        x -> {
          Assertions.assertNotNull(x.getRawResponse());
          Assertions.assertNotNull(x.getRawRequest());
        });

    Assertions.assertEquals(0, list.size());
    Assertions.assertEquals(0, this.apiActivityLogBodyRepository.findAll().size());
  }
}
