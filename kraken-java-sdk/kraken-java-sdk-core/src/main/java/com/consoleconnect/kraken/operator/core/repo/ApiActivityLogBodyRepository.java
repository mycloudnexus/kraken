package com.consoleconnect.kraken.operator.core.repo;

import com.consoleconnect.kraken.operator.core.entity.ApiActivityLogBodyEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface ApiActivityLogBodyRepository
    extends PagingAndSortingRepository<ApiActivityLogBodyEntity, UUID>,
        JpaRepository<ApiActivityLogBodyEntity, UUID>,
        JpaSpecificationExecutor<ApiActivityLogBodyEntity> {}
