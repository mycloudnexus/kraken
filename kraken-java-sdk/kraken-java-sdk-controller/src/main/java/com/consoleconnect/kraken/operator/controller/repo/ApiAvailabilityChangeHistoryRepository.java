package com.consoleconnect.kraken.operator.controller.repo;

import com.consoleconnect.kraken.operator.controller.entity.ApiAvailabilityChangeHistoryEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface ApiAvailabilityChangeHistoryRepository
    extends PagingAndSortingRepository<ApiAvailabilityChangeHistoryEntity, UUID>,
        JpaRepository<ApiAvailabilityChangeHistoryEntity, UUID>,
        JpaSpecificationExecutor<ApiAvailabilityChangeHistoryEntity> {

  List<ApiAvailabilityChangeHistoryEntity> findAllByMapperKeyAndEnvOOrderByCreatedAtDesc(
      String mapperKey, String env);
}
