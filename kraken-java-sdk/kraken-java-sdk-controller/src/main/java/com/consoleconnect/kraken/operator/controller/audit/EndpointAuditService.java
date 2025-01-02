package com.consoleconnect.kraken.operator.controller.audit;

import com.consoleconnect.kraken.operator.controller.model.EndpointAudit;
import com.consoleconnect.kraken.operator.core.exception.KrakenException;
import com.consoleconnect.kraken.operator.core.service.UUIDWrapper;
import com.consoleconnect.kraken.operator.core.toolkit.*;
import java.time.ZonedDateTime;
import java.util.Optional;
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

  public Paging<Object> search(
      EndpointAudit query,
      ZonedDateTime startTime,
      ZonedDateTime endTime,
      int page,
      int size,
      boolean liteSearch) {
    Page<Object> data;
    if (liteSearch) {
      data =
          endpointAuditRepository.searchLite(
              query, startTime, endTime, PagingHelper.toPageable(page, size));
    } else {
      data =
          endpointAuditRepository.search(
              query, startTime, endTime, PagingHelper.toPageable(page, size));
    }
    return PagingHelper.toPaging(data, x -> x);
  }

  public Paging<Object> searchByResourceId(
      String resourceId, int page, int size, boolean liteSearch) {
    EndpointAudit query = new EndpointAudit();
    query.setResourceId(resourceId);
    Page<Object> data;
    if (liteSearch) {
      data =
          endpointAuditRepository.searchLite(
              query, null, null, PagingHelper.toPageable(page, size));
    } else {
      data = endpointAuditRepository.search(query, null, null, PagingHelper.toPageable(page, size));
    }
    return PagingHelper.toPaging(data, x -> x);
  }

  public Optional<EndpointAuditEntity> findOne(String id) {
    return getUUID(id)
        .map(endpointAuditRepository::findById)
        .orElseThrow(() -> KrakenException.notFound("Asset not found,key=" + id));
  }
}
