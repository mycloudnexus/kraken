package com.consoleconnect.kraken.operator.controller.handler;

import com.consoleconnect.kraken.operator.controller.entity.EnvironmentClientEntity;
import com.consoleconnect.kraken.operator.controller.repo.EnvironmentClientRepository;
import com.consoleconnect.kraken.operator.controller.service.ProductDeploymentService;
import com.consoleconnect.kraken.operator.core.client.ClientEvent;
import com.consoleconnect.kraken.operator.core.client.ClientEventTypeEnum;
import com.consoleconnect.kraken.operator.core.client.ClientInstanceDeployment;
import com.consoleconnect.kraken.operator.core.entity.UnifiedAssetEntity;
import com.consoleconnect.kraken.operator.core.enums.ClientReportTypeEnum;
import com.consoleconnect.kraken.operator.core.enums.DeployStatusEnum;
import com.consoleconnect.kraken.operator.core.model.HttpResponse;
import com.consoleconnect.kraken.operator.core.repo.UnifiedAssetRepository;
import com.consoleconnect.kraken.operator.core.toolkit.JsonToolkit;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
@Slf4j
public class ClientDeploymentEventHandler extends ClientEventHandler {
  private final EnvironmentClientRepository environmentClientRepository;
  private final UnifiedAssetRepository unifiedAssetRepository;
  private final ProductDeploymentService productDeploymentService;

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
            .findOneByEnvIdAndAndClientIpAndKind(
                envId, event.getClientId(), ClientReportTypeEnum.DEPLOY.name())
            .orElseGet(
                () -> {
                  EnvironmentClientEntity entity = new EnvironmentClientEntity();
                  entity.setEnvId(envId);
                  entity.setKind(ClientReportTypeEnum.DEPLOY.name());
                  entity.setClientIp(event.getClientId());
                  entity.setCreatedAt(ZonedDateTime.now());
                  entity.setCreatedBy(userId);
                  return entity;
                });
    environmentClientEntity.setStatus(deployment.getStatus());
    environmentClientEntity.setReason(deployment.getReason());
    environmentClientEntity.setUpdatedAt(ZonedDateTime.now());
    environmentClientEntity.setUpdatedBy(userId);
    environmentClientRepository.save(environmentClientEntity);

    Optional<UnifiedAssetEntity> deploymentOptional =
        unifiedAssetRepository.findById(UUID.fromString(deployment.getProductReleaseId()));

    if (deploymentOptional.isPresent()) {
      log.info(
          "report asset {} configuration  reloading result success",
          deployment.getProductReleaseId());
      deploymentOptional.get().setStatus(DeployStatusEnum.SUCCESS.name());
      unifiedAssetRepository.save(deploymentOptional.get());
      productDeploymentService.reportConfigurationReloadingResult(deployment.getProductReleaseId());
    } else {
      log.info(
          "report asset {} configuration  reloading result failed",
          deployment.getProductReleaseId());
    }

    return HttpResponse.ok(null);
  }
}
