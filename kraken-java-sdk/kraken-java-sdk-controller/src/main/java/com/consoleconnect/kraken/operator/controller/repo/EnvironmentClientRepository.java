package com.consoleconnect.kraken.operator.controller.repo;

import com.consoleconnect.kraken.operator.controller.entity.EnvironmentClientEntity;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface EnvironmentClientRepository
    extends PagingAndSortingRepository<EnvironmentClientEntity, UUID>,
        JpaRepository<EnvironmentClientEntity, UUID> {
  Optional<EnvironmentClientEntity> findOneByEnvIdAndAndClientIpAndKind(
      String envId, String clientIp, String kind);

  Page<EnvironmentClientEntity> findAllByEnvIdAndKindAndUpdatedAtGreaterThan(
      String envId, String kind, ZonedDateTime updatedAt, Pageable pageable);

  Page<EnvironmentClientEntity> findAllByKindAndUpdatedAtGreaterThan(
      String kind, ZonedDateTime updatedAt, Pageable pageable);

  Page<EnvironmentClientEntity> findTop10ByEnvIdAndKindOrderByUpdatedAtDesc(
      String envId, String kind, Pageable pageable);
}
