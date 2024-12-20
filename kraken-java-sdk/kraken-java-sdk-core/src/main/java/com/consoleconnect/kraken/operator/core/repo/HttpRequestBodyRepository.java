package com.consoleconnect.kraken.operator.core.repo;

import com.consoleconnect.kraken.operator.core.entity.HttpRequestBodyEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface HttpRequestBodyRepository
    extends PagingAndSortingRepository<HttpRequestBodyEntity, UUID>,
        JpaRepository<HttpRequestBodyEntity, UUID>,
        JpaSpecificationExecutor<HttpRequestBodyEntity> {}
