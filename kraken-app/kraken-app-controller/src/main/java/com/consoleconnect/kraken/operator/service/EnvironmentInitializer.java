package com.consoleconnect.kraken.operator.service;

import com.consoleconnect.kraken.operator.config.AppMgmtProperty;
import com.consoleconnect.kraken.operator.controller.entity.EnvironmentEntity;
import com.consoleconnect.kraken.operator.controller.repo.EnvironmentRepository;
import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
@Slf4j
public class EnvironmentInitializer {

  private final AppMgmtProperty mgmtProperty;
  private final EnvironmentRepository environmentRepository;

  @PostConstruct
  public void initialize() {
    log.info("Initializing environments,{}", mgmtProperty.getProducts());
    mgmtProperty
        .getProducts()
        .forEach(
            productEnvironment -> {
              log.info("initializing environments for product {}", productEnvironment.getKey());

              List<EnvironmentEntity> existingEnvironments =
                  environmentRepository
                      .findAllByProductId(
                          productEnvironment.getKey(), PageRequest.of(0, Integer.MAX_VALUE))
                      .getContent();
              productEnvironment.getEnvironments().stream()
                  .filter(
                      environment ->
                          existingEnvironments.stream()
                              .noneMatch(
                                  environmentEntity ->
                                      environmentEntity.getName().equals(environment.getName())))
                  .forEach(
                      environment -> {
                        log.info(
                            "creating environment {} for product {}",
                            environment,
                            productEnvironment.getKey());
                        EnvironmentEntity environmentEntity = new EnvironmentEntity();
                        environmentEntity.setName(environment.getName());
                        if (environment.getId() != null) {
                          environmentEntity.setId(UUID.fromString(environment.getId()));
                        }
                        environmentEntity.setProductId(productEnvironment.getKey());
                        environmentRepository.save(environmentEntity);
                        log.info(
                            "created environment {} for product {}",
                            environment,
                            productEnvironment.getKey());
                      });
            });
    log.info("initialize environments done");
  }
}
