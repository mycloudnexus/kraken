package com.consoleconnect.kraken.operator.core.repo;

import com.consoleconnect.kraken.operator.core.entity.WorkflowInstanceEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface WorkflowInstanceRepository
    extends PagingAndSortingRepository<WorkflowInstanceEntity, UUID>,
        JpaRepository<WorkflowInstanceEntity, UUID>,
        JpaSpecificationExecutor<WorkflowInstanceEntity> {

  List<WorkflowInstanceEntity> findAllBySynced(boolean synced);

  WorkflowInstanceEntity findByRequestId(String requestId);
}
