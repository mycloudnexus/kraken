package com.consoleconnect.kraken.operator.core.repo;

import com.consoleconnect.kraken.operator.core.entity.SystemInfoEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface SystemInfoRepository
    extends PagingAndSortingRepository<SystemInfoEntity, UUID>,
        JpaRepository<SystemInfoEntity, UUID> {
  Optional<SystemInfoEntity> findOneByKey(String key);
}
