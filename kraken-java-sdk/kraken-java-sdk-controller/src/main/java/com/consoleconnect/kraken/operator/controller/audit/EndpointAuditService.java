package com.consoleconnect.kraken.operator.controller.audit;

import com.consoleconnect.kraken.operator.controller.model.EndpointAudit;
import com.consoleconnect.kraken.operator.core.exception.KrakenException;
import com.consoleconnect.kraken.operator.core.service.UUIDWrapper;
import com.consoleconnect.kraken.operator.core.toolkit.*;
import java.time.ZonedDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EndpointAuditService implements UUIDWrapper {

  private final EndpointAuditRepository endpointAuditRepository;

  public EndpointAuditService(EndpointAuditRepository endpointAuditRepository) {
    this.endpointAuditRepository = endpointAuditRepository;
  }

  public Paging<EndpointAuditEntity> search(
      EndpointAudit query,
      ZonedDateTime startTime,
      ZonedDateTime endTime,
      int page,
      int size,
      boolean liteSearch) {
    return searchInternal(query, startTime, endTime, page, size, liteSearch);
  }

  public Paging<EndpointAuditEntity> searchByResourceId(
      String resourceId, int page, int size, boolean liteSearch) {
    EndpointAudit query = new EndpointAudit();
    query.setResourceId(resourceId);
    return searchInternal(query, null, null, page, size, liteSearch);
  }

  private Paging<EndpointAuditEntity> searchInternal(
      EndpointAudit query,
      ZonedDateTime startTime,
      ZonedDateTime endTime,
      int page,
      int size,
      boolean liteSearch) {
    Page<EndpointAuditEntity> data =
        endpointAuditRepository.search(
            query, startTime, endTime, PagingHelper.toPageable(page, size));
    return liteSearch
        ? PagingHelper.toPage(
            data.stream().map(this::removeDetails).toList(), page, size, data.getTotalElements())
        : PagingHelper.toPaging(data, x -> x);
  }

  private EndpointAuditEntity removeDetails(EndpointAuditEntity entity) {
    entity.setRequest(null);
    entity.setResponse(null);
    return entity;
  }

  public EndpointAuditEntity findOne(String id) {
    return getUUID(id)
        .map(endpointAuditRepository::findById)
        .flatMap(x -> x)
        .orElseThrow(() -> KrakenException.notFound("Asset not found,key=" + id));
  }
}
