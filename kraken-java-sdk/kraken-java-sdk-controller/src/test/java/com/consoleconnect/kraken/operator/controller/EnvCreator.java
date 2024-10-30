package com.consoleconnect.kraken.operator.controller;

import com.consoleconnect.kraken.operator.auth.security.UserContext;
import com.consoleconnect.kraken.operator.controller.dto.CreateEnvRequest;
import com.consoleconnect.kraken.operator.controller.model.Environment;
import com.consoleconnect.kraken.operator.controller.service.EnvironmentService;
import com.consoleconnect.kraken.operator.core.enums.EnvNameEnum;
import org.junit.jupiter.api.Assertions;
import org.springframework.data.domain.PageRequest;

public interface EnvCreator {

  EnvironmentService getEnvironmentService();

  default Environment createStage(String productIdOrKey) {
    return createEnv(productIdOrKey, EnvNameEnum.STAGE.name());
  }

  default Environment createProduction(String productIdOrKey) {
    return createEnv(productIdOrKey, EnvNameEnum.PRODUCTION.name());
  }

  default Environment createEnv(String productIdOrKey, String envName) {
    Assertions.assertNotNull(envName);
    Assertions.assertNotNull(productIdOrKey);
    return getEnvironmentService().search(productIdOrKey, PageRequest.of(0, 50)).getData().stream()
        .filter(env -> envName.equals(env.getName()))
        .findFirst()
        .orElseGet(
            () -> {
              CreateEnvRequest request = new CreateEnvRequest();
              request.setName(envName);
              return getEnvironmentService().create(productIdOrKey, request, UserContext.ANONYMOUS);
            });
  }
}
