package com.consoleconnect.kraken.operator.gateway.repo;

import com.consoleconnect.kraken.operator.gateway.entity.HttpRequestEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.transaction.annotation.Transactional;

public interface HttpRequestRepository
    extends PagingAndSortingRepository<HttpRequestEntity, UUID>,
        JpaRepository<HttpRequestEntity, UUID>,
        JpaSpecificationExecutor<HttpRequestEntity> {
  List<HttpRequestEntity> findByExternalId(String externalId);

  Optional<HttpRequestEntity> findByProductInstanceId(String productInstanceId);

  @Query(
      value =
          "select e from #{#entityName} e "
              + " where ( (:bizType) is null or  e.bizType = :bizType )"
              + " and ( (:externalId) is null or  e.externalId = :externalId ) order by e.updatedAt desc")
  @Transactional(readOnly = true)
  Page<HttpRequestEntity> search(String bizType, String externalId, Pageable pageable);
}
