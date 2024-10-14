package com.consoleconnect.kraken.operator.service;

import com.consoleconnect.kraken.operator.config.AppMgmtProperty;
import com.consoleconnect.kraken.operator.controller.dto.CreateEnvRequest;
import com.consoleconnect.kraken.operator.controller.model.Environment;
import com.consoleconnect.kraken.operator.controller.service.EnvironmentService;
import com.consoleconnect.kraken.operator.core.entity.UnifiedAssetEntity;
import com.consoleconnect.kraken.operator.core.event.PlatformSettingCompletedEvent;
import com.consoleconnect.kraken.operator.core.service.UnifiedAssetService;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
@Slf4j
public class EnvironmentInitializer {

  private final AppMgmtProperty mgmtProperty;

  private final UnifiedAssetService unifiedAssetService;
  private final EnvironmentService environmentService;

  @EventListener(PlatformSettingCompletedEvent.class)
  public void initialize() {
    log.info("Initializing environments,{}", mgmtProperty.getProducts());
    mgmtProperty
        .getProducts()
        .forEach(
            productEnvironment -> {
              log.info("initializing environments for product {}", productEnvironment.getKey());
              UnifiedAssetEntity product =
                  unifiedAssetService.findOneByIdOrKey(productEnvironment.getKey());

              List<Environment> existingEnvironments =
                  environmentService
                      .search(
                          product.getKey(),
                          UnifiedAssetService.getSearchPageRequest(0, Integer.MAX_VALUE))
                      .getData();
              productEnvironment.getEnvironments().stream()
                  .filter(
                      environmentName ->
                          existingEnvironments.stream()
                              .noneMatch(
                                  environmentEntity ->
                                      environmentEntity.getName().equals(environmentName)))
                  .forEach(
                      environmentName -> {
                        CreateEnvRequest createEnvRequest = new CreateEnvRequest();
                        createEnvRequest.setName(environmentName);
                        log.info(
                            "creating environment {} for product {}",
                            environmentName,
                            product.getKey());
                        environmentService.create(
                            product.getId().toString(), createEnvRequest, null);
                        log.info(
                            "created environment {} for product {}",
                            environmentName,
                            product.getKey());
                      });
            });
    log.info("initialize environments done");
  }
}
