package com.consoleconnect.kraken.operator.controller.service;

import com.consoleconnect.kraken.operator.core.entity.MgmtEventEntity;
import com.consoleconnect.kraken.operator.core.enums.EventStatusType;
import com.consoleconnect.kraken.operator.core.enums.MgmtEventType;
import com.consoleconnect.kraken.operator.core.repo.MgmtEventRepository;
import com.consoleconnect.kraken.operator.core.toolkit.Paging;
import com.consoleconnect.kraken.operator.core.toolkit.PagingHelper;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class EventService {
  private final ApplicationEventPublisher publisher;
  private final MgmtEventRepository eventRepository;

  public EventService(ApplicationEventPublisher publisher, MgmtEventRepository eventRepository) {
    this.publisher = publisher;
    this.eventRepository = eventRepository;
  }

  public MgmtEventEntity publishEvent(MgmtEventType eventType, String userId, String resourceId) {
    log.info("publish event: {}", eventType);
    MgmtEventEntity entity = new MgmtEventEntity();
    entity.setEventType(eventType.name());
    entity.setCreatedBy(userId);
    entity.setStatus(EventStatusType.ACK.name());
    entity.setResourceId(resourceId);
    publisher.publishEvent(entity);
    return entity;
  }

  public Paging<MgmtEventEntity> search(String eventType, String status, PageRequest pageRequest) {
    return PagingHelper.toPaging(eventRepository.search(eventType, status, pageRequest), t -> t);
  }

  public Optional<MgmtEventEntity> findById(String id) {
    return eventRepository.findById(UUID.fromString(id));
  }

  @Transactional
  public void updateStatus(List<String> ids, String status) {
    if (ids == null) {
      return;
    }
    ids.forEach(
        id -> {
          Optional<MgmtEventEntity> eventOpt = eventRepository.findById(UUID.fromString(id));
          eventOpt.ifPresent(
              event -> {
                event.setStatus(EventStatusType.valueOf(status).name());
                eventRepository.save(event);
              });
        });
  }
}
