package com.consoleconnect.kraken.operator.controller.handler;

import com.consoleconnect.kraken.operator.controller.service.ProductDeploymentService;
import com.consoleconnect.kraken.operator.controller.tools.DeploymentErrorHelper;
import com.consoleconnect.kraken.operator.core.client.ClientEvent;
import com.consoleconnect.kraken.operator.core.client.ClientEventTypeEnum;
import com.consoleconnect.kraken.operator.core.client.ClientInstanceDeployment;
import com.consoleconnect.kraken.operator.core.dto.DeployComponentError;
import com.consoleconnect.kraken.operator.core.entity.EnvironmentClientEntity;
import com.consoleconnect.kraken.operator.core.enums.ClientReportTypeEnum;
import com.consoleconnect.kraken.operator.core.model.HttpResponse;
import com.consoleconnect.kraken.operator.core.repo.EnvironmentClientRepository;
import com.consoleconnect.kraken.operator.core.repo.UnifiedAssetRepository;
import com.consoleconnect.kraken.operator.core.toolkit.JsonToolkit;
import java.time.ZonedDateTime;
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
    DeployComponentError error = DeploymentErrorHelper.extractFailReason(deployment.getErrors());
    environmentClientEntity.setReason(error != null ? error.getReason() : "");
    environmentClientRepository.save(environmentClientEntity);

    try {
      productDeploymentService.reportConfigurationReloadingResult(
          deployment.getProductReleaseId(), deployment.getStatus(), deployment.getErrors());
    } catch (Exception e) {
      log.error("Failed to reportConfigurationReloadingResult", e);
    }

    return HttpResponse.ok(null);
  }
}
