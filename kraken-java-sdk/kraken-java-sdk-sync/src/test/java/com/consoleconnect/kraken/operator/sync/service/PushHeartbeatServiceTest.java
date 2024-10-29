package com.consoleconnect.kraken.operator.sync.service;

import static org.mockito.Mockito.*;

import com.consoleconnect.kraken.operator.core.enums.ResourceRoleEnum;
import com.consoleconnect.kraken.operator.core.model.HttpResponse;
import com.consoleconnect.kraken.operator.core.toolkit.DateTime;
import com.consoleconnect.kraken.operator.data.entity.InstanceHeartbeatEntity;
import com.consoleconnect.kraken.operator.data.repo.HeartbeatRepository;
import com.consoleconnect.kraken.operator.sync.CustomConfig;
import com.consoleconnect.kraken.operator.test.AbstractIntegrationTest;
import com.consoleconnect.kraken.operator.test.MockIntegrationTest;
import java.util.List;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ContextConfiguration;

@MockIntegrationTest
@ContextConfiguration(classes = {CustomConfig.class})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PushHeartbeatServiceTest extends AbstractIntegrationTest {

  @SpyBean private PushHeartbeatService pushHeartbeatService;

  @Autowired private HeartbeatRepository heartbeatRepository;

  @Test
  void givenClientHeartbeatData_whenPush_thenEventReported() {

    // mock
    doReturn(HttpResponse.ok(null)).when(pushHeartbeatService).pushEvent(any());

    // given
    InstanceHeartbeatEntity instance1 = new InstanceHeartbeatEntity();
    instance1.setInstanceId("10.0.0.1");
    instance1.setRole(ResourceRoleEnum.WORKER.name());
    instance1.setCreatedAt(DateTime.nowInUTC());
    instance1.setUpdatedAt(DateTime.nowInUTC());
    heartbeatRepository.save(instance1);

    InstanceHeartbeatEntity instance2 = new InstanceHeartbeatEntity();
    instance2.setInstanceId("10.0.0.2");
    instance1.setRole(ResourceRoleEnum.WORKER.name());
    instance2.setCreatedAt(DateTime.nowInUTC());
    instance2.setUpdatedAt(DateTime.nowInUTC());

    heartbeatRepository.saveAll(List.of(instance1, instance2));

    // when
    pushHeartbeatService.runIt();

    // then
    verify(pushHeartbeatService, times(1)).pushEvent(Mockito.any());

    // when run it again
    pushHeartbeatService.runIt();

    // then no more heartbeat to push
    verify(pushHeartbeatService, times(1)).pushEvent(Mockito.any());
  }
}
