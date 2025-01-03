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
class ApiActivityLogServiceAtControlPlaneTest extends AbstractIntegrationTest {
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

  @Test
  void receiveClientApiActivityLog() {
    this.insertLogWithoutSubTable();
    var envId = UUID.randomUUID();
    var now = ZonedDateTime.parse(NOW_WITH_TIMEZONE);
    addApiLogActivity(envId.toString());

    var apiLog = this.apiActivityLogRepository.findAll();
    var apiLogBody = this.apiActivityLogBodyRepository.findAll();
    var toMigrate =
        this.apiActivityLogRepository
            .findAllByMigrateStatus(PageRequest.of(0, Integer.MAX_VALUE))
            .getContent();
    Assertions.assertEquals(1, toMigrate.size());

    Assertions.assertEquals(2, apiLog.size());
    Assertions.assertEquals(1, apiLogBody.size());
    assertRequestAndResponse();
  }

  private void assertRequestAndResponse() {
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

  @Test
  void migrateExistedData() {
    this.receiveClientApiActivityLog();
    AppConfig.AchieveApiActivityLogConf achieveApiActivityLogConf =
        new AppConfig.AchieveApiActivityLogConf();
    achieveApiActivityLogConf.setLogKind(LogKindEnum.CONTROL_PLANE);
    this.apiActivityLogService.migrateApiLog(achieveApiActivityLogConf);
    var apiLog = this.apiActivityLogRepository.findAll();
    var apiLogBody = this.apiActivityLogBodyRepository.findAll();
    var toMigrate =
        this.apiActivityLogRepository
            .findAllByMigrateStatus(PageRequest.of(0, Integer.MAX_VALUE))
            .getContent();
    Assertions.assertEquals(2, apiLog.size());
    Assertions.assertEquals(2, apiLogBody.size());

    Assertions.assertEquals(0, toMigrate.size());
    assertRequestAndResponse();
  }

  @Test
  void achieveApiActivityLog() {
    this.migrateExistedData();

    AppConfig.AchieveApiActivityLogConf achieveApiActivityLogConf =
        new AppConfig.AchieveApiActivityLogConf();
    achieveApiActivityLogConf.setAchieveScope(AchieveScopeEnum.DETAIL);
    achieveApiActivityLogConf.setMonth(-1);
    achieveApiActivityLogConf.setProtocol("GET");
    achieveApiActivityLogConf.setLogKind(LogKindEnum.CONTROL_PLANE);
    apiActivityLogService.achieveApiActivityLog(achieveApiActivityLogConf);

    Assertions.assertEquals(
        0,
        this.apiActivityLogRepository
            .listExpiredApiLog(
                achieveApiActivityLogConf.toAchieve(),
                LifeStatusEnum.LIVE,
                achieveApiActivityLogConf.getProtocol(),
                PageRequest.of(0, Integer.MAX_VALUE))
            .getContent()
            .size());

    var list =
        this.apiActivityLogRepository
            .listExpiredApiLog(
                achieveApiActivityLogConf.toAchieve(),
                LifeStatusEnum.ACHIEVED,
                achieveApiActivityLogConf.getProtocol(),
                PageRequest.of(0, 20))
            .getContent();
    Assertions.assertEquals(1, list.size());
    list.forEach(
        x -> {
          Assertions.assertNull(x.getRawResponse());
          Assertions.assertNull(x.getRawRequest());
        });

    Assertions.assertEquals(1, this.apiActivityLogBodyRepository.findAll().size());
  }

  @Test
  void abc() {

    this.apiActivityLogService.achieveApiActivityLog(null);
    AppConfig.AchieveApiActivityLogConf achieveApiActivityLogConf =
        new AppConfig.AchieveApiActivityLogConf();
    achieveApiActivityLogConf.setLogKind(LogKindEnum.MANAGE_PLANE);
    this.apiActivityLogService.achieveApiActivityLog(achieveApiActivityLogConf);

    this.apiActivityLogService.migrateApiLog(null);
    this.apiActivityLogService.migrateApiLog(achieveApiActivityLogConf);

    Assertions.assertNotNull(achieveApiActivityLogConf);
  }
}
