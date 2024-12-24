package com.consoleconnect.kraken.operator.core.repo;

import com.consoleconnect.kraken.operator.core.entity.ApiActivityLogEntity;
import com.consoleconnect.kraken.operator.core.enums.SyncStatusEnum;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface ApiActivityLogRepository
    extends PagingAndSortingRepository<ApiActivityLogEntity, UUID>,
        JpaRepository<ApiActivityLogEntity, UUID>,
        JpaSpecificationExecutor<ApiActivityLogEntity> {

  Optional<ApiActivityLogEntity> findByRequestIdAndCallSeq(String requestId, int seq);

  List<ApiActivityLogEntity> findAllByRequestId(String requestId);

  Page<ApiActivityLogEntity> findAll(Pageable pageable);

  Page<ApiActivityLogEntity> findAllBySyncStatusAndCreatedAtBefore(
      SyncStatusEnum syncStatus, ZonedDateTime createdAt, Pageable pageable);

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

  @Modifying
  @Transactional
  @Query(
      nativeQuery = true,
      value = "delete from kraken_api_log_activity where created_at <  :expiredDateTime")
  void deleteExpiredLog(ZonedDateTime expiredDateTime);
}
