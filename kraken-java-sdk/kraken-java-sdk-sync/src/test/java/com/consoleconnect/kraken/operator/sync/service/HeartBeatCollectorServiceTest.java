package com.consoleconnect.kraken.operator.sync.service;

import com.consoleconnect.kraken.operator.core.entity.EnvironmentClientEntity;
import com.consoleconnect.kraken.operator.core.entity.MgmtEventEntity;
import com.consoleconnect.kraken.operator.core.enums.*;
import com.consoleconnect.kraken.operator.core.repo.EnvironmentClientRepository;
import com.consoleconnect.kraken.operator.core.repo.MgmtEventRepository;
import com.consoleconnect.kraken.operator.core.toolkit.IpUtils;
import com.consoleconnect.kraken.operator.sync.CustomConfig;
import com.consoleconnect.kraken.operator.test.AbstractIntegrationTest;
import com.consoleconnect.kraken.operator.test.MockIntegrationTest;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;

@MockIntegrationTest
@ContextConfiguration(classes = {CustomConfig.class})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class HeartBeatCollectorServiceTest extends AbstractIntegrationTest {
  @Autowired private HeartBeatCollectorService heartBeatCollectorService;
  @Autowired private EnvironmentClientRepository environmentClientRepository;
  @Autowired private MgmtEventRepository mgmtEventRepository;

  @Test
  void givenHeartBeats_whenFlowToMgmtEvent_thenShouldSuccess() {
    ZonedDateTime now = ZonedDateTime.now();
    EnvironmentClientEntity entity1 = createControlPlaneNode(now);
    EnvironmentClientEntity entity2 = createDataPlaneNode(now);
    environmentClientRepository.saveAllAndFlush(List.of(entity1, entity2));

    heartBeatCollectorService.runIt();

    Page<MgmtEventEntity> mgmtEventEntityPage =
        mgmtEventRepository.search(
            MgmtEventType.CLIENT_HEART_BEAT.name(),
            EventStatusType.WAIT_TO_SEND.name(),
            PageRequest.of(0, 10));
    Assertions.assertFalse(mgmtEventEntityPage.isEmpty());
  }

  private EnvironmentClientEntity createControlPlaneNode(ZonedDateTime now) {
    EnvironmentClientEntity entity = new EnvironmentClientEntity();
    entity.setEnvId(EnvNameEnum.UNDEFINED.name());
    entity.setEnvName(EnvNameEnum.UNDEFINED.name());
    entity.setKind(ClientReportTypeEnum.HEARTBEAT_CONTROL_PLANE.name());
    entity.setClientKey("10.0.0.3");
    entity.setFqdn(IpUtils.getFQDN());
    entity.setRole(ResourceRoleEnum.WORKER.name());
    entity.setCreatedAt(now);
    entity.setStatus(HttpStatus.OK.name());
    entity.setUpdatedAt(now);
    return entity;
  }

  private EnvironmentClientEntity createDataPlaneNode(ZonedDateTime now) {
    EnvironmentClientEntity entity = new EnvironmentClientEntity();
    entity.setEnvId(UUID.randomUUID().toString());
    entity.setEnvName(EnvNameEnum.STAGE.name());
    entity.setKind(ClientReportTypeEnum.HEARTBEAT.name());
    entity.setClientKey("10.0.0.4");
    entity.setFqdn(IpUtils.getFQDN());
    entity.setRole(ResourceRoleEnum.WORKER.name());
    entity.setCreatedAt(now);
    entity.setStatus(HttpStatus.OK.name());
    entity.setUpdatedAt(now);
    return entity;
  }
}
