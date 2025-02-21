package com.consoleconnect.kraken.operator.gateway.service;

import com.consoleconnect.kraken.operator.core.entity.ApiActivityLogEntity;
import com.consoleconnect.kraken.operator.core.enums.LifeStatusEnum;
import com.consoleconnect.kraken.operator.core.enums.SyncStatusEnum;
import com.consoleconnect.kraken.operator.core.model.ApiActivityRequestLog;
import com.consoleconnect.kraken.operator.core.model.ApiActivityResponseLog;
import com.consoleconnect.kraken.operator.core.service.ApiActivityLogService;
import java.util.Optional;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
@Slf4j
public class BackendApiActivityLogService {

  private final ApiActivityLogService apiActivityLogService;

  private final FilterHeaderService filterHeaderService;

  @Transactional(rollbackFor = Exception.class)
  public ApiActivityLogEntity logApiActivityRequest(ApiActivityRequestLog requestLog) {
    final String requestId = requestLog.getRequestId();
    final int callSeq;
    if (requestLog.getCallSeq() == null) {
      Optional<ApiActivityLogEntity> latestSeq = apiActivityLogService.findLatestSeq(requestId);
      callSeq = latestSeq.map(ApiActivityLogEntity::getCallSeq).orElse(0) + 1;
    } else {
      callSeq = requestLog.getCallSeq();
    }

    ApiActivityLogEntity entity = createApiActivityLogEntity(requestLog, callSeq);
    return apiActivityLogService.save(entity);
  }

  @Transactional(rollbackFor = Exception.class)
  public Optional<ApiActivityLogEntity> logApiActivityRequestPayload(
      ApiActivityRequestLog requestLog) {
    Optional<ApiActivityLogEntity> optionalEntity =
        apiActivityLogService.findById(UUID.fromString(requestLog.getActivityRequestLogId()));
    if (optionalEntity.isEmpty()) {
      return optionalEntity;
    }
    ApiActivityLogEntity entity = optionalEntity.get();
    if (requestLog.getRequest() != null) {
      entity.setRequest(requestLog.getRequest());
    }
    if (requestLog.getUri() != null) {
      entity.setUri(requestLog.getUri());
    }

    return Optional.of(apiActivityLogService.save(entity));
  }

  @Transactional(rollbackFor = Exception.class)
  public void logApiActivityResponse(ApiActivityResponseLog responseLog) {
    try {
      ApiActivityLogEntity updatedEntity = responseLog.getApiActivityLog();
      if (responseLog.getResponse() != null) {
        updatedEntity.setResponse(responseLog.getResponse());
      }
      updatedEntity.setHttpStatusCode(responseLog.getHttpStatusCode());
      if (responseLog.getResponseIp() != null) {
        updatedEntity.setResponseIp(responseLog.getResponseIp());
      }
      updatedEntity.setSyncStatus(SyncStatusEnum.UNDEFINED);
      apiActivityLogService.save(updatedEntity);
    } catch (Exception e) {
      log.error("tracing backed response error", e);
    }
  }

  private ApiActivityLogEntity createApiActivityLogEntity(
      ApiActivityRequestLog requestLog, int callSeq) {
    ApiActivityLogEntity entity = new ApiActivityLogEntity();
    entity.setRequestId(requestLog.getRequestId());
    entity.setCallSeq(callSeq);

    entity.setUri(requestLog.getUri());
    entity.setPath(requestLog.getPath());
    entity.setMethod(requestLog.getMethod());
    entity.setQueryParameters(requestLog.getQueryParameters());
    entity.setHeaders(filterHeaderService.filterHeaders(requestLog.getHeaders()));
    entity.setRequest(requestLog.getRequest());

    entity.setRequestIp(requestLog.getRequestId());
    entity.setResponseIp(requestLog.getResponseIp());

    entity.setSyncStatus(SyncStatusEnum.UNDEFINED);
    entity.setLifeStatus(LifeStatusEnum.LIVE);

    return entity;
  }
}
