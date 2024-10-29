package com.consoleconnect.kraken.operator.sync.service;

import com.consoleconnect.kraken.operator.core.entity.MgmtEventEntity;
import com.consoleconnect.kraken.operator.core.enums.EventStatusType;
import com.consoleconnect.kraken.operator.core.enums.MgmtEventType;
import com.consoleconnect.kraken.operator.core.event.TemplateUpgradeResultEvent;
import com.consoleconnect.kraken.operator.core.model.HttpResponse;
import com.consoleconnect.kraken.operator.core.repo.MgmtEventRepository;
import com.consoleconnect.kraken.operator.core.service.EventSinkService;
import com.consoleconnect.kraken.operator.sync.CustomConfig;
import com.consoleconnect.kraken.operator.sync.model.SyncProperty;
import com.consoleconnect.kraken.operator.test.AbstractIntegrationTest;
import com.consoleconnect.kraken.operator.test.MockIntegrationTest;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.ContextConfiguration;

@MockIntegrationTest
@ContextConfiguration(classes = {CustomConfig.class})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PushMgmtEventServiceTest extends AbstractIntegrationTest {
  public static final String MEF_SONATA_RELEASE_1_1_0 = "mef.sonata.release@1.1.0";
  @SpyBean private PushMgmtEventService pushMgmtEventService;
  @Autowired EventSinkService eventSinkService;
  @Autowired MgmtEventRepository eventRepository;
  @Autowired SyncProperty syncProperty;

  @Test
  @Order(1)
  void givenMgmtEvent_whenPushMgmtEvent_thenReturnData() {
    TemplateUpgradeResultEvent receivedEvent = new TemplateUpgradeResultEvent();
    receivedEvent.setProductReleaseKey(UUID.randomUUID().toString());
    receivedEvent.setPublishAssetKey(MEF_SONATA_RELEASE_1_1_0);
    receivedEvent.setReceivedAt(ZonedDateTime.now());
    MgmtEventEntity mgmtEvent = new MgmtEventEntity();
    mgmtEvent.setEventType(MgmtEventType.TEMPLATE_UPGRADE_RESULT.name());
    mgmtEvent.setStatus(EventStatusType.WAIT_TO_SEND.name());
    mgmtEvent.setPayload(receivedEvent);
    eventSinkService.save(mgmtEvent);
    Mockito.doReturn(HttpResponse.ok(null))
        .when(pushMgmtEventService)
        .curl(
            Mockito.eq(HttpMethod.POST),
            Mockito.eq(syncProperty.getMgmtPlane().getMgmtPushEventEndpoint()),
            Mockito.any(),
            Mockito.any(ParameterizedTypeReference.class));
    pushMgmtEventService.pushMgmtEvent();
    Optional<MgmtEventEntity> mgmtEventEntity = eventRepository.findById(mgmtEvent.getId());
    Assertions.assertTrue(mgmtEventEntity.isPresent());
    Assertions.assertEquals(EventStatusType.DONE.name(), mgmtEventEntity.get().getStatus());
  }
}
