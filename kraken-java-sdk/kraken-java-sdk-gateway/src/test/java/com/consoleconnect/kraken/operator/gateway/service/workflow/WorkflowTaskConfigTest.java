package com.consoleconnect.kraken.operator.gateway.service.workflow;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doReturn;

import com.consoleconnect.kraken.operator.core.entity.ApiActivityLogEntity;
import com.consoleconnect.kraken.operator.core.entity.WorkflowInstanceEntity;
import com.consoleconnect.kraken.operator.core.enums.WorkflowStatusEnum;
import com.consoleconnect.kraken.operator.core.repo.ApiActivityLogRepository;
import com.consoleconnect.kraken.operator.core.repo.WorkflowInstanceRepository;
import com.consoleconnect.kraken.operator.gateway.CustomConfig;
import com.consoleconnect.kraken.operator.gateway.entity.HttpRequestEntity;
import com.consoleconnect.kraken.operator.gateway.repo.HttpRequestRepository;
import com.consoleconnect.kraken.operator.gateway.service.WorkflowTaskConfig;
import com.consoleconnect.kraken.operator.test.AbstractIntegrationTest;
import com.consoleconnect.kraken.operator.test.MockIntegrationTest;
import com.consoleconnect.kraken.operator.workflow.model.LogTaskRequest;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ContextConfiguration;

@MockIntegrationTest
@ContextConfiguration(classes = CustomConfig.class)
class WorkflowTaskConfigTest extends AbstractIntegrationTest {
  @SpyBean HttpRequestRepository httpRequestRepository;
  @SpyBean ApiActivityLogRepository apiActivityLogRepository;
  @Autowired WorkflowTaskConfig workflowTaskConfig;
  @Autowired WorkflowInstanceRepository workflowInstanceRepository;

  @Test
  void givenUrlAndId_whenNotify_thenSuccess() {
    String id = UUID.randomUUID().toString();
    String url = "https://httpbin.org/anything";
    assertDoesNotThrow(() -> workflowTaskConfig.notify(id, url));
  }

  @Test
  void givenId_whenFailAndRejectOrder_thenSuccess() {
    String id = UUID.randomUUID().toString();
    WorkflowInstanceEntity workflowInstanceEntity = new WorkflowInstanceEntity();
    workflowInstanceEntity.setStatus(WorkflowStatusEnum.FAILED.name());
    workflowInstanceEntity.setErrorMsg("error");
    workflowInstanceEntity.setRequestId(id);
    workflowInstanceEntity.setSynced(false);
    workflowInstanceEntity.setWorkflowInstanceId(UUID.randomUUID().toString());
    workflowInstanceRepository.save(workflowInstanceEntity);

    doReturn(Optional.of(new ApiActivityLogEntity()))
        .when(apiActivityLogRepository)
        .findByRequestIdAndCallSeq(anyString(), anyInt());
    doReturn(new ApiActivityLogEntity()).when(apiActivityLogRepository).save(any());

    HttpRequestEntity entity = new HttpRequestEntity();
    entity.setRenderedResponse(Map.of("state", "active"));
    doReturn(Optional.of(entity)).when(httpRequestRepository).findById(any());
    doReturn(entity).when(httpRequestRepository).save(any());
    assertDoesNotThrow(
        () -> workflowTaskConfig.evaluateTask(Map.of("entity", entity), "", "${entity}"));
    assertDoesNotThrow(
        () -> workflowTaskConfig.evaluateExpressionTask(Map.of("entity", entity), null));
    assertDoesNotThrow(() -> workflowTaskConfig.logRequestPayload(new LogTaskRequest()));
    assertDoesNotThrow(() -> workflowTaskConfig.persistResponse(id, Map.of("id", "123"), "id"));
    assertDoesNotThrow(() -> workflowTaskConfig.failOrder(id));
    assertDoesNotThrow(() -> workflowTaskConfig.processOrder(id));
    assertDoesNotThrow(() -> workflowTaskConfig.doNothing());
    assertDoesNotThrow(() -> workflowTaskConfig.persistResponse(null, Map.of("id", "123"), "id"));
    assertDoesNotThrow(() -> workflowTaskConfig.workflowFailedTask(id, "unexpected error"));
    assertDoesNotThrow(() -> workflowTaskConfig.workflowSuccessTask(id));
    assertDoesNotThrow(
        () -> workflowTaskConfig.evaluateTask(Map.of("entity", entity), "", "${entity1}"));
    assertDoesNotThrow(() -> workflowTaskConfig.rejectOrder(id));
  }

  @Test
  void givenLogTaskInput_whenLogApiActivityLog_thenSuccess() throws IOException {
    final String requestId = UUID.randomUUID().toString();
    LogTaskRequest logTaskRequest = new LogTaskRequest();
    logTaskRequest.setRequestId(requestId);
    logTaskRequest.setRequestPayload(readFileToString("mockData/workflowRequest.json"));
    logTaskRequest.setResponsePayload(readFileToString("mockData/workflowResponse.json"));
    assertDoesNotThrow(() -> workflowTaskConfig.logRequestPayload(logTaskRequest));

    List<ApiActivityLogEntity> apiActivityLogEntities =
        apiActivityLogRepository.findAllByRequestId(requestId);
    Assertions.assertEquals(1, apiActivityLogEntities.size());
    ApiActivityLogEntity activityLog = apiActivityLogEntities.get(0);
    Assertions.assertEquals(1, activityLog.getCallSeq());
    Assertions.assertEquals("https://localhost/qe1company/ports/order?q=v", activityLog.getUri());
    Assertions.assertEquals("/qe1company/ports/order", activityLog.getPath());
    Assertions.assertEquals("PUT", activityLog.getMethod());
    Assertions.assertEquals("v", activityLog.getQueryParameters().get("q"));
    Assertions.assertEquals("HEADER_VAL1", activityLog.getHeaders().get("TEST_HEADER"));

    Map<String, String> request =
        (Map<String, String>) activityLog.getApiLogBodyEntity().getRequest();
    Assertions.assertEquals("test-qc01", request.get("portName"));

    Map<String, String> response =
        (Map<String, String>) activityLog.getApiLogBodyEntity().getResponse();
    Assertions.assertEquals("Mock company name", response.get("name"));
    Assertions.assertEquals(200, activityLog.getHttpStatusCode());
  }
}
