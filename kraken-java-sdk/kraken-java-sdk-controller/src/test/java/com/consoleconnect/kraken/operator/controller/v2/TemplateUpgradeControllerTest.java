package com.consoleconnect.kraken.operator.controller.v2;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import com.consoleconnect.kraken.operator.config.TestApplication;
import com.consoleconnect.kraken.operator.config.TestContextConstants;
import com.consoleconnect.kraken.operator.controller.EnvCreator;
import com.consoleconnect.kraken.operator.controller.WebTestClientHelper;
import com.consoleconnect.kraken.operator.controller.dto.ClientMapperVersionPayloadDto;
import com.consoleconnect.kraken.operator.controller.dto.CreateProductionUpgradeRequest;
import com.consoleconnect.kraken.operator.controller.dto.CreateUpgradeRequest;
import com.consoleconnect.kraken.operator.controller.model.MgmtProperty;
import com.consoleconnect.kraken.operator.controller.service.ClientEventService;
import com.consoleconnect.kraken.operator.controller.service.EnvironmentService;
import com.consoleconnect.kraken.operator.controller.service.EvnClientMapperInfoService;
import com.consoleconnect.kraken.operator.controller.service.ProductDeploymentService;
import com.consoleconnect.kraken.operator.controller.service.upgrade.MgmtSourceUpgradeService;
import com.consoleconnect.kraken.operator.core.client.ClientEvent;
import com.consoleconnect.kraken.operator.core.client.ClientEventTypeEnum;
import com.consoleconnect.kraken.operator.core.dto.Tuple2;
import com.consoleconnect.kraken.operator.core.dto.UnifiedAssetDto;
import com.consoleconnect.kraken.operator.core.entity.EnvironmentClientEntity;
import com.consoleconnect.kraken.operator.core.entity.MgmtEventEntity;
import com.consoleconnect.kraken.operator.core.enums.*;
import com.consoleconnect.kraken.operator.core.event.IngestionDataResult;
import com.consoleconnect.kraken.operator.core.event.TemplateUpgradeResultEvent;
import com.consoleconnect.kraken.operator.core.model.AppProperty;
import com.consoleconnect.kraken.operator.core.model.HttpResponse;
import com.consoleconnect.kraken.operator.core.model.SyncMetadata;
import com.consoleconnect.kraken.operator.core.repo.EnvironmentClientRepository;
import com.consoleconnect.kraken.operator.core.repo.MgmtEventRepository;
import com.consoleconnect.kraken.operator.core.service.UnifiedAssetService;
import com.consoleconnect.kraken.operator.core.toolkit.*;
import com.consoleconnect.kraken.operator.test.AbstractIntegrationTest;
import com.consoleconnect.kraken.operator.test.MockIntegrationTest;
import com.fasterxml.jackson.core.type.TypeReference;
import java.nio.charset.Charset;
import java.time.Duration;
import java.util.*;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.reactive.server.WebTestClient;

@Slf4j
@MockIntegrationTest
@ContextConfiguration(classes = {TestApplication.class})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestClassOrder(ClassOrderer.OrderAnnotation.class)
@Order(101)
class TemplateUpgradeControllerTest extends AbstractIntegrationTest implements EnvCreator {
  public static final String MEF_SONATA_RELEASE_1_1_0_PUBLISHED =
      "mef.sonata.release@1.1.0.published";
  public static final String MEF_SONATA_API_TARGET_MAPPER_ADDRESS_RETRIEVE =
      "mef.sonata.api-target-mapper.address.retrieve";
  @Autowired UnifiedAssetService unifiedAssetService;
  @Autowired MgmtProperty mgmtProperty;
  @Autowired AppProperty appProperty;
  private final WebTestClientHelper testClientHelper;
  @Autowired ProductDeploymentService productDeploymentService;
  @Getter @Autowired EnvironmentService environmentService;
  @Autowired MgmtSourceUpgradeService mgmtSourceUpgradeService;
  @Autowired MgmtEventRepository mgmtEventRepository;
  @Autowired ClientEventService clientEventService;
  @Autowired EnvironmentClientRepository environmentClientRepository;
  @Autowired EvnClientMapperInfoService evnClientMapperInfoService;
  @Autowired EnvironmentClientRepository envClientRepository;

  @Autowired
  TemplateUpgradeControllerTest(WebTestClient webTestClient) {
    testClientHelper =
        new WebTestClientHelper(
            webTestClient.mutate().responseTimeout(Duration.ofSeconds(1000)).build());
  }

  @Test
  @Order(1)
  void givenErrorTemplateUpgradeId_WhenProductionUpgrade_thenReturn400() {
    String url = "/v2/products/{productId}/template-upgrade/production";
    CreateProductionUpgradeRequest createUpgradeRequest = new CreateProductionUpgradeRequest();
    createUpgradeRequest.setTemplateUpgradeId(UUID.randomUUID().toString());
    createUpgradeRequest.setStageEnvId(TestApplication.envId);
    createUpgradeRequest.setProductEnvId(TestApplication.envId);
    testClientHelper.requestAndVerify(
        HttpMethod.POST,
        uriBuilder ->
            uriBuilder.path(url).build(TestContextConstants.PRODUCT_ID, TestApplication.envId),
        400,
        createUpgradeRequest,
        body -> {
          assertThat(body, hasJsonPath("$.code", equalTo("invalidBody")));
        });
  }

  @Test
  @Order(2)
  void givenErrorTemplateUpgradeId_WhenStageUpgrade_thenReturn400() {
    String url = "/v2/products/{productId}/template-upgrade/stage";
    CreateUpgradeRequest createUpgradeRequest = new CreateUpgradeRequest();
    createUpgradeRequest.setTemplateUpgradeId(UUID.randomUUID().toString());
    createUpgradeRequest.setStageEnvId(TestApplication.envId);
    appProperty
        .getTenant()
        .setWorkspacePath("classpath:deployment-config/template-upgrade/kraken.yaml");
    testClientHelper.requestAndVerify(
        HttpMethod.POST,
        uriBuilder ->
            uriBuilder.path(url).build(TestContextConstants.PRODUCT_ID, TestApplication.envId),
        400,
        createUpgradeRequest,
        body -> {
          assertThat(body, hasJsonPath("$.code", equalTo("invalidBody")));
        });
  }

  @Test
  @Order(3)
  void givenUpgradeId_WhenStageUpgrade_thenReturnOk() {
    Optional<EnvironmentClientEntity> oneByEnvIdAndClientKeyAndKind =
        envClientRepository.findOneByEnvIdAndClientKeyAndKind(
            TestApplication.envId,
            MEF_SONATA_API_TARGET_MAPPER_ADDRESS_RETRIEVE,
            ClientEventTypeEnum.CLIENT_MAPPER_VERSION.name());
    if (oneByEnvIdAndClientKeyAndKind.isPresent()) {
      return;
    }
    UnifiedAssetDto assetDto =
        unifiedAssetService.findOne(MEF_SONATA_API_TARGET_MAPPER_ADDRESS_RETRIEVE);
    assetDto.getMetadata().setLabels(new HashMap<>());
    assetDto
        .getMetadata()
        .getLabels()
        .put(LabelConstants.LABEL_DEPLOYED_STATUS, LabelConstants.VALUE_DEPLOYED_STATUS_DEPLOYED);
    SyncMetadata syncMetadata = new SyncMetadata();
    syncMetadata.setSyncedAt(DateTime.nowInUTCString());
    unifiedAssetService.syncAsset(assetDto.getParentId(), assetDto, syncMetadata, true);
    String url = "/v2/products/{productId}/template-upgrade/stage";
    Paging<UnifiedAssetDto> assetDtoPaging =
        unifiedAssetService.findBySpecification(
            List.of(
                Tuple2.of(
                    AssetsConstants.FIELD_KIND, AssetKindEnum.PRODUCT_TEMPLATE_UPGRADE.getKind())),
            null,
            null,
            null,
            null);
    CreateUpgradeRequest createUpgradeRequest = new CreateUpgradeRequest();
    createUpgradeRequest.setTemplateUpgradeId(assetDtoPaging.getData().get(0).getId());
    createUpgradeRequest.setStageEnvId(TestApplication.envId);
    appProperty
        .getTenant()
        .setWorkspacePath("classpath:deployment-config/template-upgrade/kraken.yaml");
    testClientHelper.postAndVerify(
        uriBuilder ->
            uriBuilder.path(url).build(TestContextConstants.PRODUCT_ID, TestApplication.envId),
        createUpgradeRequest,
        body -> {
          assertThat(body, hasJsonPath("$.code", equalTo(200)));
        });
  }

  @Test
  @Order(4)
  @Sql(
      statements = {
        "update  kraken_asset set  status='SUCCESS' where kind in ('kraken.product-deployment','kraken.product.template-deployment')"
      })
  void givenStageUpgradeId_WhenProductionUpgrade_thenReturnOk() {
    String url = "/v2/products/{productId}/template-upgrade/production";
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
            uriBuilder.path(url).build(TestContextConstants.PRODUCT_ID, TestApplication.envId),
        createUpgradeRequest,
        body -> {
          assertThat(body, hasJsonPath("$.code", equalTo(200)));
          Paging<UnifiedAssetDto> assetDtoPaging1 =
              unifiedAssetService.findBySpecification(
                  Tuple2.ofList(
                      AssetsConstants.FIELD_KIND,
                      AssetKindEnum.PRODUCT_TEMPLATE_DEPLOYMENT.getKind()),
                  null,
                  null,
                  PageRequest.of(0, 10),
                  null);
          MatcherAssert.assertThat(assetDtoPaging1.getData(), hasSize(2));
        });
  }

  @Test
  @Order(5)
  void givenStageUpgraded_WhenReportClientMapperVersion_thenReturnOk() {
    ClientMapperVersionPayloadDto clientMapperVersionPayloadDto =
        new ClientMapperVersionPayloadDto();
    clientMapperVersionPayloadDto.setMapperKey(MEF_SONATA_API_TARGET_MAPPER_ADDRESS_RETRIEVE);
    ClientEvent clientEvent =
        ClientEvent.of(
            MEF_SONATA_API_TARGET_MAPPER_ADDRESS_RETRIEVE,
            ClientEventTypeEnum.CLIENT_MAPPER_VERSION,
            List.of(clientMapperVersionPayloadDto));
    clientEventService.onEvent(TestApplication.envId, null, clientEvent);
    Optional<EnvironmentClientEntity> oneByEnvIdAndClientKeyAndKind =
        environmentClientRepository.findOneByEnvIdAndClientKeyAndKind(
            TestApplication.envId,
            MEF_SONATA_API_TARGET_MAPPER_ADDRESS_RETRIEVE,
            ClientReportTypeEnum.CLIENT_MAPPER_VERSION.name());
    assertThat(oneByEnvIdAndClientKeyAndKind.isPresent(), is(true));
  }

  @Test
  @Order(6)
  void givenProductId_whenListTemplateChangeLog_thenReturnData() {
    String url = "/v2/products/{productId}/template-upgrade/releases";
    testClientHelper.getAndVerify(
        uriBuilder ->
            uriBuilder.path(url).build(TestContextConstants.PRODUCT_ID, TestApplication.envId),
        body -> {
          assertThat(body, hasJsonPath("$.code", equalTo(200)));
          assertThat(body, hasJsonPath("$.data.data", hasSize(greaterThanOrEqualTo(1))));
        });
  }

  @Test
  @Order(7)
  void givenTemplateId_whenListApiUseCaseFromClasspath_thenReturnData() {
    Paging<UnifiedAssetDto> assetDtoPaging =
        unifiedAssetService.findBySpecification(
            Tuple2.ofList(
                AssetsConstants.FIELD_KIND, AssetKindEnum.PRODUCT_TEMPLATE_UPGRADE.getKind()),
            null,
            null,
            PageRequest.of(0, 1, Sort.Direction.DESC, AssetsConstants.FIELD_CREATE_AT),
            null);
    String url = "/v3/products/{productId}/template-upgrade/api-use-cases";
    testClientHelper.getAndVerify(
        uriBuilder ->
            uriBuilder
                .path(url)
                .queryParam("templateUpgradeId", assetDtoPaging.getData().get(0).getId())
                .build(TestContextConstants.PRODUCT_ID, TestApplication.envId),
        body -> {
          assertThat(body, hasJsonPath("$.code", equalTo(200)));
          assertThat(body, hasJsonPath("$.data", hasSize(greaterThanOrEqualTo(1))));
        });
  }

  @Test
  @Order(8)
  void givenStageUpgraded_whenListTemplateDeployment_thenReturnData() {
    String url = "/v2/products/{productId}/template-upgrade/template-deployments";
    testClientHelper.getAndVerify(
        uriBuilder ->
            uriBuilder.path(url).build(TestContextConstants.PRODUCT_ID, TestApplication.envId),
        body -> {
          assertThat(body, hasJsonPath("$.code", equalTo(200)));
          assertThat(body, hasJsonPath("$.data.data", hasSize(greaterThanOrEqualTo(1))));
        });
  }

  @Test
  @Order(9)
  void givenStageUpgraded_whenGetDetail_thenReturnData() {
    Paging<UnifiedAssetDto> assetDtoPaging =
        unifiedAssetService.findBySpecification(
            Tuple2.ofList(
                AssetsConstants.FIELD_KIND, AssetKindEnum.PRODUCT_TEMPLATE_DEPLOYMENT.getKind()),
            null,
            null,
            null,
            null);
    String url = "/v2/products/{productId}/template-upgrade/template-deployments/{deploymentId}";
    testClientHelper.getAndVerify(
        uriBuilder ->
            uriBuilder
                .path(url)
                .build(TestContextConstants.PRODUCT_ID, assetDtoPaging.getData().get(0).getId()),
        body -> {
          log.info("{}", body);
          assertThat(body, hasJsonPath("$.code", equalTo(200)));
          assertThat(body, hasJsonPath("$.data", hasSize(greaterThanOrEqualTo(1))));
          assertThat(
              body,
              hasJsonPath(
                  "$.data[0].mapperKey", containsString("mef.sonata.api-target-mapper.address")));
        });
  }

  @Test
  @Order(10)
  void givenStageDeployed_whenCurrentVersion_thenReturnData() {
    Paging<UnifiedAssetDto> assetDtoPaging =
        unifiedAssetService.findBySpecification(
            Tuple2.ofList(
                AssetsConstants.FIELD_KIND, AssetKindEnum.PRODUCT_TEMPLATE_DEPLOYMENT.getKind()),
            null,
            null,
            PageRequest.of(0, 1, Sort.by(Sort.Direction.ASC, AssetsConstants.FIELD_CREATE_AT)),
            null);
    UnifiedAssetDto unifiedAssetDto = assetDtoPaging.getData().get(0);
    unifiedAssetDto.getMetadata().setStatus(DeployStatusEnum.SUCCESS.name());
    unifiedAssetService.syncAsset(
        unifiedAssetDto.getParentId(),
        unifiedAssetDto,
        new SyncMetadata("", "", DateTime.nowInUTCString()),
        true);
    String url = "/v2/products/{productId}/template-upgrade/current-versions";
    testClientHelper.getAndVerify(
        uriBuilder -> uriBuilder.path(url).build(TestContextConstants.PRODUCT_ID),
        body -> {
          log.info("{}", body);
          assertThat(body, hasJsonPath("$.code", equalTo(200)));
          assertThat(body, hasJsonPath("$.data", hasSize(greaterThanOrEqualTo(1))));
          assertThat(body, hasJsonPath("$.data[0].productVersion", equalTo("V1.0.1")));
        });
  }

  @Test
  @Order(11)
  void givenProductDeployInProcess_whenReportDeploymentStatus_thenOk() {
    Paging<UnifiedAssetDto> assetDtoPaging =
        unifiedAssetService.findBySpecification(
            Tuple2.ofList(
                AssetsConstants.FIELD_KIND, AssetKindEnum.PRODUCT_TEMPLATE_DEPLOYMENT.getKind()),
            null,
            null,
            PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, AssetsConstants.FIELD_CREATE_AT)),
            null);
    UnifiedAssetDto unifiedAssetDto = assetDtoPaging.getData().get(0);
    Paging<UnifiedAssetDto> assetDtoPaging2 =
        unifiedAssetService.findBySpecification(
            Tuple2.ofList(AssetsConstants.FIELD_KIND, AssetKindEnum.PRODUCT_DEPLOYMENT.getKind()),
            Tuple2.ofList(LabelConstants.LABEL_APP_TEMPLATE_DEPLOYMENT_ID, unifiedAssetDto.getId()),
            null,
            PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, AssetsConstants.FIELD_CREATE_AT)),
            null);
    for (UnifiedAssetDto assetDto : assetDtoPaging2.getData()) {
      productDeploymentService.reportConfigurationReloadingResult(assetDto.getId());
    }
    Paging<UnifiedAssetDto> assetDtoPaging3 =
        unifiedAssetService.findBySpecification(
            Tuple2.ofList(
                AssetsConstants.FIELD_KIND, AssetKindEnum.PRODUCT_TEMPLATE_DEPLOYMENT.getKind()),
            null,
            null,
            PageRequest.of(0, 2, Sort.by(Sort.Direction.DESC, AssetsConstants.FIELD_CREATE_AT)),
            null);
    List<UnifiedAssetDto> successList =
        assetDtoPaging3.getData().stream()
            .filter(
                dto ->
                    DeployStatusEnum.SUCCESS.name().equalsIgnoreCase(dto.getMetadata().getStatus()))
            .toList();
    assertThat(successList, hasSize(greaterThanOrEqualTo(1)));
  }

  @Test
  @Order(12)
  void givenCompletedDeployment_whenQueryCanUpdate_thenOk() {
    String url = "/v2/products/{productId}/template-upgrade/allow-update-operations";
    testClientHelper.getAndVerify(
        uriBuilder -> uriBuilder.path(url).build(TestContextConstants.PRODUCT_ID),
        body -> {
          log.info("{}", body);
          assertThat(body, hasJsonPath("$.code", equalTo(200)));
          assertThat(body, hasJsonPath("$.data", equalTo(true)));
        });
  }

  @Test
  @Order(13)
  @Sql(
      statements = {
        "update  kraken_asset set  status='SUCCESS' where kind in ('kraken.product-deployment','kraken.product.template-deployment')",
        "UPDATE kraken_mgmt_system_info set status='RUNNING' where key='CONTROL_PLANE'"
      })
  void givenAfterClasspathStageUpgrade_whenStageUpgradeFromMgmt_thenOk() throws Exception {
    String releaseStr =
        IOUtils.toString(
            ClassLoader.getSystemResourceAsStream("data/latest-product-release.json"),
            Charset.defaultCharset());
    String publishStr =
        IOUtils.toString(
            ClassLoader.getSystemResourceAsStream("data/download-mapping-template.json"),
            Charset.defaultCharset());
    UnifiedAssetDto downloadAsset =
        JsonToolkit.fromJson(publishStr, new TypeReference<HttpResponse<UnifiedAssetDto>>() {})
            .getData();
    UnifiedAssetDto releaseAsset = JsonToolkit.fromJson(releaseStr, UnifiedAssetDto.class);
    // upgrade source
    releaseAsset
        .getMetadata()
        .getLabels()
        .put(LabelConstants.LABEL_UPGRADE_SOURCE, UpgradeSourceEnum.MGMT.name());
    IngestionDataResult releaseResult =
        unifiedAssetService.syncAsset(
            "mef.sonata", releaseAsset, new SyncMetadata("", "", DateTime.nowInUTCString()), true);
    downloadAsset
        .getMetadata()
        .getLabels()
        .put(
            LabelConstants.LABEL_APP_TEMPLATE_UPGRADE_ID,
            releaseResult.getData().getId().toString());
    unifiedAssetService.syncAsset(
        releaseAsset.getMetadata().getKey(),
        downloadAsset,
        new SyncMetadata("", "", DateTime.nowInUTCString()),
        true);

    String url = "/v2/products/{productId}/template-upgrade/stage";
    CreateUpgradeRequest createUpgradeRequest = new CreateUpgradeRequest();
    createUpgradeRequest.setTemplateUpgradeId(releaseResult.getData().getId().toString());
    createUpgradeRequest.setStageEnvId(TestApplication.envId);
    testClientHelper.postAndVerify(
        uriBuilder ->
            uriBuilder.path(url).build(TestContextConstants.PRODUCT_ID, TestApplication.envId),
        createUpgradeRequest,
        body -> {
          assertThat(body, hasJsonPath("$.code", equalTo(200)));
        });
  }

  @Test
  @Order(14)
  void givenStageUpgradeFromMgmtIn_Process_whenReport_thenOk() {
    mgmtEventRepository.findAll().stream()
        .filter(item -> MgmtEventType.TEMPLATE_UPGRADE_RESULT.equals(item.getEventType()))
        .filter(item -> item.getStatus().equalsIgnoreCase(EventStatusType.WAIT_TO_SEND.name()))
        .sorted(Comparator.comparing(MgmtEventEntity::getCreatedAt))
        .findFirst()
        .ifPresent(
            entity -> {
              TemplateUpgradeResultEvent resultEvent =
                  JsonToolkit.fromJson(
                      JsonToolkit.toJson(entity.getPayload()), TemplateUpgradeResultEvent.class);
              assertThat(resultEvent.getUpgradeBeginAt(), Matchers.notNullValue());
              assertThat(resultEvent.getUpgradeEndAt(), Matchers.nullValue());
              entity.setStatus(EventStatusType.DONE.name());
              mgmtEventRepository.save(entity);
            });
  }

  @Test
  @Order(15)
  void givenStageUpgradeFromMgmtIn_Process_whenListRelease_thenReturnData() {
    String url = "/v2/products/{productId}/template-upgrade/releases";
    testClientHelper.getAndVerify(
        uriBuilder ->
            uriBuilder.path(url).build(TestContextConstants.PRODUCT_ID, TestApplication.envId),
        body -> {
          assertThat(body, hasJsonPath("$.code", equalTo(200)));
          assertThat(body, hasJsonPath("$.data.data", hasSize(greaterThanOrEqualTo(2))));
          assertThat(body, hasJsonPath("$.data.data[0].showStageUpgradeButton", is(true)));
          assertThat(body, hasJsonPath("$.data.data[1].showProductionUpgradeButton", is(false)));
        });
  }

  @Test
  @Order(16)
  @Sql(
      statements = {
        "update  kraken_asset set  status='SUCCESS' where kind in ('kraken.product-deployment','kraken.product.template-deployment')"
      })
  void givenStageUpgradeFromMgmtCompleted_whenReport_thenOk() {
    UnifiedAssetDto templateUpgrade =
        unifiedAssetService.findOne(MEF_SONATA_RELEASE_1_1_0_PUBLISHED);

    UnifiedAssetDto deployment =
        unifiedAssetService
            .findBySpecification(
                Tuple2.ofList(
                    AssetsConstants.FIELD_KIND,
                    AssetKindEnum.PRODUCT_TEMPLATE_DEPLOYMENT.getKind()),
                Tuple2.ofList(
                    LabelConstants.LABEL_APP_TEMPLATE_UPGRADE_ID, templateUpgrade.getId()),
                null,
                null,
                null)
            .getData()
            .get(0);
    mgmtSourceUpgradeService.reportResult(templateUpgrade.getId(), deployment.getId());
    mgmtEventRepository.findAll().stream()
        .filter(item -> MgmtEventType.TEMPLATE_UPGRADE_RESULT.equals(item.getEventType()))
        .filter(ent -> ent.getStatus().equals(EventStatusType.WAIT_TO_SEND.name()))
        .sorted(Comparator.comparing(MgmtEventEntity::getCreatedAt))
        .findFirst()
        .ifPresent(
            entity -> {
              TemplateUpgradeResultEvent resultEvent =
                  JsonToolkit.fromJson(
                      JsonToolkit.toJson(entity.getPayload()), TemplateUpgradeResultEvent.class);
              assertThat(resultEvent.getUpgradeBeginAt(), nullValue());
              assertThat(resultEvent.getUpgradeEndAt(), Matchers.notNullValue());
              entity.setStatus(EventStatusType.DONE.name());
              mgmtEventRepository.save(entity);
            });
  }

  @Test
  @Order(17)
  void givenTemplateId_whenListApiUseCaseFromMgmt_thenReturnData() {
    Paging<UnifiedAssetDto> assetDtoPaging =
        unifiedAssetService.findBySpecification(
            Tuple2.ofList(
                AssetsConstants.FIELD_KIND, AssetKindEnum.PRODUCT_TEMPLATE_UPGRADE.getKind()),
            Tuple2.ofList(LabelConstants.LABEL_UPGRADE_SOURCE, UpgradeSourceEnum.MGMT.name()),
            null,
            PageRequest.of(0, 1, Sort.Direction.DESC, AssetsConstants.FIELD_CREATE_AT),
            null);
    String url = "/v3/products/{productId}/template-upgrade/api-use-cases";
    testClientHelper.getAndVerify(
        uriBuilder ->
            uriBuilder
                .path(url)
                .queryParam("templateUpgradeId", assetDtoPaging.getData().get(0).getId())
                .build(TestContextConstants.PRODUCT_ID, TestApplication.envId),
        body -> {
          assertThat(body, hasJsonPath("$.code", equalTo(200)));
          assertThat(body, hasJsonPath("$.data", hasSize(greaterThanOrEqualTo(1))));
        });
  }
}
