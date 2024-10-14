package com.consoleconnect.kraken.operator.gateway.repo;

import com.consoleconnect.kraken.operator.gateway.entity.EventEntity;
import com.consoleconnect.kraken.operator.gateway.model.RegisterState;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface EventRepository
    extends PagingAndSortingRepository<EventEntity, UUID>,
        JpaRepository<EventEntity, UUID>,
        JpaSpecificationExecutor<EventEntity> {
  List<EventEntity> findEventEntitiesByBuyerIdAndState(String buyerId, RegisterState state);
}
