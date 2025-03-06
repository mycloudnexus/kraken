package com.consoleconnect.kraken.operator.controller.handler;

import com.consoleconnect.kraken.operator.core.client.ClientEvent;
import com.consoleconnect.kraken.operator.core.client.ClientEventTypeEnum;
import com.consoleconnect.kraken.operator.core.client.ClientWorkflowTerminate;
import com.consoleconnect.kraken.operator.core.model.HttpResponse;
import com.consoleconnect.kraken.operator.core.repo.ApiActivityLogRepository;
import com.consoleconnect.kraken.operator.core.toolkit.JsonToolkit;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class ClientWorkflowStateChangeEventHandler extends ClientEventHandler {
  private final ApiActivityLogRepository apiActivityLogRepository;

  @Override
  public ClientEventTypeEnum getEventType() {
    return ClientEventTypeEnum.CLIENT_TERMINATE_WORKFLOW;
  }

  @Override
  public HttpResponse<Void> onEvent(String envId, String userId, ClientEvent event) {
    List<ClientWorkflowTerminate> requestList =
        JsonToolkit.fromJson(event.getEventPayload(), new TypeReference<>() {});
    requestList.stream()
        .forEach(
            clientWorkflowTerminate ->
                apiActivityLogRepository
                    .findByRequestIdAndCallSeq(clientWorkflowTerminate.getRequestId(), 0)
                    .ifPresent(
                        entity -> {
                          entity.setErrorMsg(clientWorkflowTerminate.getErrorMessage());
                          entity.setWorkflowInstanceId(
                              clientWorkflowTerminate.getWorkflowInstanceId());
                          entity.setWorkflowStatus(clientWorkflowTerminate.getStatus());
                          apiActivityLogRepository.save(entity);
                        }));
    return HttpResponse.ok(null);
  }
}
