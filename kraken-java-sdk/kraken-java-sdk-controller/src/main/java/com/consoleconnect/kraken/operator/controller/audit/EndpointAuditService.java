package com.consoleconnect.kraken.operator.controller.audit;

import com.consoleconnect.kraken.operator.controller.model.EndpointAudit;
import com.consoleconnect.kraken.operator.core.toolkit.*;
import java.time.ZonedDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EndpointAuditService {

  private final EndpointAuditRepository endpointAuditRepository;

  public EndpointAuditService(EndpointAuditRepository endpointAuditRepository) {
    this.endpointAuditRepository = endpointAuditRepository;
  }

  public Paging<EndpointAuditEntity> search(
      EndpointAudit query, ZonedDateTime startTime, ZonedDateTime endTime, int page, int size) {
    Page<EndpointAuditEntity> data =
        endpointAuditRepository.search(
            query, startTime, endTime, PagingHelper.toPageable(page, size));
    return PagingHelper.toPaging(data, x -> x);
  }

  public Paging<EndpointAuditEntity> searchByResourceId(String resourceId, int page, int size) {
    EndpointAudit query = new EndpointAudit();
    query.setResourceId(resourceId);
    Page<EndpointAuditEntity> data =
        endpointAuditRepository.search(query, null, null, PagingHelper.toPageable(page, size));
    return PagingHelper.toPaging(data, x -> x);
  }
}
