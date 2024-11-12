package com.consoleconnect.kraken.operator.sync.service;

import static com.consoleconnect.kraken.operator.core.enums.MgmtEventType.CLIENT_SYSTEM_INFO;

import com.consoleconnect.kraken.operator.core.entity.MgmtEventEntity;
import com.consoleconnect.kraken.operator.core.entity.SystemInfoEntity;
import com.consoleconnect.kraken.operator.core.enums.EventStatusType;
import com.consoleconnect.kraken.operator.core.repo.MgmtEventRepository;
import com.consoleconnect.kraken.operator.core.repo.SystemInfoRepository;
import com.consoleconnect.kraken.operator.sync.CustomConfig;
import com.consoleconnect.kraken.operator.test.AbstractIntegrationTest;
import com.consoleconnect.kraken.operator.test.MockIntegrationTest;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ContextConfiguration;

@MockIntegrationTest
@ContextConfiguration(classes = {CustomConfig.class})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PushKrakenVersionServiceTest extends AbstractIntegrationTest {

  @Autowired private PushKrakenVersionService pushKrakenVersionService;
  @Autowired private SystemInfoRepository systemInfoRepository;
  @Autowired private MgmtEventRepository mgmtEventRepository;

  @Test
  void givenSystemInfoEvent_whenRun_thenPushSuccessfully() {

    // given exist event
    SystemInfoEntity entity = new SystemInfoEntity();
    entity.setKey("CONTROL_PLANE");
    entity.setControlProductVersion("1.0.0");
    entity.setProductKey("Grace");
    entity.setStageAppVersion("1.0.0");
    entity.setProductionAppVersion("1.0.0");
    systemInfoRepository.save(entity);
    // when
    pushKrakenVersionService.runIt();
    // then
    Page<MgmtEventEntity> eventEntities =
        mgmtEventRepository.findByEventTypeInAndStatus(
            List.of(CLIENT_SYSTEM_INFO.name()),
            EventStatusType.WAIT_TO_SEND.name(),
            Pageable.ofSize(1));
    Assertions.assertEquals(
        eventEntities.getContent().get(0).getResourceId(), entity.getId().toString());
  }
}
