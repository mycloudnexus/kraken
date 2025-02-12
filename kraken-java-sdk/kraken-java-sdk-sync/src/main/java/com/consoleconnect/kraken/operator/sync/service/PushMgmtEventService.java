package com.consoleconnect.kraken.operator.sync.service;

import com.consoleconnect.kraken.operator.core.client.ClientEvent;
import com.consoleconnect.kraken.operator.core.client.ClientEventTypeEnum;
import com.consoleconnect.kraken.operator.core.entity.MgmtEventEntity;
import com.consoleconnect.kraken.operator.core.enums.EventStatusType;
import com.consoleconnect.kraken.operator.core.enums.MgmtEventType;
import com.consoleconnect.kraken.operator.core.mapper.MgmtEventMapper;
import com.consoleconnect.kraken.operator.core.service.EventSinkService;
import com.consoleconnect.kraken.operator.core.toolkit.AssetsConstants;
import com.consoleconnect.kraken.operator.core.toolkit.IpUtils;
import com.consoleconnect.kraken.operator.core.toolkit.JsonToolkit;
import com.consoleconnect.kraken.operator.sync.model.SyncProperty;
import com.consoleconnect.kraken.operator.sync.service.security.ExternalSystemTokenProvider;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@Slf4j
public class PushMgmtEventService extends KrakenServerConnector {
  private final EventSinkService eventSinkService;
  protected static final Map<MgmtEventType, ClientEventTypeEnum> TYPE_MAPPING =
      new EnumMap<>(MgmtEventType.class);

  static {
    TYPE_MAPPING.put(
        MgmtEventType.TEMPLATE_UPGRADE_RESULT, ClientEventTypeEnum.CLIENT_TEMPLATE_UPGRADE_RESULT);
    TYPE_MAPPING.put(MgmtEventType.CLIENT_HEART_BEAT, ClientEventTypeEnum.CLIENT_HEARTBEAT);
    TYPE_MAPPING.put(
        MgmtEventType.CLIENT_APP_VERSION_UPGRADE_RESULT,
        ClientEventTypeEnum.CLIENT_APP_VERSION_UPGRADE_RESULT);
    TYPE_MAPPING.put(MgmtEventType.CLIENT_SYSTEM_INFO, ClientEventTypeEnum.CLIENT_SYSTEM_INFO);
  }

  protected static final List<MgmtEventType> QUERY_EVENT_TYPES =
      TYPE_MAPPING.keySet().stream().toList();

  public PushMgmtEventService(
      SyncProperty appProperty,
      WebClient webClient,
      ExternalSystemTokenProvider externalSystemTokenProvider,
      EventSinkService eventSinkService) {
    super(appProperty, webClient, externalSystemTokenProvider);
    this.eventSinkService = eventSinkService;
  }

  @SchedulerLock(
      name = "pushMgmtEventLock",
      lockAtMostFor = "${app.cron-job.lock.at-most-for}",
      lockAtLeastFor = "${app.cron-job.lock.at-least-for}")
  @Scheduled(cron = "${app.cron-job.push-mgmt-event:-}")
  public void pushMgmtEvent() {
    List<MgmtEventEntity> mgmtEventEntities =
        eventSinkService.findByPage(
            QUERY_EVENT_TYPES,
            EventStatusType.WAIT_TO_SEND,
            PageRequest.of(0, 5, Sort.Direction.ASC, AssetsConstants.FIELD_CREATE_AT));
    if (CollectionUtils.isEmpty(mgmtEventEntities)) {
      return;
    }
    mgmtEventEntities.forEach(
        entity -> {
          ClientEvent clientEvent = new ClientEvent();
          clientEvent.setEventType(TYPE_MAPPING.get(MgmtEventType.valueOf(entity.getEventType())));
          clientEvent.setEventPayload(JsonToolkit.toJson(MgmtEventMapper.INSTANCE.map(entity)));
          clientEvent.setClientId(IpUtils.getHostAddress());
          curl(
              HttpMethod.POST,
              getAppProperty().getMgmtPlane().getMgmtPushEventEndpoint(),
              clientEvent,
              response -> {
                if (response.getCode() == HttpStatus.OK.value()) {
                  entity.setStatus(EventStatusType.DONE.name());
                  eventSinkService.save(entity);
                }
              });
        });
  }
}
