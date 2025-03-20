package com.consoleconnect.kraken.operator.sync.service;

import com.consoleconnect.kraken.operator.core.dto.UnifiedAssetDto;
import com.consoleconnect.kraken.operator.core.enums.AssetKindEnum;
import com.consoleconnect.kraken.operator.core.exception.KrakenDeploymentException;
import com.consoleconnect.kraken.operator.core.model.HttpResponse;
import com.consoleconnect.kraken.operator.core.model.Metadata;
import com.consoleconnect.kraken.operator.core.service.UnifiedAssetService;
import com.consoleconnect.kraken.operator.sync.CustomConfig;
import com.consoleconnect.kraken.operator.sync.model.SyncProperty;
import com.consoleconnect.kraken.operator.test.AbstractIntegrationTest;
import com.consoleconnect.kraken.operator.test.MockIntegrationTest;
import java.util.*;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.ContextConfiguration;

@MockIntegrationTest
@ContextConfiguration(classes = {CustomConfig.class})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PullDeploymentServiceTest extends AbstractIntegrationTest {

  @SpyBean private PullDeploymentService pullDeploymentService;

  @SpyBean private UnifiedAssetService unifiedAssetService;

  @Autowired private SyncProperty syncProperty;

  @Test
  void givenLatestProductRelease_whenHandle_thenSuccess() {

    // given
    Mockito.doReturn(HttpResponse.ok(null))
        .when(pullDeploymentService)
        .curl(Mockito.any(), Mockito.any(), Mockito.any());

    // mock retrieve latest releaseId
    String releaseId = UUID.randomUUID().toString();

    Mockito.doReturn(HttpResponse.ok(releaseId))
        .when(pullDeploymentService)
        .curl(
            Mockito.eq(HttpMethod.GET),
            Mockito.eq(syncProperty.getControlPlane().getLatestDeploymentEndpoint()),
            Mockito.isNull(),
            Mockito.any(ParameterizedTypeReference.class));

    // mock retrieve release details
    UnifiedAssetDto assetDto = new UnifiedAssetDto();
    assetDto.setId(UUID.randomUUID().toString());
    assetDto.setKind(AssetKindEnum.COMPONENT_API_TARGET.getKind());
    Metadata metadata = new Metadata();
    metadata.setKey(UUID.randomUUID().toString());
    assetDto.setMetadata(metadata);

    HttpResponse<List<UnifiedAssetDto>> response = new HttpResponse<>();
    response.setData(List.of(assetDto));
    response.setCode(200);

    Mockito.doReturn(response)
        .when(pullDeploymentService)
        .curl(
            Mockito.eq(HttpMethod.GET),
            Mockito.eq(
                String.format(
                    syncProperty.getControlPlane().getRetrieveProductReleaseDetailEndpoint(),
                    releaseId)),
            Mockito.isNull(),
            Mockito.any(ParameterizedTypeReference.class));

    // mock upload deployment status
    Mockito.doReturn(HttpResponse.ok(null)).when(pullDeploymentService).pushEvent(Mockito.any());

    // when
    pullDeploymentService.scheduledCheckLatestProductRelease();

    // verify latest releaseId has been downloaded
    Mockito.verify(pullDeploymentService, Mockito.times(1))
        .curl(
            Mockito.eq(HttpMethod.GET),
            Mockito.eq(syncProperty.getControlPlane().getLatestDeploymentEndpoint()),
            Mockito.isNull(),
            Mockito.any(ParameterizedTypeReference.class));

    // verify release details have been downloaded
    Mockito.verify(pullDeploymentService, Mockito.times(1))
        .curl(
            Mockito.eq(HttpMethod.GET),
            Mockito.eq(
                String.format(
                    syncProperty.getControlPlane().getRetrieveProductReleaseDetailEndpoint(),
                    releaseId)),
            Mockito.isNull(),
            Mockito.any(ParameterizedTypeReference.class));

    // verify deployment has been installed
    Mockito.verify(pullDeploymentService, Mockito.times(1)).ingestData(Mockito.any());

    // verify deployment status has been pushed
    Mockito.verify(pullDeploymentService, Mockito.times(1)).pushEvent(Mockito.any());
  }

  @Test
  void givenLatestProductRelease_whenDeployFail_thenUpdatedStatusToFailed() {

    // given
    Mockito.doReturn(HttpResponse.ok(null))
        .when(pullDeploymentService)
        .curl(Mockito.any(), Mockito.any(), Mockito.any());

    // mock retrieve latest releaseId
    String releaseId = UUID.randomUUID().toString();

    Mockito.doReturn(HttpResponse.ok(releaseId))
        .when(pullDeploymentService)
        .curl(
            Mockito.eq(HttpMethod.GET),
            Mockito.eq(syncProperty.getControlPlane().getLatestDeploymentEndpoint()),
            Mockito.isNull(),
            Mockito.any(ParameterizedTypeReference.class));

    // mock retrieve release details
    UnifiedAssetDto assetDto = new UnifiedAssetDto();
    assetDto.setId(UUID.randomUUID().toString());
    assetDto.setKind(AssetKindEnum.COMPONENT_API_TARGET.getKind());
    Metadata metadata = new Metadata();
    metadata.setKey(UUID.randomUUID().toString());
    assetDto.setMetadata(metadata);

    HttpResponse<List<UnifiedAssetDto>> response = new HttpResponse<>();
    response.setData(List.of(assetDto));
    response.setCode(200);

    Mockito.doReturn(response)
        .when(pullDeploymentService)
        .curl(
            Mockito.eq(HttpMethod.GET),
            Mockito.eq(
                String.format(
                    syncProperty.getControlPlane().getRetrieveProductReleaseDetailEndpoint(),
                    releaseId)),
            Mockito.isNull(),
            Mockito.any(ParameterizedTypeReference.class));

    // mock upload deployment status
    Mockito.doReturn(HttpResponse.ok(null)).when(pullDeploymentService).pushEvent(Mockito.any());

    // Mock deploy failure
    Mockito.doThrow(
            KrakenDeploymentException.internalFatalError(
                AssetKindEnum.COMPONENT_API_WORK_FLOW.getKind(), "Failed to deploy workflow"))
        .when(unifiedAssetService)
        .syncAsset(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean());

    // when
    pullDeploymentService.scheduledCheckLatestProductRelease();

    // verify latest releaseId has been downloaded
    Mockito.verify(pullDeploymentService, Mockito.times(1))
        .curl(
            Mockito.eq(HttpMethod.GET),
            Mockito.eq(syncProperty.getControlPlane().getLatestDeploymentEndpoint()),
            Mockito.isNull(),
            Mockito.any(ParameterizedTypeReference.class));

    // verify release details have been downloaded
    Mockito.verify(pullDeploymentService, Mockito.times(1))
        .curl(
            Mockito.eq(HttpMethod.GET),
            Mockito.eq(
                String.format(
                    syncProperty.getControlPlane().getRetrieveProductReleaseDetailEndpoint(),
                    releaseId)),
            Mockito.isNull(),
            Mockito.any(ParameterizedTypeReference.class));

    // verify deployment has been installed
    Mockito.verify(pullDeploymentService, Mockito.times(1)).ingestData(Mockito.any());

    // verify deployment status has been pushed
    Mockito.verify(pullDeploymentService, Mockito.times(1)).pushEvent(Mockito.any());
  }
}
