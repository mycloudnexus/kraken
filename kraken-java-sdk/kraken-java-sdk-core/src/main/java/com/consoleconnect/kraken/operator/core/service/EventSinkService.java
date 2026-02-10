package com.consoleconnect.kraken.operator.core.service;

import static com.consoleconnect.kraken.operator.core.enums.MgmtEventType.CLIENT_APP_VERSION_UPGRADE_RESULT;

import com.consoleconnect.kraken.operator.core.dto.UnifiedAssetDto;
import com.consoleconnect.kraken.operator.core.entity.MgmtEventEntity;
import com.consoleconnect.kraken.operator.core.enums.EnvNameEnum;
import com.consoleconnect.kraken.operator.core.enums.EventStatusType;
import com.consoleconnect.kraken.operator.core.enums.MgmtEventType;
import com.consoleconnect.kraken.operator.core.enums.UpgradeResultEventEnum;
import com.consoleconnect.kraken.operator.core.event.AppVersionUpgradeResultEvent;
import com.consoleconnect.kraken.operator.core.event.TemplateUpgradeResultEvent;
import com.consoleconnect.kraken.operator.core.repo.MgmtEventRepository;
import com.consoleconnect.kraken.operator.core.toolkit.Constants;
import com.consoleconnect.kraken.operator.core.toolkit.JsonToolkit;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
@Slf4j
public class EventSinkService {
  private final MgmtEventRepository eventRepository;

  @Transactional(rollbackFor = Exception.class)
  public void save(MgmtEventEntity event) {
    eventRepository.save(event);
  }

  public List<MgmtEventEntity> findByPage(
      List<MgmtEventType> eventTypes, EventStatusType eventStatus, PageRequest pageRequest) {
    return eventRepository
        .findByEventTypeInAndStatus(
            eventTypes.stream().map(MgmtEventType::name).toList(), eventStatus.name(), pageRequest)
        .getContent();
  }

  public void reportTemplateUpgradeResult(
      UnifiedAssetDto templateDto,
      UpgradeResultEventEnum resultEventEnum,
      Consumer<TemplateUpgradeResultEvent> consumer) {
    log.info(
        "[{}][{}][{}] Report template version upgrade result, event category: {}",
        Constants.LOG_FIELD_SYNC_EVENT,
        Constants.LOG_FIELD_REPORT_UPGRADE,
        Constants.LOG_FIELD_TEMPLATE,
        resultEventEnum);
    MgmtEventEntity mgmtEventEntity = new MgmtEventEntity();
    mgmtEventEntity.setEventType(MgmtEventType.TEMPLATE_UPGRADE_RESULT.name());
    TemplateUpgradeResultEvent receivedEvent = new TemplateUpgradeResultEvent();
    receivedEvent.setTemplateKey(templateDto.getMetadata().getKey());
    receivedEvent.setResultEventType(resultEventEnum);
    if (consumer != null) {
      consumer.accept(receivedEvent);
    }
    mgmtEventEntity.setPayload(JsonToolkit.toJson(receivedEvent));
    mgmtEventEntity.setStatus(EventStatusType.WAIT_TO_SEND.name());
    eventRepository.save(mgmtEventEntity);
  }

  public void reportKrakenVersionUpgradeResult(
      EnvNameEnum envName, String appVersion, ZonedDateTime upgradeAt) {
    log.info(
        "[{}][{}] Report kraken version upgrade result, envName: {}, appVersion: {}",
        Constants.LOG_FIELD_SYNC_EVENT,
        Constants.LOG_FIELD_REPORT_UPGRADE,
        Constants.LOG_FIELD_APP_VERSION,
        envName,
        appVersion);
    MgmtEventEntity mgmtEventEntity = new MgmtEventEntity();
    mgmtEventEntity.setEventType(CLIENT_APP_VERSION_UPGRADE_RESULT.name());
    AppVersionUpgradeResultEvent appVersionUpgradeResultEvent = new AppVersionUpgradeResultEvent();
    mgmtEventEntity.setPayload(appVersionUpgradeResultEvent);
    appVersionUpgradeResultEvent.setUpgradeAt(upgradeAt);
    appVersionUpgradeResultEvent.setAppVersion(appVersion);
    appVersionUpgradeResultEvent.setEnvName(envName);
    mgmtEventEntity.setStatus(EventStatusType.WAIT_TO_SEND.name());
    eventRepository.save(mgmtEventEntity);
  }
}
