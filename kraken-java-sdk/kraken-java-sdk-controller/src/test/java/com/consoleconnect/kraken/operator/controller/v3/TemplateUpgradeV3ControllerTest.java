package com.consoleconnect.kraken.operator.controller.v3;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.consoleconnect.kraken.operator.config.TestApplication;
import com.consoleconnect.kraken.operator.config.TestContextConstants;
import com.consoleconnect.kraken.operator.controller.WebTestClientHelper;
import com.consoleconnect.kraken.operator.controller.dto.*;
import com.consoleconnect.kraken.operator.controller.service.ComponentTagService;
import com.consoleconnect.kraken.operator.controller.service.EnvironmentService;
import com.consoleconnect.kraken.operator.core.client.ClientEventTypeEnum;
import com.consoleconnect.kraken.operator.core.dto.Tuple2;
import com.consoleconnect.kraken.operator.core.dto.UnifiedAssetDto;
import com.consoleconnect.kraken.operator.core.entity.EnvironmentClientEntity;
import com.consoleconnect.kraken.operator.core.enums.AssetKindEnum;
import com.consoleconnect.kraken.operator.core.enums.DeployStatusEnum;
import com.consoleconnect.kraken.operator.core.event.IngestionDataResult;
import com.consoleconnect.kraken.operator.core.repo.EnvironmentClientRepository;
import com.consoleconnect.kraken.operator.core.repo.UnifiedAssetRepository;
import com.consoleconnect.kraken.operator.core.service.UnifiedAssetService;
import com.consoleconnect.kraken.operator.core.toolkit.AssetsConstants;
import com.consoleconnect.kraken.operator.core.toolkit.LabelConstants;
import com.consoleconnect.kraken.operator.core.toolkit.Paging;
import com.consoleconnect.kraken.operator.test.AbstractIntegrationTest;
import com.consoleconnect.kraken.operator.test.MockIntegrationTest;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.reactive.server.WebTestClient;

@Slf4j
@MockIntegrationTest
@ContextConfiguration(classes = {TestApplication.class})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestClassOrder(ClassOrderer.OrderAnnotation.class)
@Order(100)
public class TemplateUpgradeV3ControllerTest extends AbstractIntegrationTest {
  private final WebTestClientHelper testClientHelper;
  @Autowired UnifiedAssetService unifiedAssetService;
  @Autowired ComponentTagService componentTagService;
  public static final String MEF_SONATA_API_TARGET_MAPPER_ADDRESS_RETRIEVE =
      "mef.sonata.api-target-mapper.address.retrieve";

  public static final String CONTROL_UPGRADE_URL =
      "/v3/products/{productId}/template-upgrade/control-plane";
  public static final String STAGE_UPGRADE_URL = "/v3/products/{productId}/template-upgrade/stage";
  public static final String PRODUCTION_UPGRADE_URL =
      "/v3/products/{productId}/template-upgrade/production";
  @Autowired EnvironmentClientRepository envClientRepository;

  @Autowired EnvironmentService environmentService;
  @Autowired UnifiedAssetRepository unifiedAssetRepository;

  @Autowired
  TemplateUpgradeV3ControllerTest(WebTestClient webTestClient) {
    testClientHelper =
        new WebTestClientHelper(
            webTestClient.mutate().responseTimeout(Duration.ofSeconds(1000)).build());
  }

  @Test
  @Order(1)
  void givenTemplateUpgradeId_whenControlPlaneUpgrade_thenSuccess() {
    Paging<UnifiedAssetDto> assetDtoPaging =
        unifiedAssetService.findBySpecification(
            List.of(
                Tuple2.of(
                    AssetsConstants.FIELD_KIND, AssetKindEnum.PRODUCT_TEMPLATE_UPGRADE.getKind())),
            null,
            null,
            null,
            null);
    String templateId = assetDtoPaging.getData().get(0).getId();
    CreateControlPlaneUpgradeRequest planeUpgradeRequest = new CreateControlPlaneUpgradeRequest();
    planeUpgradeRequest.setTemplateUpgradeId(templateId);
    testClientHelper.postAndVerify(
        uriBuilder -> uriBuilder.path(CONTROL_UPGRADE_URL).build(TestContextConstants.PRODUCT_ID),
        planeUpgradeRequest,
        body -> {
          assertThat(body, hasJsonPath("$.code", equalTo(200)));
        });
  }

  @Test
  @Order(2)
  void givenControlPlaneUpgraded_whenStageUpgrade_thenSuccess() {
    IngestionDataResult mappingTag =
        componentTagService.createMappingTag(
            "mef.sonata.api.serviceability.address",
            MEF_SONATA_API_TARGET_MAPPER_ADDRESS_RETRIEVE,
            null);
    newClientMapperVersion(mappingTag, TestApplication.envId);
    newClientMapperVersion(mappingTag, TestApplication.productionEnvId);
    Paging<UnifiedAssetDto> assetDtoPaging =
        unifiedAssetService.findBySpecification(
            List.of(
                Tuple2.of(
                    AssetsConstants.FIELD_KIND, AssetKindEnum.PRODUCT_TEMPLATE_UPGRADE.getKind())),
            null,
            null,
            null,
            null);
    String templateId = assetDtoPaging.getData().get(0).getId();
    CreateUpgradeRequest createUpgradeRequest = new CreateUpgradeRequest();
    createUpgradeRequest.setTemplateUpgradeId(templateId);
    createUpgradeRequest.setStageEnvId(TestApplication.envId);
    testClientHelper.postAndVerify(
        uriBuilder -> uriBuilder.path(STAGE_UPGRADE_URL).build(TestContextConstants.PRODUCT_ID),
        createUpgradeRequest,
        body -> {
          assertThat(body, hasJsonPath("$.code", equalTo(200)));
        });
  }

  private void newClientMapperVersion(IngestionDataResult mappingTag, String envId) {
    Optional<EnvironmentClientEntity> oneByEnvIdAndClientKeyAndKind =
        envClientRepository.findOneByEnvIdAndClientKeyAndKind(
            envId,
            MEF_SONATA_API_TARGET_MAPPER_ADDRESS_RETRIEVE,
            ClientEventTypeEnum.CLIENT_MAPPER_VERSION.name());
    if (oneByEnvIdAndClientKeyAndKind.isPresent()) {
      return;
    }
    EnvironmentClientEntity environmentClientEntity = new EnvironmentClientEntity();
    environmentClientEntity.setKind(ClientEventTypeEnum.CLIENT_MAPPER_VERSION.name());
    environmentClientEntity.setClientKey(MEF_SONATA_API_TARGET_MAPPER_ADDRESS_RETRIEVE);
    environmentClientEntity.setEnvId(envId);
    environmentClientEntity.setUpdatedAt(ZonedDateTime.now());
    ClientMapperVersionPayloadDto payload = new ClientMapperVersionPayloadDto();
    payload.setTagId(mappingTag.getData().getId().toString());
    payload.setMapperKey(MEF_SONATA_API_TARGET_MAPPER_ADDRESS_RETRIEVE);
    environmentClientEntity.setPayload(payload);
    envClientRepository.save(environmentClientEntity);
  }

  @Test
  @Order(3)
  @Sql(
      statements = {
        "update  kraken_asset set  status='SUCCESS' where kind in ('kraken.product-deployment','kraken.product.template-deployment')"
      })
  void givenStageUpgraded_whenProductionUpgrade_thenSuccess() {
    Paging<UnifiedAssetDto> assetDtoPaging =
        unifiedAssetService.findBySpecification(
            Tuple2.ofList(
                AssetsConstants.FIELD_KIND, AssetKindEnum.PRODUCT_TEMPLATE_UPGRADE.getKind()),
            null,
            null,
            null,
            null);
    CreateProductionUpgradeRequest createUpgradeRequest = new CreateProductionUpgradeRequest();
    createUpgradeRequest.setTemplateUpgradeId(assetDtoPaging.getData().get(0).getId());
    createUpgradeRequest.setStageEnvId(TestApplication.envId);
    createUpgradeRequest.setProductEnvId(TestApplication.productionEnvId);
    testClientHelper.postAndVerify(
        uriBuilder ->
            uriBuilder
                .path(PRODUCTION_UPGRADE_URL)
                .build(TestContextConstants.PRODUCT_ID, TestApplication.envId),
        createUpgradeRequest,
        body -> {
          assertThat(body, hasJsonPath("$.code", equalTo(200)));
          unifiedAssetRepository
              .findByKindOrderByCreatedAtDesc(AssetKindEnum.PRODUCT_DEPLOYMENT.getKind())
              .forEach(
                  entity -> {
                    entity.setStatus(DeployStatusEnum.SUCCESS.name());
                    unifiedAssetRepository.save(entity);
                  });
          unifiedAssetRepository
              .findByKindOrderByCreatedAtDesc(AssetKindEnum.PRODUCT_TEMPLATE_DEPLOYMENT.getKind())
              .forEach(
                  entity -> {
                    // when integration tests, not allowed two production deployment
                    if (entity
                        .getLabels()
                        .get(LabelConstants.LABEL_ENV_ID)
                        .equalsIgnoreCase(TestApplication.productionEnvId)) {
                      entity.setKind("kraken.product.template-deployment-deleted");
                      unifiedAssetRepository.save(entity);
                    } else {
                      entity.setStatus(DeployStatusEnum.SUCCESS.name());
                      unifiedAssetRepository.save(entity);
                    }
                  });
        });
  }
}
