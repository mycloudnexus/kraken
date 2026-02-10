package com.consoleconnect.kraken.operator.core.repo;

import com.consoleconnect.kraken.operator.core.entity.MgmtEventEntity;
import java.util.List;
import java.util.Optional;
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

  Optional<MgmtEventEntity> findFirstByEventTypeAndStatus(String eventType, String status);

  Optional<MgmtEventEntity> findFirstByEventTypeAndStatusOrderByCreatedAtAsc(
      String eventType, String status);

  @Query(
      value =
          "SELECT EXISTS ("
              + "select * from kraken_mgmt_event e WHERE"
              + " e.status in :status "
              + " AND e.event_type = :eventType "
              + " AND e.payload->>'envId' = :envId "
              + " AND e.payload->>'startTime' = :startTime "
              + " AND e.payload->>'endTime' = :endTime "
              + ")",
      nativeQuery = true)
  boolean existsBy(
      @Param("status") List<String> status,
      @Param("envId") String envId,
      @Param("startTime") String startTime,
      @Param("endTime") String endTime,
      @Param("eventType") String eventType);
}
