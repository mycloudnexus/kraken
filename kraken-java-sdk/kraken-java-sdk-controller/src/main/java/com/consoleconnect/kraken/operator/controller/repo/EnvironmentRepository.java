package com.consoleconnect.kraken.operator.controller.repo;

import com.consoleconnect.kraken.operator.controller.entity.EnvironmentEntity;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface EnvironmentRepository
    extends PagingAndSortingRepository<EnvironmentEntity, UUID>,
        JpaRepository<EnvironmentEntity, UUID>,
        JpaSpecificationExecutor<EnvironmentEntity> {
  Page<EnvironmentEntity> findAllByProductId(String productId, Pageable pageable);
}
