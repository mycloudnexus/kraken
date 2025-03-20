package com.consoleconnect.kraken.operator.controller.handler;

import com.consoleconnect.kraken.operator.controller.service.ProductDeploymentService;
import com.consoleconnect.kraken.operator.core.client.ClientEvent;
import com.consoleconnect.kraken.operator.core.client.ClientEventTypeEnum;
import com.consoleconnect.kraken.operator.core.client.ClientInstanceDeployment;
import com.consoleconnect.kraken.operator.core.dto.DeployComponentError;
import com.consoleconnect.kraken.operator.core.entity.EnvironmentClientEntity;
import com.consoleconnect.kraken.operator.core.entity.UnifiedAssetEntity;
import com.consoleconnect.kraken.operator.core.enums.ClientReportTypeEnum;
import com.consoleconnect.kraken.operator.core.enums.DeployStatusEnum;
import com.consoleconnect.kraken.operator.core.enums.ErrorSeverityEnum;
import com.consoleconnect.kraken.operator.core.model.HttpResponse;
import com.consoleconnect.kraken.operator.core.repo.EnvironmentClientRepository;
import com.consoleconnect.kraken.operator.core.repo.UnifiedAssetRepository;
import com.consoleconnect.kraken.operator.core.toolkit.JsonToolkit;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
@Slf4j
public class ClientDeploymentEventHandler extends ClientEventHandler {
  private final EnvironmentClientRepository environmentClientRepository;
  private final UnifiedAssetRepository unifiedAssetRepository;
  private final ProductDeploymentService productDeploymentService;

  private static final String DEPLOY_STATUS_FAILED = "FAILED";

  @Override
  public ClientEventTypeEnum getEventType() {
    return ClientEventTypeEnum.CLIENT_DEPLOYMENT;
  }

  @Transactional
  @Override
  public HttpResponse<Void> onEvent(String envId, String userId, ClientEvent event) {

    if (event.getEventPayload() == null) {
      return HttpResponse.ok(null);
    }

    ClientInstanceDeployment deployment =
        JsonToolkit.fromJson(event.getEventPayload(), ClientInstanceDeployment.class);
    EnvironmentClientEntity environmentClientEntity =
        environmentClientRepository
            .findOneByEnvIdAndClientKeyAndKind(
                envId, event.getClientId(), ClientReportTypeEnum.DEPLOY.name())
            .orElseGet(
                () -> {
                  EnvironmentClientEntity entity = new EnvironmentClientEntity();
                  entity.setEnvId(envId);
                  entity.setKind(ClientReportTypeEnum.DEPLOY.name());
                  entity.setClientKey(event.getClientId());
                  entity.setCreatedAt(ZonedDateTime.now());
                  entity.setCreatedBy(userId);
                  return entity;
                });
    environmentClientEntity.setStatus(deployment.getStatus());
    environmentClientEntity.setUpdatedAt(ZonedDateTime.now());
    environmentClientEntity.setUpdatedBy(userId);
    processDeployError(environmentClientEntity, deployment);
    environmentClientRepository.save(environmentClientEntity);

    Optional<UnifiedAssetEntity> deploymentOptional =
        unifiedAssetRepository.findById(UUID.fromString(deployment.getProductReleaseId()));

    if (deploymentOptional.isPresent()) {
      log.info(
          "report asset {} configuration  reloading result success, status: {}",
          deployment.getProductReleaseId(),
          deployment.getStatus());
      if (DEPLOY_STATUS_FAILED.equals(deployment.getStatus())) {
        deploymentOptional.get().setStatus(DeployStatusEnum.FAILED.name());
      } else {
        deploymentOptional.get().setStatus(DeployStatusEnum.SUCCESS.name());
      }

      unifiedAssetRepository.save(deploymentOptional.get());
      try {
        productDeploymentService.reportConfigurationReloadingResult(
            deployment.getProductReleaseId(), deploymentOptional.get().getStatus());
      } catch (Exception e) {
        log.error("Failed to reportConfigurationReloadingResult", e);
      }
    } else {
      log.info(
          "report asset {} configuration  reloading result failed",
          deployment.getProductReleaseId());
    }

    return HttpResponse.ok(null);
  }

  private static final int MAX_REASON_MSG = 120;

  private void processDeployError(
      EnvironmentClientEntity environmentClient, ClientInstanceDeployment deployment) {
    environmentClient.setErrors(deployment.getErrors());
    if (CollectionUtils.isNotEmpty(deployment.getErrors())) {
      Optional<DeployComponentError> fatal =
          deployment.getErrors().stream()
              .filter(e -> e.getSeverity() == ErrorSeverityEnum.FATAL)
              .findFirst();
      if (fatal.isPresent()) {
        if (fatal.get().getReason().length() > MAX_REASON_MSG) {
          fatal.get().setReason(fatal.get().getReason().substring(0, MAX_REASON_MSG) + "...");
        }
        environmentClient.setReason(JsonToolkit.toJson(fatal.get()));
      }
    }
  }
}
