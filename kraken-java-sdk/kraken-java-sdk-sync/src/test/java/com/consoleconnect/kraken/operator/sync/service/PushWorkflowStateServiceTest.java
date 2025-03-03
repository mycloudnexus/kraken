package com.consoleconnect.kraken.operator.sync.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

import com.consoleconnect.kraken.operator.core.entity.WorkflowInstanceEntity;
import com.consoleconnect.kraken.operator.core.enums.WorkflowStatusEnum;
import com.consoleconnect.kraken.operator.core.model.HttpResponse;
import com.consoleconnect.kraken.operator.core.repo.WorkflowInstanceRepository;
import com.consoleconnect.kraken.operator.sync.CustomConfig;
import com.consoleconnect.kraken.operator.test.AbstractIntegrationTest;
import com.consoleconnect.kraken.operator.test.MockIntegrationTest;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ContextConfiguration;

@MockIntegrationTest
@ContextConfiguration(classes = {CustomConfig.class})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PushWorkflowStateServiceTest extends AbstractIntegrationTest {
  @SpyBean PushWorkflowStateService pushWorkflowStateService;
  @Autowired WorkflowInstanceRepository workflowInstanceRepository;

  @Test
  void givenWorkflowChangeEvent_whenRun_thenResponseOK() {
    doReturn(HttpResponse.ok(null)).when(pushWorkflowStateService).pushEvent(any());
    WorkflowInstanceEntity workflowInstanceEntity = new WorkflowInstanceEntity();
    String requestId = UUID.randomUUID().toString();
    workflowInstanceEntity.setWorkflowInstanceId(UUID.randomUUID().toString());
    workflowInstanceEntity.setErrorMsg("error");
    workflowInstanceEntity.setSynced(false);
    workflowInstanceEntity.setStatus(WorkflowStatusEnum.FAILED.name());
    workflowInstanceEntity.setRequestId(requestId);
    workflowInstanceRepository.save(workflowInstanceEntity);
    pushWorkflowStateService.runIt();
    Assertions.assertTrue(workflowInstanceRepository.findByRequestId(requestId).isSynced());
  }
}
