package com.consoleconnect.kraken.operator.core.repo;

import com.consoleconnect.kraken.operator.core.entity.MgmtEventEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

public interface MgmtEventRepository
    extends PagingAndSortingRepository<MgmtEventEntity, UUID>,
        JpaRepository<MgmtEventEntity, UUID>,
        JpaSpecificationExecutor<MgmtEventEntity> {

  @Query(
      value =
          "select e from #{#entityName} e "
              + " where ( (:eventType) is null or  e.eventType = :eventType )"
              + " and ( (:status) is null or  e.status = :status )")
  Page<MgmtEventEntity> search(
      @Param("eventType") String eventType, @Param("status") String status, Pageable pageable);

  Page<MgmtEventEntity> findByEventTypeInAndStatus(
      List<String> mgmtEventTypeList, String eventStatusType, Pageable pageable);
}
