package com.consoleconnect.kraken.operator.controller;

import com.consoleconnect.kraken.operator.core.entity.ApiActivityLogEntity;
import com.consoleconnect.kraken.operator.core.repo.ApiActivityLogRepository;
import com.google.common.collect.Maps;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.UUID;

public interface ApiActivityLogCreator {

  ApiActivityLogRepository getApiActivityLogRepository();

  default ApiActivityLogEntity createApiActivityLog(
      String buyerId, String envId, String productType) {
    return createApiActivityLog(buyerId, envId, productType, "/123", "localhost", "GET", 200);
  }

  default ApiActivityLogEntity createApiActivityLog(
      String buyerId,
      String envId,
      String productType,
      String path,
      String uri,
      String method,
      Integer httpStatusCode) {
    ApiActivityLogEntity apiActivityLogEntity = new ApiActivityLogEntity();
    apiActivityLogEntity.setRequestId(UUID.randomUUID().toString());
    apiActivityLogEntity.setPath(path);
    apiActivityLogEntity.setUri(uri);
    apiActivityLogEntity.setMethod(method);
    apiActivityLogEntity.setHttpStatusCode(httpStatusCode);
    apiActivityLogEntity.setEnv(envId);
    Map<String, String> headers = Maps.newHashMap();
    headers.put("acces_token", "2334");
    apiActivityLogEntity.setHeaders(headers);
    apiActivityLogEntity.setBuyer(buyerId);
    apiActivityLogEntity.setCallSeq(0);
    apiActivityLogEntity.setProductType(productType);
    apiActivityLogEntity.setTriggeredAt(ZonedDateTime.now());
    apiActivityLogEntity = getApiActivityLogRepository().save(apiActivityLogEntity);
    return apiActivityLogEntity;
  }
}
