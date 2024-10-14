package com.consoleconnect.kraken.operator.data.repo;

import com.consoleconnect.kraken.operator.data.entity.AssetReleaseEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface AssetReleaseRepository
    extends PagingAndSortingRepository<AssetReleaseEntity, UUID>,
        JpaRepository<AssetReleaseEntity, UUID>,
        JpaSpecificationExecutor<AssetReleaseEntity> {
  Optional<AssetReleaseEntity> findFirstByOrderByCreatedAtDesc();
}
