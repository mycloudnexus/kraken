package com.consoleconnect.kraken.operator.gateway.service.workflow;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.consoleconnect.kraken.operator.gateway.CustomConfig;
import com.consoleconnect.kraken.operator.gateway.entity.HttpRequestEntity;
import com.consoleconnect.kraken.operator.gateway.repo.HttpRequestRepository;
import com.consoleconnect.kraken.operator.gateway.service.WorkflowTaskConfig;
import com.consoleconnect.kraken.operator.test.AbstractIntegrationTest;
import com.consoleconnect.kraken.operator.test.MockIntegrationTest;
import java.util.*;
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
    Map<String, Object> entityMap = Map.of("entity", entity);
    doReturn(entity).when(httpRequestRepository).save(any());
    assertDoesNotThrow(() -> workflowTaskConfig.evaluateTask(entityMap, "${id}"));
    assertDoesNotThrow(() -> workflowTaskConfig.logRequestPayload(entityMap, id));
    assertDoesNotThrow(() -> workflowTaskConfig.logResponsePayload(entityMap, id));
    assertDoesNotThrow(() -> workflowTaskConfig.failOrder(id));
    assertDoesNotThrow(() -> workflowTaskConfig.rejectOrder(id));
  }
}
