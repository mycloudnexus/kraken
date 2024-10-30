package com.consoleconnect.kraken.operator.controller.v2;

import static com.consoleconnect.kraken.operator.controller.ComponentMgmtControllerTest.GET_DETAIL_MAPPING_URL;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import com.consoleconnect.kraken.operator.config.TestApplication;
import com.consoleconnect.kraken.operator.config.TestContextConstants;
import com.consoleconnect.kraken.operator.controller.ComponentMgmtControllerTest;
import com.consoleconnect.kraken.operator.controller.WebTestClientHelper;
import com.consoleconnect.kraken.operator.controller.dto.*;
import com.consoleconnect.kraken.operator.controller.model.APIToken;
import com.consoleconnect.kraken.operator.controller.service.APITokenService;
import com.consoleconnect.kraken.operator.controller.service.ComponentTagService;
import com.consoleconnect.kraken.operator.controller.service.EnvironmentService;
import com.consoleconnect.kraken.operator.core.dto.UnifiedAssetDto;
import com.consoleconnect.kraken.operator.core.entity.UnifiedAssetEntity;
import com.consoleconnect.kraken.operator.core.enums.AssetKindEnum;
import com.consoleconnect.kraken.operator.core.enums.DeployStatusEnum;
import com.consoleconnect.kraken.operator.core.enums.ReleaseKindEnum;
import com.consoleconnect.kraken.operator.core.exception.KrakenException;
import com.consoleconnect.kraken.operator.core.model.HttpResponse;
import com.consoleconnect.kraken.operator.core.model.SyncMetadata;
import com.consoleconnect.kraken.operator.core.model.UnifiedAsset;
import com.consoleconnect.kraken.operator.core.model.facet.ComponentAPITargetFacets;
import com.consoleconnect.kraken.operator.core.repo.UnifiedAssetRepository;
import com.consoleconnect.kraken.operator.core.service.UnifiedAssetService;
import com.consoleconnect.kraken.operator.core.toolkit.JsonToolkit;
import com.consoleconnect.kraken.operator.core.toolkit.LabelConstants;
import com.consoleconnect.kraken.operator.core.toolkit.Paging;
import com.consoleconnect.kraken.operator.test.AbstractIntegrationTest;
import com.consoleconnect.kraken.operator.test.MockIntegrationTest;
import com.consoleconnect.kraken.operator.toolkit.TestConstant;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.hamcrest.Matchers;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;

@ActiveProfiles("test-auth-server-enabled")
@MockIntegrationTest
@ContextConfiguration(classes = TestApplication.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Slf4j
class ProductDeploymentControllerTest extends AbstractIntegrationTest {

  private static final String PRODUCT_BASE_PATH = "/v2/products";
  private static final String PRODUCT_ID = TestContextConstants.PRODUCT_ID;
  private static final String COMPONENT_ID = "mef.sonata.api.order";
  public static final String ALL_RUNNING_COMPONENTS_URL =
      "/products/" + PRODUCT_ID + "/running-components";
  public static final String QUERY_COMPONENT_EXPAND_INFO_URL =
      "/products/" + PRODUCT_ID + "/components/mef.sonata.api.order/mapper-details";
  public static final String RETRIEVE_API_MAPPER_DEPLOYMENTS_URL =
      "/v2/products/" + PRODUCT_ID + "/api-mapper-deployments";
  public static final String RETRIEVE_RUNNING_API_MAPPER_DEPLOYMENTS_URL =
      "/v2/products/" + PRODUCT_ID + "/running-api-mapper-deployments";
  public static final String RETRIEVE_RUNNING_VERSIONS_URL =
      "/products/" + PRODUCT_ID + "/components/mef.sonata.api.order/running-versions";
  public static final String LATEST_DEPLOYMENT_URL =
      "/v2/products/" + PRODUCT_ID + "/latest-running-api-mapper-deployments";

  private final WebTestClientHelper testClientHelper;

  @Autowired private ComponentTagService tagService;
  @Autowired EnvironmentService environmentService;
  @Autowired UnifiedAssetRepository unifiedAssetRepository;
  @Autowired UnifiedAssetService unifiedAssetService;
  @Autowired private APITokenService apiTokenService;
  @Autowired private ComponentTagService componentTagService;

  @Autowired
  ProductDeploymentControllerTest(WebTestClient webTestClient) {
    testClientHelper = new WebTestClientHelper(webTestClient);
  }

  @Test
  @Order(1)
  void givenApiMapper_whenDeploy_thenReturnOk() {
    String path = String.format("%s/%s/api-mapper-deployments", PRODUCT_BASE_PATH, PRODUCT_ID);

    // create a component tag
    Optional<UnifiedAssetEntity> mapper =
        unifiedAssetRepository.findOneByKey(TestConstant.COMPONENT_ORDER_ELINE_ADD_MAPPER);
    UnifiedAssetEntity unifiedAssetEntity = mapper.orElse(null);
    Assertions.assertNotNull(unifiedAssetEntity);
    unifiedAssetEntity.setName("mapper name");
    unifiedAssetRepository.save(unifiedAssetEntity);

    CreateAPIMapperDeploymentRequest request = new CreateAPIMapperDeploymentRequest();
    request.setEnvId(TestApplication.envId);
    request.setComponentId(TestConstant.COMPONENT_ORDER);
    request.setMapperKeys(List.of(TestConstant.COMPONENT_ORDER_ELINE_ADD_MAPPER));

    testClientHelper.postAndVerify(
        (uriBuilder -> uriBuilder.path(path).build()),
        JsonToolkit.toJson(request),
        bodyStr -> {
          log.info("testApiMapperDeployment result {}", bodyStr);
          assertThat(bodyStr, hasJsonPath("$.data", notNullValue()));
        });
  }

  @Test
  @Order(1)
  void givenApiMapper_whenDeployAgain_thenReturnOK() {
    UnifiedAssetEntity deploymentAsset =
        unifiedAssetRepository
            .findByKindOrderByCreatedAtDesc(AssetKindEnum.PRODUCT_DEPLOYMENT.getKind())
            .stream()
            .filter(
                en ->
                    en.getLabels()
                        .get(LabelConstants.LABEL_RELEASE_KIND)
                        .equalsIgnoreCase(ReleaseKindEnum.API_LEVEL.getKind()))
            .findFirst()
            .orElseThrow(() -> KrakenException.notFound("asset not found"));
    deploymentAsset.setStatus(DeployStatusEnum.SUCCESS.name());
    unifiedAssetRepository.save(deploymentAsset);
    String path = String.format("%s/%s/api-mapper-deployments", PRODUCT_BASE_PATH, PRODUCT_ID);

    // create a component tag
    Optional<UnifiedAssetEntity> mapper =
        unifiedAssetRepository.findOneByKey(TestConstant.COMPONENT_ORDER_ELINE_ADD_MAPPER);
    UnifiedAssetEntity mapperAsset = mapper.orElse(null);
    Assertions.assertNotNull(mapperAsset);
    mapperAsset.setName("mapper name again");
    unifiedAssetRepository.save(mapperAsset);

    CreateAPIMapperDeploymentRequest request = new CreateAPIMapperDeploymentRequest();
    request.setEnvId(TestApplication.envId);
    request.setComponentId(TestConstant.COMPONENT_ORDER);
    request.setMapperKeys(List.of(TestConstant.COMPONENT_ORDER_ELINE_ADD_MAPPER));

    testClientHelper.postAndVerify(
        (uriBuilder -> uriBuilder.path(path).build()),
        JsonToolkit.toJson(request),
        bodyStr -> {
          assertThat(bodyStr, hasJsonPath("$.data", notNullValue()));
        });
  }

  @Order(2)
  @Test
  void givenAComponent_whenDeploy_thenReturnOK() {
    String path = String.format("%s/%s/deployments", PRODUCT_BASE_PATH, PRODUCT_ID);
    // create a component tag
    CreateTagRequest createTagRequest = new CreateTagRequest();
    createTagRequest.setTag("1.0.0");
    createTagRequest.setName("v1.0.0");
    createTagRequest.setDescription("this is a short description");
    Optional<UnifiedAssetEntity> mapper =
        unifiedAssetRepository.findOneByKey(TestConstant.COMPONENT_ORDER_ELINE_ADD_MAPPER);
    UnifiedAssetEntity mapperAsset = mapper.orElse(null);
    Assertions.assertNotNull(mapperAsset);
    mapperAsset.setName("mapper name");
    unifiedAssetRepository.save(mapper.get());

    String tagId =
        tagService.createTag(COMPONENT_ID, createTagRequest, null).getData().getId().toString();
    CreateProductDeploymentRequest request = new CreateProductDeploymentRequest();
    request.setEnvId(TestApplication.envId);
    request.setTagIds(List.of(tagId));

    testClientHelper.postAndVerify(
        (uriBuilder -> uriBuilder.path(path).build()),
        JsonToolkit.toJson(request),
        bodyStr -> {
          log.info("testCreateDeployment result {}", bodyStr);
          assertThat(bodyStr, hasJsonPath("$.data", notNullValue()));
        });
  }

  @Order(3)
  @Test
  void givenEmptyCondition_whenSearchDeployments_thenReturnOK() {
    String path = String.format("%s/%s/deployments", PRODUCT_BASE_PATH, PRODUCT_ID);
    testClientHelper.getAndVerify(
        (uriBuilder -> uriBuilder.path(path).build()),
        bodyStr -> {
          assertThat(bodyStr, hasJsonPath("$.data.data", hasSize(greaterThanOrEqualTo(3))));
        });
  }

  @Order(3)
  @Test
  void givenAllConditions_whenSearchDeployments_thenReturnOK() {
    String path = String.format("%s/%s/deployments", PRODUCT_BASE_PATH, PRODUCT_ID);
    testClientHelper.getAndVerify(
        (uriBuilder -> {
          uriBuilder.queryParam("envId", TestApplication.envId);
          uriBuilder.queryParam("componentId", TestConstant.COMPONENT_ORDER);
          return uriBuilder.path(path).build();
        }),
        bodyStr -> {
          log.info("testSearchDeployments result {}", bodyStr);
          assertThat(bodyStr, hasJsonPath("$.data.data", hasSize(1)));
        });
  }

  @Test
  @Order(4)
  void givenComponent_whenQueryLatestDeployment_thenReturnOK() {
    APIToken accessToken = TestApplication.createAccessToken(apiTokenService);
    String latestDeploymentPath = "/v2/callback/audits/deployments/latest";
    final Map<String, String> headers = new HashMap<>();
    testClientHelper.requestAndVerify(
        HttpMethod.GET,
        (uriBuilder ->
            uriBuilder
                .path(latestDeploymentPath)
                .queryParam("env", accessToken.getEnvId())
                .build()),
        null,
        HttpStatus.OK.value(),
        null,
        bodyStr -> {
          log.info("testQueryLatestDeployment result {}", bodyStr);
          assertThat(bodyStr, hasJsonPath("$.data", Matchers.notNullValue()));
          HttpResponse<String> response =
              JsonToolkit.fromJson(bodyStr, new TypeReference<HttpResponse<String>>() {});
          String latestDeploymentDetailPath =
              String.format("/v2/callback/audits/releases/%s/components", response.getData());
          testClientHelper.requestAndVerify(
              HttpMethod.GET,
              (uriBuilder ->
                  uriBuilder
                      .path(latestDeploymentDetailPath)
                      .queryParam("env", accessToken.getEnvId())
                      .build()),
              headers,
              HttpStatus.OK.value(),
              null,
              responseBody -> {
                assertThat(responseBody, hasJsonPath("$.data", hasSize(greaterThanOrEqualTo(3))));
              });
        });
  }

  @Order(5)
  @Test
  void givenEnvAndStatus_whenRetrieveDeployedComponents_thenReturnOK() {
    String path = "products/mef.sonata/deployments";
    testClientHelper.getAndVerify(
        (uriBuilder -> {
          uriBuilder.queryParam("envId", TestApplication.envId);
          uriBuilder.queryParam("status", DeployStatusEnum.IN_PROCESS.name());
          return uriBuilder.path(path).build();
        }),
        bodyStr -> {
          log.info("testRetrieveDeployedComponents result {}", bodyStr);
          assertThat(bodyStr, hasJsonPath("$.data.data", hasSize(1)));
        });
  }

  @Test
  @Order(6)
  void givenProduct_whenQueryComponentVersions_thenReturnOK() {
    String path = ComponentMgmtControllerTest.LIST_VERSIONS_URL;
    testClientHelper.getAndVerify(
        (uriBuilder -> uriBuilder.path(path).build()),
        bodyStr -> {
          log.info("testListVersions result {}", bodyStr);
          assertThat(bodyStr, hasJsonPath("$.data", hasSize(greaterThanOrEqualTo(1))));
        });
  }

  @Test
  @Order(7)
  void givenEnvAndProduct_whenQueryRunningComponents_thenReturnOK() {
    UnifiedAssetEntity deploymentAsset =
        unifiedAssetRepository
            .findByKindOrderByCreatedAtDesc(AssetKindEnum.PRODUCT_DEPLOYMENT.getKind())
            .stream()
            .filter(
                en ->
                    en.getLabels()
                        .get(LabelConstants.LABEL_RELEASE_KIND)
                        .equalsIgnoreCase(ReleaseKindEnum.COMPONENT_LEVEL.getKind()))
            .findFirst()
            .orElseThrow(() -> KrakenException.notFound("asset not found"));
    deploymentAsset.setStatus(DeployStatusEnum.SUCCESS.name());
    unifiedAssetRepository.save(deploymentAsset);
    testClientHelper.getAndVerify(
        (uriBuilder -> {
          uriBuilder.queryParam("envId", TestApplication.envId);
          return uriBuilder.path(ALL_RUNNING_COMPONENTS_URL).build();
        }),
        bodyStr -> {
          assertThat(bodyStr, hasJsonPath("$.data.data", hasSize(greaterThanOrEqualTo(1))));
          deploymentAsset.setStatus(DeployStatusEnum.IN_PROCESS.name());
          unifiedAssetRepository.save(deploymentAsset);
        });
  }

  @Test
  @Order(7)
  void givenEnvAndComponent_whenQueryRunningVersions_thenReturnOK() {
    UnifiedAssetEntity deploymentAsset =
        unifiedAssetRepository
            .findByKindOrderByCreatedAtDesc(AssetKindEnum.PRODUCT_DEPLOYMENT.getKind())
            .stream()
            .filter(
                en ->
                    en.getLabels()
                        .get(LabelConstants.LABEL_RELEASE_KIND)
                        .equalsIgnoreCase(ReleaseKindEnum.COMPONENT_LEVEL.getKind()))
            .findFirst()
            .orElseThrow(() -> KrakenException.notFound("asset not found"));
    deploymentAsset.setStatus(DeployStatusEnum.SUCCESS.name());
    unifiedAssetRepository.save(deploymentAsset);
    testClientHelper.getAndVerify(
        (uriBuilder -> {
          uriBuilder.queryParam("envId", TestApplication.envId);
          return uriBuilder.path(RETRIEVE_RUNNING_VERSIONS_URL).build();
        }),
        bodyStr -> {
          log.info("testRunningVersions result {}", bodyStr);
          assertThat(bodyStr, hasJsonPath("$.data", hasSize(greaterThanOrEqualTo(1))));
          deploymentAsset.setStatus(DeployStatusEnum.IN_PROCESS.name());
          unifiedAssetRepository.save(deploymentAsset);
        });
  }

  @Test
  @Order(8)
  void givenEnv_thenQueryComponentExpandInfo_thenReturnOK() {
    UnifiedAssetDto assetDto =
        unifiedAssetService.findOne(TestConstant.COMPONENT_ORDER_ELINE_ADD_MAPPER);
    ComponentAPITargetFacets facets =
        UnifiedAsset.getFacets(assetDto, ComponentAPITargetFacets.class);
    facets.getEndpoints().get(0).setId(" change the id");
    Map<String, Object> newFacet = new HashMap<>();
    newFacet.put("trigger", facets.getTrigger());
    newFacet.put("endpoints", facets.getEndpoints());
    assetDto.setFacets(newFacet);
    unifiedAssetService.syncAsset(
        assetDto.getParentId(), assetDto, new SyncMetadata("", "", ""), true);
    testClientHelper.getAndVerify(
        (uriBuilder -> {
          uriBuilder.queryParam("envId", TestApplication.envId);
          return uriBuilder.path(QUERY_COMPONENT_EXPAND_INFO_URL).build();
        }),
        bodyStr -> {
          log.info("testQueryComponentExpandInfo result {}", bodyStr);
          assertThat(bodyStr, hasJsonPath("$.data.details", hasSize(greaterThanOrEqualTo(1))));
          assertThat(bodyStr, hasJsonPath("$.data.details[0].diffWithStage", equalTo(true)));
        });
  }

  @Test
  @Order(9)
  void givenEnv_whenQueryApiMapperDeploymentHistory_thenReturnOK() {
    UnifiedAssetEntity deploymentAsset =
        unifiedAssetRepository
            .findByKindOrderByCreatedAtDesc(AssetKindEnum.PRODUCT_DEPLOYMENT.getKind())
            .stream()
            .filter(
                en ->
                    en.getLabels()
                        .get(LabelConstants.LABEL_RELEASE_KIND)
                        .equalsIgnoreCase(ReleaseKindEnum.API_LEVEL.getKind()))
            .findFirst()
            .orElseThrow(() -> KrakenException.notFound("asset not found"));
    deploymentAsset.setStatus(DeployStatusEnum.SUCCESS.name());
    unifiedAssetRepository.save(deploymentAsset);
    testClientHelper.getAndVerify(
        (uriBuilder -> {
          uriBuilder.queryParam("envId", TestApplication.envId);
          return uriBuilder.path(RETRIEVE_API_MAPPER_DEPLOYMENTS_URL).build();
        }),
        bodyStr -> {
          log.info("testRetrieveApiMapperDeployments result {}", bodyStr);
          deploymentAsset.setStatus(DeployStatusEnum.IN_PROCESS.name());
          unifiedAssetRepository.save(deploymentAsset);
          assertThat(bodyStr, hasJsonPath("$.data.data", hasSize(greaterThanOrEqualTo(1))));
        });
  }

  @Test
  @Order(10)
  void givenApiMapper_whenQueryRunningDeployments_thenReturnOK() {
    UnifiedAssetEntity deploymentAsset =
        unifiedAssetRepository
            .findByKindOrderByCreatedAtDesc(AssetKindEnum.PRODUCT_DEPLOYMENT.getKind())
            .stream()
            .filter(
                en ->
                    en.getLabels()
                        .get(LabelConstants.LABEL_RELEASE_KIND)
                        .equalsIgnoreCase(ReleaseKindEnum.API_LEVEL.getKind()))
            .findFirst()
            .orElseThrow(() -> KrakenException.notFound("asset not found"));
    deploymentAsset.setStatus(DeployStatusEnum.SUCCESS.name());
    unifiedAssetRepository.save(deploymentAsset);
    testClientHelper.getAndVerify(
        (uriBuilder -> {
          uriBuilder.queryParam("envId", TestApplication.envId);
          return uriBuilder.path(RETRIEVE_RUNNING_API_MAPPER_DEPLOYMENTS_URL).build();
        }),
        bodyStr -> {
          log.info("testRetrieveRunningApiMapperDeployments result {}", bodyStr);
          deploymentAsset.setStatus(DeployStatusEnum.IN_PROCESS.name());
          unifiedAssetRepository.save(deploymentAsset);
          assertThat(bodyStr, hasJsonPath("$.data", hasSize(greaterThanOrEqualTo(1))));
        });
  }

  @Test
  @Order(11)
  void givenMapperDeployment_whenVerifyInLabels_thenOK() {
    UnifiedAssetEntity deploymentAsset =
        unifiedAssetRepository
            .findByKindOrderByCreatedAtDesc(AssetKindEnum.PRODUCT_DEPLOYMENT.getKind())
            .stream()
            .filter(
                en ->
                    en.getLabels()
                        .get(LabelConstants.LABEL_RELEASE_KIND)
                        .equalsIgnoreCase(ReleaseKindEnum.API_LEVEL.getKind()))
            .findFirst()
            .orElseThrow(() -> KrakenException.notFound("asset not found"));
    deploymentAsset.setStatus(DeployStatusEnum.SUCCESS.name());
    unifiedAssetRepository.save(deploymentAsset);
    testClientHelper.getAndVerify(
        (uriBuilder -> {
          uriBuilder.queryParam("envId", TestApplication.envId);
          return uriBuilder.path(RETRIEVE_API_MAPPER_DEPLOYMENTS_URL).build();
        }),
        bodyStr -> {
          assertThat(bodyStr, hasJsonPath("$.data.data", hasSize(greaterThanOrEqualTo(1))));
          HttpResponse<Paging<ApiMapperDeploymentDTO>> mapperPages =
              JsonToolkit.fromJson(
                  bodyStr, new TypeReference<HttpResponse<Paging<ApiMapperDeploymentDTO>>>() {});
          List<ApiMapperDeploymentDTO> list = mapperPages.getData().getData();
          list.forEach(
              item -> {
                VerifyMapperRequest verifyRequestBody = new VerifyMapperRequest();
                verifyRequestBody.setTagId(item.getTagId());
                verifyRequestBody.setVerified(true);
                String verifyPath = "/v2/products/" + PRODUCT_ID + "/verify-api-mapper-in-labels";
                testClientHelper.patchAndVerify(
                    (uriBuilder -> uriBuilder.path(verifyPath).build()),
                    JsonToolkit.toJson(verifyRequestBody),
                    bodyStrVerified -> {
                      assertThat(bodyStrVerified, hasJsonPath("$.data", notNullValue()));
                    });
              });
          deploymentAsset.setStatus(DeployStatusEnum.IN_PROCESS.name());
          unifiedAssetRepository.save(deploymentAsset);
        });
  }

  @Test
  @Order(12)
  void givenValidStageDeployment_whenDeployToProduction_thenOK() {
    UnifiedAssetEntity deploymentAsset =
        unifiedAssetRepository
            .findByKindOrderByCreatedAtDesc(AssetKindEnum.PRODUCT_DEPLOYMENT.getKind())
            .stream()
            .filter(
                en ->
                    en.getLabels()
                        .get(LabelConstants.LABEL_RELEASE_KIND)
                        .equalsIgnoreCase(ReleaseKindEnum.API_LEVEL.getKind()))
            .findFirst()
            .orElseThrow(() -> KrakenException.notFound("asset not found"));
    deploymentAsset.setStatus(DeployStatusEnum.SUCCESS.name());
    unifiedAssetRepository.save(deploymentAsset);
    testClientHelper.getAndVerify(
        (uriBuilder -> {
          uriBuilder.queryParam("envId", TestApplication.envId);
          return uriBuilder.path(RETRIEVE_API_MAPPER_DEPLOYMENTS_URL).build();
        }),
        bodyStr -> {
          assertThat(bodyStr, hasJsonPath("$.data.data", hasSize(greaterThanOrEqualTo(1))));
          HttpResponse<Paging<ApiMapperDeploymentDTO>> mapperPages =
              JsonToolkit.fromJson(
                  bodyStr, new TypeReference<HttpResponse<Paging<ApiMapperDeploymentDTO>>>() {});
          List<ApiMapperDeploymentDTO> list = mapperPages.getData().getData();
          assertThat(list.size(), greaterThanOrEqualTo(2));
          ApiMapperDeploymentDTO item = list.get(1);
          Optional<UnifiedAssetEntity> tagAssetOpt =
              unifiedAssetRepository.findById(UUID.fromString(item.getTagId()));
          if (tagAssetOpt.isPresent()) {
            UnifiedAssetEntity tagAsset = tagAssetOpt.get();
            tagAsset.getLabels().put("version", "1.2");
            unifiedAssetRepository.save(tagAsset);
          }
          DeployToProductionRequest deployToProductionRequest = getDeployToProductionRequest(item);
          String deployToProductionPath =
              "/v2/products/" + PRODUCT_ID + "/deploy-stage-to-production";
          testClientHelper.postAndVerify(
              (uriBuilder -> uriBuilder.path(deployToProductionPath).build()),
              JsonToolkit.toJson(deployToProductionRequest),
              bodyStrVerified -> {
                assertThat(bodyStrVerified, hasJsonPath("$.data", notNullValue()));
              });
        });
  }

  @Test
  @Order(13)
  void givenLowerVersionStageDeployment_whenDeployToProduction_thenFailed() {
    List<UnifiedAssetEntity> unifiedAssetEntityList =
        unifiedAssetRepository
            .findByKindOrderByCreatedAtDesc(AssetKindEnum.PRODUCT_DEPLOYMENT.getKind())
            .stream()
            .filter(
                en ->
                    en.getLabels()
                        .get(LabelConstants.LABEL_RELEASE_KIND)
                        .equalsIgnoreCase(ReleaseKindEnum.API_LEVEL.getKind()))
            .toList();
    unifiedAssetEntityList.forEach(
        unifiedAssetEntity -> {
          unifiedAssetEntity.setStatus(DeployStatusEnum.SUCCESS.name());
          unifiedAssetRepository.save(unifiedAssetEntity);
        });
    testClientHelper.getAndVerify(
        (uriBuilder -> {
          uriBuilder.queryParam("envId", TestApplication.envId);
          return uriBuilder.path(RETRIEVE_API_MAPPER_DEPLOYMENTS_URL).build();
        }),
        bodyStr -> {
          assertThat(bodyStr, hasJsonPath("$.data.data", hasSize(greaterThanOrEqualTo(1))));
          HttpResponse<Paging<ApiMapperDeploymentDTO>> mapperPages =
              JsonToolkit.fromJson(
                  bodyStr, new TypeReference<HttpResponse<Paging<ApiMapperDeploymentDTO>>>() {});
          List<ApiMapperDeploymentDTO> list = mapperPages.getData().getData();
          assertThat(list.size(), greaterThanOrEqualTo(3));
          ApiMapperDeploymentDTO item = list.get(0);
          DeployToProductionRequest deployToProductionRequest = getDeployToProductionRequest(item);
          String deployToProductionPath =
              "/v2/products/" + PRODUCT_ID + "/deploy-stage-to-production";
          testClientHelper.postAndVerify(
              (uriBuilder -> uriBuilder.path(deployToProductionPath).build()),
              HttpStatus.BAD_REQUEST,
              JsonToolkit.toJson(deployToProductionRequest),
              bodyStrVerified -> {
                assertThat(bodyStrVerified, hasJsonPath("$.code", notNullValue()));
              });
        });
  }

  @Order(14)
  @Test
  void givenMapperDeployment_whenQueryLatestDeployment_thenReturnOK() {
    List<UnifiedAssetEntity> deploymentAssets =
        unifiedAssetRepository
            .findByKindOrderByCreatedAtDesc(AssetKindEnum.PRODUCT_DEPLOYMENT.getKind())
            .stream()
            .filter(
                en ->
                    en.getLabels()
                        .get(LabelConstants.LABEL_RELEASE_KIND)
                        .equalsIgnoreCase(ReleaseKindEnum.API_LEVEL.getKind()))
            .toList();
    deploymentAssets.forEach(
        unifiedAssetEntity -> {
          unifiedAssetEntity.setStatus(DeployStatusEnum.SUCCESS.name());
          unifiedAssetRepository.save(unifiedAssetEntity);
        });
    UnifiedAssetEntity assetEntity = deploymentAssets.get(0);
    String mapperKey =
        assetEntity.getLabels().entrySet().stream()
            .filter(entry -> entry.getKey().contains("target-mapper"))
            .findFirst()
            .map(Map.Entry::getKey)
            .orElse(null);
    testClientHelper.getAndVerify(
        (uriBuilder -> {
          uriBuilder.queryParam("mapperKey", mapperKey);
          return uriBuilder.path(LATEST_DEPLOYMENT_URL).build();
        }),
        bodyStr -> {
          log.info("latest deployment result {}", bodyStr);
          assertThat(bodyStr, hasJsonPath("$.data", notNullValue()));
        });
  }

  @Order(15)
  @Test
  void givenFailedMapperDeployment_whenQueryLatestDeployment_thenReturnOK() {
    List<UnifiedAssetEntity> deploymentAssets =
        unifiedAssetRepository
            .findByKindOrderByCreatedAtDesc(AssetKindEnum.PRODUCT_DEPLOYMENT.getKind())
            .stream()
            .filter(
                en ->
                    en.getLabels()
                        .get(LabelConstants.LABEL_RELEASE_KIND)
                        .equalsIgnoreCase(ReleaseKindEnum.API_LEVEL.getKind()))
            .toList();
    deploymentAssets.forEach(
        unifiedAssetEntity -> {
          unifiedAssetEntity.setStatus(DeployStatusEnum.FAILED.name());
          unifiedAssetRepository.save(unifiedAssetEntity);
        });
    UnifiedAssetEntity assetEntity = deploymentAssets.get(0);
    String mapperKey =
        assetEntity.getLabels().entrySet().stream()
            .filter(entry -> entry.getKey().contains("target-mapper"))
            .findFirst()
            .map(Map.Entry::getKey)
            .orElse(null);
    testClientHelper.getAndVerify(
        (uriBuilder -> {
          uriBuilder.queryParam("mapperKey", mapperKey);
          return uriBuilder.path(LATEST_DEPLOYMENT_URL).build();
        }),
        bodyStr -> {
          log.info("failed deployment latest result:{}", bodyStr);
          assertThat(bodyStr, hasJsonPath("$.data", notNullValue()));
        });
  }

  @Test
  @Order(16)
  void givenMapperDeployment_whenUnVerifyInLabels_thenReturnOK() {
    UnifiedAssetEntity deploymentAsset =
        unifiedAssetRepository
            .findByKindOrderByCreatedAtDesc(AssetKindEnum.PRODUCT_DEPLOYMENT.getKind())
            .stream()
            .filter(
                en ->
                    en.getLabels()
                        .get(LabelConstants.LABEL_RELEASE_KIND)
                        .equalsIgnoreCase(ReleaseKindEnum.API_LEVEL.getKind()))
            .findFirst()
            .orElseThrow(() -> KrakenException.notFound("asset not found"));
    deploymentAsset.setStatus(DeployStatusEnum.SUCCESS.name());
    unifiedAssetRepository.save(deploymentAsset);
    testClientHelper.getAndVerify(
        (uriBuilder -> {
          uriBuilder.queryParam("envId", TestApplication.envId);
          return uriBuilder.path(RETRIEVE_API_MAPPER_DEPLOYMENTS_URL).build();
        }),
        bodyStr -> {
          assertThat(bodyStr, hasJsonPath("$.data.data", hasSize(greaterThanOrEqualTo(1))));
          HttpResponse<Paging<ApiMapperDeploymentDTO>> mapperPages =
              JsonToolkit.fromJson(
                  bodyStr, new TypeReference<HttpResponse<Paging<ApiMapperDeploymentDTO>>>() {});
          List<ApiMapperDeploymentDTO> list = mapperPages.getData().getData();
          list.forEach(
              item -> {
                VerifyMapperRequest verifyRequestBody = new VerifyMapperRequest();
                verifyRequestBody.setTagId(item.getTagId());
                verifyRequestBody.setVerified(false);
                String verifyPath = "/v2/products/" + PRODUCT_ID + "/verify-api-mapper-in-labels";
                testClientHelper.patchAndVerify(
                    (uriBuilder -> uriBuilder.path(verifyPath).build()),
                    JsonToolkit.toJson(verifyRequestBody),
                    bodyStrVerified -> {
                      assertThat(bodyStrVerified, hasJsonPath("$.data", notNullValue()));
                      deploymentAsset.setStatus(DeployStatusEnum.IN_PROCESS.name());
                      unifiedAssetRepository.save(deploymentAsset);
                    });
              });
        });
  }

  private static @NotNull DeployToProductionRequest getDeployToProductionRequest(
      ApiMapperDeploymentDTO item) {
    DeployToProductionRequest deployToProductionRequest = new DeployToProductionRequest();
    // stage environment
    deployToProductionRequest.setSourceEnvId(item.getEnvId());
    // production environment
    deployToProductionRequest.setTargetEnvId(item.getEnvId());
    List<TagInfoDto> tagInfos = new ArrayList<>();
    TagInfoDto tagInfo = new TagInfoDto();
    tagInfo.setTagId(item.getTagId());
    tagInfos.add(tagInfo);
    deployToProductionRequest.setTagInfos(tagInfos);
    return deployToProductionRequest;
  }

  @Order(17)
  @Test
  void givenEnv_whenSearchMapperDetails_thenReturnOK() {
    testClientHelper.getAndVerify(
        (uriBuilder -> {
          uriBuilder.queryParam("envId", TestApplication.envId);
          return uriBuilder.path(GET_DETAIL_MAPPING_URL).build();
        }),
        bodyStr -> {
          log.info("mapper-details result {}", bodyStr);
          assertThat(bodyStr, hasJsonPath("$.data", notNullValue()));
        });
  }

  @Order(18)
  @Test
  void givenNotTagAsset_whenExtractMapperKey_thenReturnEmpty() {
    UnifiedAssetEntity deploymentAsset =
        unifiedAssetRepository
            .findByKindOrderByCreatedAtDesc(AssetKindEnum.PRODUCT_DEPLOYMENT.getKind())
            .stream()
            .filter(
                en ->
                    en.getLabels()
                        .get(LabelConstants.LABEL_RELEASE_KIND)
                        .equalsIgnoreCase(ReleaseKindEnum.COMPONENT_LEVEL.getKind()))
            .findFirst()
            .orElse(null);
    Assertions.assertNotNull(deploymentAsset);
    String result = componentTagService.extractMapperKeyFromTagAsset(deploymentAsset);
    Assertions.assertEquals("", result);
  }

  @Order(19)
  @Test
  void givenTagAssetNoMapperKey_whenExtractMapperKey_thenReturnOK() {
    UnifiedAssetEntity deploymentAsset =
        unifiedAssetRepository
            .findByKindOrderByCreatedAtDesc(AssetKindEnum.PRODUCT_DEPLOYMENT.getKind())
            .stream()
            .filter(
                en ->
                    en.getLabels()
                        .get(LabelConstants.LABEL_RELEASE_KIND)
                        .equalsIgnoreCase(ReleaseKindEnum.API_LEVEL.getKind()))
            .findFirst()
            .orElse(null);
    Assertions.assertNotNull(deploymentAsset);
    String tagId = deploymentAsset.getTags().stream().findFirst().orElse("");
    if (StringUtils.isNotBlank(tagId)) {
      unifiedAssetRepository
          .findById(UUID.fromString(tagId))
          .ifPresent(
              item -> {
                item.getLabels().remove("mapperKey");
                String result = componentTagService.extractMapperKeyFromTagAsset(item);
                Assertions.assertTrue(StringUtils.isNotBlank(result));
              });
    }
  }
}
