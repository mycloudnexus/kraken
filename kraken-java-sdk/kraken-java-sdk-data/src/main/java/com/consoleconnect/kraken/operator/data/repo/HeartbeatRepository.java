package com.consoleconnect.kraken.operator.data.repo;

import com.consoleconnect.kraken.operator.data.entity.InstanceHeartbeatEntity;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface HeartbeatRepository
    extends PagingAndSortingRepository<InstanceHeartbeatEntity, UUID>,
        JpaRepository<InstanceHeartbeatEntity, UUID> {
  Optional<InstanceHeartbeatEntity> findOneByInstanceId(String instanceId);

  List<InstanceHeartbeatEntity> findAllByUpdatedAtGreaterThanEqual(ZonedDateTime updatedAt);
}
