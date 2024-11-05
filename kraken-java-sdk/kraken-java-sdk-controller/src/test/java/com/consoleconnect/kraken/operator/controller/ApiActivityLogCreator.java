package com.consoleconnect.kraken.operator.controller;

import com.consoleconnect.kraken.operator.core.entity.ApiActivityLogEntity;
import com.consoleconnect.kraken.operator.core.repo.ApiActivityLogRepository;
import com.google.common.collect.Maps;
import java.util.Map;
import java.util.UUID;

public interface ApiActivityLogCreator {

  ApiActivityLogRepository getApiActivityLogRepository();

  default ApiActivityLogEntity createApiActivityLog(String buyerId, String envId) {
    ApiActivityLogEntity apiActivityLogEntity = new ApiActivityLogEntity();
    apiActivityLogEntity.setRequestId(UUID.randomUUID().toString());
    apiActivityLogEntity.setPath("/123");
    apiActivityLogEntity.setUri("localhost");
    apiActivityLogEntity.setMethod("GET");
    apiActivityLogEntity.setEnv(envId);
    Map<String, String> headers = Maps.newHashMap();
    headers.put("acces_token", "2334");
    apiActivityLogEntity.setHeaders(headers);
    apiActivityLogEntity.setBuyer(buyerId);
    apiActivityLogEntity.setCallSeq(0);
    apiActivityLogEntity = getApiActivityLogRepository().save(apiActivityLogEntity);
    return apiActivityLogEntity;
  }
}
