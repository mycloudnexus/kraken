package com.consoleconnect.kraken.operator.sync.service;

import com.consoleconnect.kraken.operator.core.client.ClientEvent;
import com.consoleconnect.kraken.operator.core.client.ClientEventTypeEnum;
import com.consoleconnect.kraken.operator.core.client.ClientWorkflowTerminate;
import com.consoleconnect.kraken.operator.core.entity.WorkflowInstanceEntity;
import com.consoleconnect.kraken.operator.core.model.HttpResponse;
import com.consoleconnect.kraken.operator.core.repo.WorkflowInstanceRepository;
import com.consoleconnect.kraken.operator.sync.model.SyncProperty;
import com.consoleconnect.kraken.operator.sync.service.security.ExternalSystemTokenProvider;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@Slf4j
public class PushWorkflowStateService extends KrakenServerConnector {
  private final WorkflowInstanceRepository workflowInstanceRepository;

  public PushWorkflowStateService(
      SyncProperty appProperty,
      WebClient webClient,
      ExternalSystemTokenProvider externalSystemTokenProvider,
      WorkflowInstanceRepository workflowInstanceRepository) {
    super(appProperty, webClient, externalSystemTokenProvider);
    this.workflowInstanceRepository = workflowInstanceRepository;
  }

  @Transactional
  @SchedulerLock(
      name = "pushWorkflowStatusLock",
      lockAtMostFor = "${app.cron-job.lock.at-most-for}",
      lockAtLeastFor = "${app.cron-job.lock.at-least-for}")
  @Scheduled(cron = "${app.cron-job.push-workflow-status:-}")
  public void runIt() {
    List<WorkflowInstanceEntity> workflowInstanceEntities =
        workflowInstanceRepository.findAllBySynced(false);
    List<ClientWorkflowTerminate> list =
        workflowInstanceEntities.stream()
            .map(
                entity -> {
                  ClientWorkflowTerminate workflowTerminate = new ClientWorkflowTerminate();
                  workflowTerminate.setWorkflowInstanceId(entity.getWorkflowInstanceId());
                  workflowTerminate.setStatus(entity.getStatus());
                  workflowTerminate.setRequestId(entity.getRequestId());
                  workflowTerminate.setErrorMessage(entity.getErrorMsg());
                  return workflowTerminate;
                })
            .toList();
    ClientEvent event =
        ClientEvent.of(CLIENT_ID, ClientEventTypeEnum.CLIENT_TERMINATE_WORKFLOW, list);

    HttpResponse<Void> res = pushEvent(event);
    if (res.getCode() == HttpStatus.OK.value()) {
      workflowInstanceRepository.saveAll(
          workflowInstanceEntities.stream()
              .map(
                  entity -> {
                    entity.setSynced(true);
                    return entity;
                  })
              .toList());
    }
  }
}
