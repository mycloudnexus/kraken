package com.consoleconnect.kraken.operator.controller.repo;

import com.consoleconnect.kraken.operator.controller.entity.TokenStorageEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface TokenStorageRepository
    extends PagingAndSortingRepository<TokenStorageEntity, UUID>,
        JpaRepository<TokenStorageEntity, UUID>,
        JpaSpecificationExecutor<TokenStorageEntity> {
  TokenStorageEntity findFirstByAssetId(String assetId);
}
