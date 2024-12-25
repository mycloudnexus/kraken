package com.consoleconnect.kraken.operator.controller.handler;

import com.consoleconnect.kraken.operator.core.client.ClientEvent;
import com.consoleconnect.kraken.operator.core.client.ClientEventTypeEnum;
import com.consoleconnect.kraken.operator.core.dto.ApiActivityLog;
import com.consoleconnect.kraken.operator.core.entity.ApiActivityLogBodyEntity;
import com.consoleconnect.kraken.operator.core.entity.ApiActivityLogEntity;
import com.consoleconnect.kraken.operator.core.mapper.ApiActivityLogBodyMapper;
import com.consoleconnect.kraken.operator.core.mapper.ApiActivityLogMapper;
import com.consoleconnect.kraken.operator.core.model.HttpResponse;
import com.consoleconnect.kraken.operator.core.repo.ApiActivityLogBodyRepository;
import com.consoleconnect.kraken.operator.core.repo.ApiActivityLogRepository;
import com.consoleconnect.kraken.operator.core.toolkit.JsonToolkit;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.AllArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class ClientAPIAuditLogEventHandler extends ClientEventHandler {
  private final ApiActivityLogRepository repository;
  private final ApiActivityLogBodyRepository apiActivityLogBodyRepository;

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
    Set<ApiActivityLogBodyEntity> newLogActivities = new HashSet<>();
    for (ApiActivityLog dto : requestList) {
      Optional<ApiActivityLogEntity> db =
          repository.findByRequestIdAndCallSeq(dto.getRequestId(), dto.getCallSeq());
      if (db.isEmpty()) {
        ApiActivityLogEntity entity = ApiActivityLogMapper.INSTANCE.map(dto);
        entity.setRequest(null);
        entity.setResponse(null);
        entity.setEnv(envId);
        entity.setCreatedBy(userId);
        newActivities.add(entity);

        ApiActivityLogBodyEntity apiLogBodyEntity = ApiActivityLogBodyMapper.INSTANCE.map(dto);
        entity.setApiLogBodyEntity(apiLogBodyEntity);
        newLogActivities.add(apiLogBodyEntity);
      }
    }
    apiActivityLogBodyRepository.saveAll(newLogActivities);
    repository.saveAll(newActivities);
    return HttpResponse.ok(null);
  }

  @Override
  public ClientEventTypeEnum getEventType() {
    return ClientEventTypeEnum.CLIENT_API_AUDIT_LOG;
  }
}
