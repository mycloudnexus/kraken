package com.consoleconnect.kraken.operator.core.repo;

import com.consoleconnect.kraken.operator.core.entity.ApiActivityLogEntity;
import com.consoleconnect.kraken.operator.core.entity.HttpRequestBodyEntity;
import com.consoleconnect.kraken.operator.core.enums.SyncStatusEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface HttpRequestBodyRepository
    extends PagingAndSortingRepository<HttpRequestBodyEntity, UUID>,
        JpaRepository<HttpRequestBodyEntity, UUID>,
        JpaSpecificationExecutor<HttpRequestBodyEntity> {

}
