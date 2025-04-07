package com.consoleconnect.kraken.operator.core.repo;

import com.consoleconnect.kraken.operator.core.entity.ApiActivityLogEntity;
import com.consoleconnect.kraken.operator.core.enums.LifeStatusEnum;
import com.consoleconnect.kraken.operator.core.enums.SyncStatusEnum;
import java.time.ZonedDateTime;
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

public interface ApiActivityLogRepository
    extends PagingAndSortingRepository<ApiActivityLogEntity, UUID>,
        JpaRepository<ApiActivityLogEntity, UUID>,
        JpaSpecificationExecutor<ApiActivityLogEntity> {

  Optional<ApiActivityLogEntity> findByRequestIdAndCallSeq(String requestId, int seq);

  List<ApiActivityLogEntity> findAllByRequestId(String requestId);

  Page<ApiActivityLogEntity> findAll(Pageable pageable);

  @Query(
      "select e from #{#entityName} e "
          + " where ( :requestId is not null and e.requestId = :requestId) "
          + " order by e.callSeq desc limit 1")
  Optional<ApiActivityLogEntity> findLatestSeq(String requestId);

  @Query(value = "SELECT e FROM #{#entityName} e where e.lifeStatus is null ")
  Page<ApiActivityLogEntity> findAllByMigrateStatus(Pageable pageable);

  Page<ApiActivityLogEntity> findAllBySyncStatusAndLifeStatusAndCreatedAtBefore(
      SyncStatusEnum syncStatus,
      LifeStatusEnum lifeStatus,
      ZonedDateTime createdAt,
      Pageable pageable);

  @Query(
      "SELECT e.path, e.method , COUNT(e) FROM #{#entityName} e "
          + "WHERE e.env = :env "
          + "AND e.callSeq = :callSeq "
          + "AND e.createdAt BETWEEN :startDate AND :endDate "
          + "AND ( (:buyer) is null or  e.buyer = :buyer )"
          + "GROUP BY e.path,  e.method "
          + "ORDER BY COUNT(e) DESC "
          + "LIMIT :limit ")
  List<Object[]> findTopEndpoints(
      @Param("env") String env,
      @Param("startDate") ZonedDateTime startDate,
      @Param("endDate") ZonedDateTime endDate,
      @Param("callSeq") String callSeq,
      @Param("buyer") String buyer,
      @Param("limit") int limit);

  List<ApiActivityLogEntity> findAllByRequestIdIn(List<String> requestIds);

  @Query(
      value =
          "SELECT e FROM #{#entityName} e where e.createdAt < :expiredDateTime and e.lifeStatus = :lifeStatus"
              + " and (:method is null or e.method = :method)")
  Page<ApiActivityLogEntity> listExpiredApiLog(
      ZonedDateTime expiredDateTime, LifeStatusEnum lifeStatus, String method, Pageable pageable);
}
