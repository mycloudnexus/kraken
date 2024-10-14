package com.consoleconnect.kraken.operator.controller.repo;

import com.consoleconnect.kraken.operator.controller.entity.MgmtEventEntity;
import com.consoleconnect.kraken.operator.core.enums.EventStatusType;
import com.consoleconnect.kraken.operator.core.enums.MgmtEventType;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

public interface EventRepository
    extends PagingAndSortingRepository<MgmtEventEntity, UUID>,
        JpaRepository<MgmtEventEntity, UUID> {

  @Query(
      value =
          "select e from #{#entityName} e "
              + " where ( (:eventType) is null or  e.eventType = :eventType )"
              + " and ( (:status) is null or  e.status = :status )")
  Page<MgmtEventEntity> search(
      @Param("eventType") MgmtEventType eventType,
      @Param("status") EventStatusType status,
      Pageable pageable);
}
