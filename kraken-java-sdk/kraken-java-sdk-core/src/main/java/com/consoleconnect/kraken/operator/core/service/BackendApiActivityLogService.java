package com.consoleconnect.kraken.operator.core.service;

import com.consoleconnect.kraken.operator.core.entity.ApiActivityLogEntity;
import com.consoleconnect.kraken.operator.core.enums.LifeStatusEnum;
import com.consoleconnect.kraken.operator.core.enums.SyncStatusEnum;
import com.consoleconnect.kraken.operator.core.exception.KrakenException;
import com.consoleconnect.kraken.operator.core.model.ApiActivityRequestLog;
import com.consoleconnect.kraken.operator.core.model.ApiActivityResponseLog;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
@Slf4j
public class BackendApiActivityLogService {

  private final ApiActivityLogService apiActivityLogService;

  private static final String GATEWAY_SERVICE = "GATEWAY";

  @Transactional(rollbackFor = Exception.class)
  public void logApiActivityRequest(ApiActivityRequestLog requestLog) {
    final String requestId = requestLog.getRequestId();
    Optional<ApiActivityLogEntity> latestSeq = apiActivityLogService.findLatestSeq(requestId);
    int callSeq = latestSeq.map(ApiActivityLogEntity::getCallSeq).orElse(0) + 1;

    ApiActivityLogEntity entity = new ApiActivityLogEntity();
    entity.setRequestId(requestId);
    entity.setUri(requestLog.getUri());
    entity.setPath(requestLog.getPath());
    entity.setMethod(requestLog.getMethod());
    entity.setCallSeq(callSeq);
    entity.setSyncStatus(SyncStatusEnum.UNDEFINED);

    entity.setQueryParameters(requestLog.getQueryParameters());
    entity.setHeaders(requestLog.getHeaders());
    entity.setRequestIp(GATEWAY_SERVICE);
    entity.setRequest(requestLog.getRequest());
    entity.setLifeStatus(LifeStatusEnum.LIVE);
    entity.setResponseIp(requestLog.getResponseIp());
    apiActivityLogService.save(entity);
  }

  public void logApiActivityResponse(ApiActivityResponseLog responseLog) {
    try {
      ApiActivityLogEntity updatedEntity =
          apiActivityLogService
              .findByRequestIdAndCallSeq(responseLog.getRequestId(), responseLog.getCallSeq())
              .orElseThrow(
                  () ->
                      KrakenException.notFound(
                          String.format(
                              "request % %s not found",
                              responseLog.getRequestId(), responseLog.getCallSeq())));
      if (StringUtils.isNoneBlank(responseLog.getResponse())) {
        updatedEntity.setResponse(responseLog.getResponse());
      }
      updatedEntity.setHttpStatusCode(responseLog.getHttpStatusCode());
      updatedEntity.setResponseIp(updatedEntity.getUri());
      updatedEntity.setSyncStatus(SyncStatusEnum.UNDEFINED);
      apiActivityLogService.save(updatedEntity);
    } catch (Exception e) {
      log.error("tracing backed response error", e);
    }
  }
}
