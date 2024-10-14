package com.consoleconnect.kraken.operator.controller.v2;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import com.consoleconnect.kraken.operator.auth.security.UserContext;
import com.consoleconnect.kraken.operator.config.TestApplication;
import com.consoleconnect.kraken.operator.config.TestContextConstants;
import com.consoleconnect.kraken.operator.controller.WebTestClientHelper;
import com.consoleconnect.kraken.operator.controller.dto.CreateEnvRequest;
import com.consoleconnect.kraken.operator.controller.dto.CreateProductionUpgradeRequest;
import com.consoleconnect.kraken.operator.controller.dto.CreateUpgradeRequest;
import com.consoleconnect.kraken.operator.controller.model.Environment;
import com.consoleconnect.kraken.operator.controller.model.MgmtProperty;
import com.consoleconnect.kraken.operator.controller.service.EnvironmentService;
import com.consoleconnect.kraken.operator.controller.service.ProductDeploymentService;
import com.consoleconnect.kraken.operator.core.dto.Tuple2;
import com.consoleconnect.kraken.operator.core.dto.UnifiedAssetDto;
import com.consoleconnect.kraken.operator.core.enums.AssetKindEnum;
import com.consoleconnect.kraken.operator.core.enums.DeployStatusEnum;
import com.consoleconnect.kraken.operator.core.model.AppProperty;
import com.consoleconnect.kraken.operator.core.model.SyncMetadata;
import com.consoleconnect.kraken.operator.core.service.UnifiedAssetService;
import com.consoleconnect.kraken.operator.core.toolkit.AssetsConstants;
import com.consoleconnect.kraken.operator.core.toolkit.DateTime;
import com.consoleconnect.kraken.operator.core.toolkit.LabelConstants;
import com.consoleconnect.kraken.operator.core.toolkit.Paging;
import com.consoleconnect.kraken.operator.test.AbstractIntegrationTest;
import com.consoleconnect.kraken.operator.test.MockIntegrationTest;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
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
class TemplateUpgradeControllerTest extends AbstractIntegrationTest {
  public static final String PRODUCTION = "production";
  @Autowired UnifiedAssetService unifiedAssetService;
  @Autowired MgmtProperty mgmtProperty;
  @Autowired AppProperty appProperty;
  private final WebTestClientHelper testClientHelper;
  @Autowired ProductDeploymentService productDeploymentService;
  @Autowired EnvironmentService environmentService;

  @Autowired
  TemplateUpgradeControllerTest(WebTestClient webTestClient) {
    testClientHelper =
        new WebTestClientHelper(
            webTestClient.mutate().responseTimeout(Duration.ofSeconds(1000)).build());
  }

  @Test
  @Order(1)
  void givenErrorTemplateUpgradeId_WhnProductionUpgrade_thenReturn400() {
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
  @Order(1)
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
  @Order(1)
  void givenUpgradeId_WhenStageUpgrade_thenReturnOk() {
    UnifiedAssetDto assetDto =
        unifiedAssetService.findOne("mef.sonata.api-target-mapper.address.retrieve");
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
  @Order(2)
  @Sql(
      statements = {
        "update  kraken_asset set  status='SUCCESS' where kind in ('kraken.product-deployment','kraken.product.template-deployment')"
      })
  void givenStageUpgradeId_WhenProductionUpgrade_thenReturnOk() {
    CreateEnvRequest request = new CreateEnvRequest();
    request.setName(PRODUCTION);
    Environment productionEnv =
        environmentService.create(TestContextConstants.PRODUCT_ID, request, UserContext.ANONYMOUS);
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
    createUpgradeRequest.setProductEnvId(productionEnv.getId());
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
  @Order(3)
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
  @Order(4)
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
  @Order(5)
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
                  "$.data[0].mapperKey", equalTo("mef.sonata.api-target-mapper.address.retrieve")));
        });
  }

  @Test
  @Order(6)
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
          assertThat(body, hasJsonPath("$.data[0].releaseVersion", equalTo("V1.0.1")));
        });
  }

  @Test
  @Order(7)
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
    assertThat(successList, hasSize(2));
  }

  @Test
  @Order(8)
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
}
