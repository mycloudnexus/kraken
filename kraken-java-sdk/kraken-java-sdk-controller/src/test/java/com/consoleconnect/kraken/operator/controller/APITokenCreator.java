package com.consoleconnect.kraken.operator.controller;

import com.consoleconnect.kraken.operator.controller.dto.CreateAPITokenRequest;
import com.consoleconnect.kraken.operator.controller.model.APIToken;
import com.consoleconnect.kraken.operator.controller.service.APITokenService;

public interface APITokenCreator {

  APITokenService getApiTokenService();

  default APIToken createToken(String envId, String productId) {
    CreateAPITokenRequest body = new CreateAPITokenRequest();
    body.setName("Token-" + System.currentTimeMillis());
    body.setEnvId(envId);
    return getApiTokenService().createToken(productId, body, null);
  }
}
