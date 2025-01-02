package com.consoleconnect.kraken.operator.controller.audit;

import com.consoleconnect.kraken.operator.controller.model.EndpointAudit;
import java.time.ZonedDateTime;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface EndpointAuditRepository
    extends PagingAndSortingRepository<EndpointAuditEntity, UUID>,
        CrudRepository<EndpointAuditEntity, UUID> {

  @Query(
          "select e.userId, e.email, e.name, e.path, e.method, e.action, "
                  + " e.resource, e.resourceId, e.statusCode, e.createdBy, e.createdAt, e.updatedBy, e.updatedAt "
                  + " from #{#entityName} e "
                  + " where ( :#{#query.getUserId()}  is null or e.userId = :#{#query.getUserId()} ) "
                  + " and ( :#{#query.getResource()}  is null or e.resource = :#{#query.getResource()} ) "
                  + " and ( :#{#query.getResourceId()}  is null or e.resourceId = :#{#query.getResourceId()} ) "
                  + " and ( :#{#query.getAction()}  is null or e.action = :#{#query.getAction()} ) "
                  + " and ( cast(:startTime as date) is null or e.createdAt >= :startTime )"
                  + " and ( cast(:endTime as date) is null or e.createdAt <= :endTime )")
  @Transactional(readOnly = true)
  Page<Object> search(
      @Param("query") EndpointAudit query,
      @Param("startTime") ZonedDateTime startTime,
      @Param("endTime") ZonedDateTime endTime,
      Pageable pageable);
}
