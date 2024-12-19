package com.consoleconnect.kraken.operator.controller.handler;

import com.consoleconnect.kraken.operator.core.client.ClientEvent;
import com.consoleconnect.kraken.operator.core.client.ClientEventTypeEnum;
import com.consoleconnect.kraken.operator.core.dto.ApiActivityLog;
import com.consoleconnect.kraken.operator.core.entity.ApiActivityLogEntity;
import com.consoleconnect.kraken.operator.core.mapper.ApiActivityLogMapper;
import com.consoleconnect.kraken.operator.core.model.HttpResponse;
import com.consoleconnect.kraken.operator.core.repo.ApiActivityLogRepository;
import com.consoleconnect.kraken.operator.core.toolkit.JsonToolkit;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.*;

import lombok.AllArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class ClientAPIAuditLogEventHandler extends ClientEventHandler {
  private final ApiActivityLogRepository repository;

  public void onEvent(int count) {
    int max = 100000;
    Set<ApiActivityLogEntity> newActivities = new HashSet<>();
    var firstOne =
    this.repository.findById(UUID.fromString( "e0289c22-b54b-4383-86ee-0fef50d7a9bd")).orElse(null);
    if(firstOne == null)
    {
      return;
    }
    for(int i=0;i<count;i++)
    {

      ApiActivityLogEntity apiActivityLogEntity = new ApiActivityLogEntity();

      apiActivityLogEntity.setEnv("b2d775e5-44ad-43cb-8dd4-6fbe52585ec9");
      apiActivityLogEntity.setHeaders(firstOne.getHeaders());
      apiActivityLogEntity.setHttpStatusCode(firstOne.getHttpStatusCode());
      apiActivityLogEntity.setMethod(firstOne.getMethod());
      apiActivityLogEntity.setPath(firstOne.getPath());
      apiActivityLogEntity.setQueryParameters(firstOne.getQueryParameters());
      apiActivityLogEntity.setRequest(firstOne.getRequest());
      apiActivityLogEntity.setRequestId(UUID.randomUUID().toString());
      apiActivityLogEntity.setResponse(firstOne.getResponse());
      apiActivityLogEntity.setUri(firstOne.getUri());

      apiActivityLogEntity.setCallSeq(firstOne.getCallSeq());
      apiActivityLogEntity.setRequestIp(firstOne.getRequestId());

      apiActivityLogEntity.setRequestIp(firstOne.getRequestIp());
      apiActivityLogEntity.setResponseIp(firstOne.getResponseIp());

      apiActivityLogEntity.setSyncStatus(firstOne.getSyncStatus());
      apiActivityLogEntity.setSyncedAt(firstOne.getSyncedAt());

      apiActivityLogEntity.setBuyer(firstOne.getBuyer());

      newActivities.add(apiActivityLogEntity);

      if(i % max == 0)
      {
        repository.saveAll(newActivities);
        newActivities.clear();
      }
    }
  }

  @Override
  @Transactional
  public HttpResponse<Void> onEvent(String envId, String userId, ClientEvent event) {
    if (event.getEventPayload() == null) {
      return HttpResponse.ok(null);
    }
    List<ApiActivityLog> requestList =
        JsonToolkit.fromJson(event.getEventPayload(), new TypeReference<>() {});

    if (CollectionUtils.isEmpty(requestList)) {
      return HttpResponse.ok(null);
    }
    Set<ApiActivityLogEntity> newActivities = new HashSet<>();
    for (ApiActivityLog dto : requestList) {
      Optional<ApiActivityLogEntity> db =
          repository.findByRequestIdAndCallSeq(dto.getRequestId(), dto.getCallSeq());
      ApiActivityLogEntity entity = ApiActivityLogMapper.INSTANCE.map(dto);
      if (db.isEmpty()) {
        entity.setEnv(envId);
        entity.setCreatedBy(userId);
        newActivities.add(entity);
      }
    }
    repository.saveAll(newActivities);
    return HttpResponse.ok(null);
  }

  @Override
  public ClientEventTypeEnum getEventType() {
    return ClientEventTypeEnum.CLIENT_API_AUDIT_LOG;
  }
}
