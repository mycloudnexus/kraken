package com.consoleconnect.kraken.operator.gateway.service.workflow;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

import com.consoleconnect.kraken.operator.gateway.CustomConfig;
import com.consoleconnect.kraken.operator.gateway.entity.HttpRequestEntity;
import com.consoleconnect.kraken.operator.gateway.repo.HttpRequestRepository;
import com.consoleconnect.kraken.operator.gateway.service.WorkflowTaskConfig;
import com.consoleconnect.kraken.operator.test.AbstractIntegrationTest;
import com.consoleconnect.kraken.operator.test.MockIntegrationTest;
import com.consoleconnect.kraken.operator.workflow.model.LogTaskRequest;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ContextConfiguration;

@MockIntegrationTest
@ContextConfiguration(classes = CustomConfig.class)
class WorkflowTaskConfigTest extends AbstractIntegrationTest {
  @SpyBean HttpRequestRepository httpRequestRepository;
  @Autowired WorkflowTaskConfig workflowTaskConfig;

  @Test
  void givenUrlAndId_whenNotify_thenSuccess() {
    String id = UUID.randomUUID().toString();
    String url = "https://httpbin.org/anything";
    assertDoesNotThrow(() -> workflowTaskConfig.notify(id, url));
  }

  @Test
  void givenId_whenFailAndRejectOrder_thenSuccess() {
    String id = UUID.randomUUID().toString();
    HttpRequestEntity entity = new HttpRequestEntity();
    entity.setRenderedResponse(Map.of("state", "active"));
    doReturn(Optional.of(entity)).when(httpRequestRepository).findById(any());
    doReturn(entity).when(httpRequestRepository).save(any());
    assertDoesNotThrow(
        () -> workflowTaskConfig.evaluateTask(Map.of("entity", entity), "", "${entity}"));
    assertDoesNotThrow(() -> workflowTaskConfig.logRequestPayload(new LogTaskRequest()));
    assertDoesNotThrow(() -> workflowTaskConfig.persistResponse(id, entity));
    assertDoesNotThrow(() -> workflowTaskConfig.failOrder(id));
    assertDoesNotThrow(() -> workflowTaskConfig.processOrder(id));
    assertDoesNotThrow(() -> workflowTaskConfig.doNothing());
    assertDoesNotThrow(() -> workflowTaskConfig.persistResponse(null, entity));
    assertDoesNotThrow(
        () -> workflowTaskConfig.evaluateTask(Map.of("entity", entity), "", "${entity1}"));
    assertDoesNotThrow(() -> workflowTaskConfig.rejectOrder(id));
  }
}
