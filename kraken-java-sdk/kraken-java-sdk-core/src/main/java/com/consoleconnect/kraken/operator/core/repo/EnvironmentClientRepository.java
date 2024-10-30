package com.consoleconnect.kraken.operator.core.repo;

import com.consoleconnect.kraken.operator.core.entity.EnvironmentClientEntity;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface EnvironmentClientRepository
    extends PagingAndSortingRepository<EnvironmentClientEntity, UUID>,
        JpaRepository<EnvironmentClientEntity, UUID> {
  Optional<EnvironmentClientEntity> findOneByEnvIdAndClientKeyAndKind(
      String envId, String clientKey, String kind);

  Page<EnvironmentClientEntity> findAllByEnvIdAndKindAndUpdatedAtGreaterThan(
      String envId, String kind, ZonedDateTime updatedAt, Pageable pageable);

  Page<EnvironmentClientEntity> findAllByKindAndUpdatedAtGreaterThan(
      String kind, ZonedDateTime updatedAt, Pageable pageable);

  Page<EnvironmentClientEntity> findTop10ByEnvIdAndKindOrderByUpdatedAtDesc(
      String envId, String kind, Pageable pageable);

  List<EnvironmentClientEntity> findAllByEnvIdAndKind(String envId, String kind);

  List<EnvironmentClientEntity> findAllByClientKeyAndKind(String clientKey, String kind);
}
