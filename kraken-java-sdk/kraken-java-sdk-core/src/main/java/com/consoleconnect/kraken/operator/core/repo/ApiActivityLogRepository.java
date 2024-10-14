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
import org.springframework.data.repository.PagingAndSortingRepository;

public interface ApiActivityLogRepository
    extends PagingAndSortingRepository<ApiActivityLogEntity, UUID>,
        JpaRepository<ApiActivityLogEntity, UUID>,
        JpaSpecificationExecutor<ApiActivityLogEntity> {

  Optional<ApiActivityLogEntity> findByRequestIdAndCallSeq(String requestId, int seq);

  List<ApiActivityLogEntity> findAllByRequestId(String requestId);

  Page<ApiActivityLogEntity> findAll(Pageable pageable);

  Page<ApiActivityLogEntity> findAllBySyncStatusAndCreatedAtBefore(
      SyncStatusEnum syncStatus, ZonedDateTime createdAt, Pageable pageable);
}
