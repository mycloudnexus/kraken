package com.consoleconnect.kraken.operator.controller;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import com.consoleconnect.kraken.operator.auth.security.UserContext;
import com.consoleconnect.kraken.operator.config.TestApplication;
import com.consoleconnect.kraken.operator.controller.dto.ComponentExpandDTO;
import com.consoleconnect.kraken.operator.controller.model.Environment;
import com.consoleconnect.kraken.operator.controller.service.EnvironmentService;
import com.consoleconnect.kraken.operator.core.client.ClientEvent;
import com.consoleconnect.kraken.operator.core.client.ClientEventTypeEnum;
import com.consoleconnect.kraken.operator.core.client.ServerAPIDto;
import com.consoleconnect.kraken.operator.core.dto.UnifiedAssetDto;
import com.consoleconnect.kraken.operator.core.mapper.FacetsMapper;
import com.consoleconnect.kraken.operator.core.model.HttpResponse;
import com.consoleconnect.kraken.operator.core.model.SyncMetadata;
import com.consoleconnect.kraken.operator.core.model.UnifiedAsset;
import com.consoleconnect.kraken.operator.core.model.facet.ComponentAPITargetFacets;
import com.consoleconnect.kraken.operator.core.service.UnifiedAssetService;
import com.consoleconnect.kraken.operator.core.toolkit.DateTime;
import com.consoleconnect.kraken.operator.core.toolkit.JsonToolkit;
import com.consoleconnect.kraken.operator.core.toolkit.YamlToolkit;
import com.consoleconnect.kraken.operator.test.AbstractIntegrationTest;
import com.consoleconnect.kraken.operator.test.MockIntegrationTest;
import com.fasterxml.jackson.core.type.TypeReference;
import java.io.IOException;
import java.time.Duration;
import java.util.*;
import lombok.Getter;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.shaded.com.google.common.collect.Ordering;

@MockIntegrationTest
@ContextConfiguration(classes = {TestApplication.class})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ComponentMgmtControllerTest extends AbstractIntegrationTest
    implements APIServerCreator, EnvCreator {
  private static final Logger log = LoggerFactory.getLogger(ComponentMgmtControllerTest.class);
  @Autowired WebTestClient webTestClient;
  @Getter private final WebTestClientHelper testClientHelper;
  public static final String GET_DETAIL_MAPPING_URL =
      "products/mef.sonata.api.order/components/mef.sonata.api.order/mapper-details";
  public static final String LIST_VERSIONS_URL = "products/mef.sonata/component-versions";
  public static final String UPDATE_COMPONENT =
      "/products/kraken.component.api-target-mapper/components/{id}/targetMapper";
  public static final String LIST_API_USE_CASES =
      "/products/kraken.component.api-target-mapper/api-use-cases";
  @Autowired UnifiedAssetService unifiedAssetService;
  @Getter @Autowired EnvironmentService environmentService;

  @Autowired
  public ComponentMgmtControllerTest(WebTestClient webTestClient) {
    testClientHelper = new WebTestClientHelper(webTestClient);
  }

  @Order(9)
  @Test
  void givenRequest_whenRequestIsCorrect_thenSuccess() {
    UnifiedAssetDto asset =
        unifiedAssetService.findOne("mef.sonata.api-target-mapper.order.eline.add");
    webTestClient
        .mutate()
        .responseTimeout(Duration.ofSeconds(600))
        .build()
        .patch()
        .uri(
            uriBuilder ->
                uriBuilder
                    .path(UPDATE_COMPONENT)
                    .build("mef.sonata.api-target-mapper.order.eline.add"))
        .bodyValue(asset)
        .exchange()
        .expectBody()
        .consumeWith(
            response -> {
              String bodyStr = new String(Objects.requireNonNull(response.getResponseBody()));
              log.info("update result {}", bodyStr);
              assertThat(bodyStr, Matchers.notNullValue());
            });
  }

  @Test
  void givenMapper_whenChangePreDefinedProperty_thenThrowError() {
    String key = "mef.sonata.api-target-mapper.order.eline.add";
    UnifiedAssetDto asset = unifiedAssetService.findOne(key);
    ComponentAPITargetFacets facets = UnifiedAsset.getFacets(asset, ComponentAPITargetFacets.class);
    ComponentAPITargetFacets.Endpoint endpoint = facets.getEndpoints().get(0);

    endpoint.getMappers().getResponse().get(0).setTargetValues(new ArrayList<>());
    UnifiedAsset asset1 = new UnifiedAsset();
    asset1.setMetadata(asset.getMetadata());
    asset1.setFacets(
        JsonToolkit.fromJson(
            JsonToolkit.toJson(facets), new TypeReference<Map<String, Object>>() {}));
    assertUpdateErrorResult(asset1);

    ComponentAPITargetFacets facets2 =
        UnifiedAsset.getFacets(asset, ComponentAPITargetFacets.class);
    ComponentAPITargetFacets.Endpoint endpoint2 = facets2.getEndpoints().get(0);
    endpoint2.getMappers().getRequest().get(0).setSource(null);
    UnifiedAsset asset2 = new UnifiedAsset();
    asset2.setMetadata(asset.getMetadata());
    asset2.setFacets(
        JsonToolkit.fromJson(
            JsonToolkit.toJson(facets2), new TypeReference<Map<String, Object>>() {}));
    assertUpdateErrorResult(asset2);
  }

  private void assertUpdateErrorResult(Object payload) {
    webTestClient
        .mutate()
        .responseTimeout(Duration.ofSeconds(600))
        .build()
        .patch()
        .uri(
            uriBuilder ->
                uriBuilder
                    .path(UPDATE_COMPONENT)
                    .build("mef.sonata.api-target-mapper.order.eline.add"))
        .bodyValue(payload)
        .exchange()
        .expectStatus()
        .is4xxClientError()
        .expectBody()
        .consumeWith(
            response -> {
              String bodyStr = new String(Objects.requireNonNull(response.getResponseBody()));
              assertThat(bodyStr, Matchers.notNullValue());
            });
  }

  @Test
  void testCreateTargetSpec() {
    String path = "/products/mef.sonata/components/mef.sonata.api-target-mapper.address.retrieve";
    webTestClient
        .get()
        .uri(uriBuilder -> uriBuilder.path(path).build())
        .exchange()
        .expectStatus()
        .is2xxSuccessful()
        .expectBody()
        .consumeWith(
            response -> {
              String bodyStr = new String(Objects.requireNonNull(response.getResponseBody()));
              log.info("query result {}", bodyStr);
              assertThat(bodyStr, Matchers.notNullValue());
            });
  }

  @Order(10)
  @Test
  void giveMappingDetail_whenOrderBy_thenResultShouldBeOrdered() {
    webTestClient
        .mutate()
        .responseTimeout(Duration.ofSeconds(600))
        .build()
        .get()
        .uri(
            uriBuilder -> {
              uriBuilder.queryParam("envId", TestApplication.envId);
              return uriBuilder.path(GET_DETAIL_MAPPING_URL).build();
            })
        .exchange()
        .expectBody()
        .consumeWith(
            response -> {
              String bodyStr = new String(Objects.requireNonNull(response.getResponseBody()));
              log.info("result:{}", bodyStr);
              assertThat(bodyStr, Matchers.notNullValue());
              HttpResponse<ComponentExpandDTO> result =
                  JsonToolkit.fromJson(
                      bodyStr, new TypeReference<HttpResponse<ComponentExpandDTO>>() {});
              ComponentExpandDTO data = result.getData();
              assertThat(data, Matchers.notNullValue());
              Comparator<ComponentExpandDTO.TargetMappingDetail> comp =
                  (t1, t2) -> {
                    if (StringUtils.isBlank(t1.getOrderBy())
                        || StringUtils.isBlank(t2.getOrderBy())) {
                      return 0;
                    }
                    return t1.getOrderBy().compareTo(t2.getOrderBy());
                  };
              Assertions.assertTrue(isSorted(data.getDetails(), comp));
            });
  }

  private boolean isSorted(
      List<ComponentExpandDTO.TargetMappingDetail> details,
      Comparator<ComponentExpandDTO.TargetMappingDetail> comp) {
    return Ordering.from(comp).isOrdered(details);
  }

  @SneakyThrows
  @Test
  void givenRequestNewItems_whenUpdateTargetMapper_thenSuccess() {
    UnifiedAssetDto assetDto =
        unifiedAssetService.findOne("mef.sonata.api-target-mapper.order.eline.add");
    Optional<UnifiedAsset> mapperAssetOpt =
        YamlToolkit.parseYaml(
            readFileToString(
                "deployment-config/api-targets-mappers/api-target-mapper.order.eline.add.yaml"),
            UnifiedAsset.class);
    if (mapperAssetOpt.isPresent()) {
      UnifiedAsset targetMapperAsset = mapperAssetOpt.get();
      ComponentAPITargetFacets newFacets =
          UnifiedAsset.getFacets(targetMapperAsset, ComponentAPITargetFacets.class);
      ComponentAPITargetFacets.Endpoint newEndpoints = newFacets.getEndpoints().get(0);

      ComponentAPITargetFacets existFacets =
          UnifiedAsset.getFacets(assetDto, ComponentAPITargetFacets.class);
      ComponentAPITargetFacets.Endpoint existEndpoints = existFacets.getEndpoints().get(0);
      FacetsMapper.INSTANCE.toEndpoint(newEndpoints, existEndpoints);
    }
    log.info(JsonToolkit.toJson(assetDto));
    webTestClient
        .mutate()
        .responseTimeout(Duration.ofSeconds(600))
        .build()
        .patch()
        .uri(
            uriBuilder ->
                uriBuilder
                    .path(UPDATE_COMPONENT)
                    .build("mef.sonata.api-target-mapper.order.eline.add"))
        .bodyValue(assetDto)
        .exchange()
        .expectBody()
        .consumeWith(
            response -> {
              String bodyStr = new String(Objects.requireNonNull(response.getResponseBody()));
              log.info("mapper with duplicated request items update result {}", bodyStr);
              assertThat(bodyStr, hasJsonPath("$.data", notNullValue()));
            });
  }

  @SneakyThrows
  @Test
  void givenDifferentKey_whenUpdateTargetMapper_thenInvalidBody() {
    UnifiedAssetDto assetDto =
        unifiedAssetService.findOne("mef.sonata.api-target-mapper.order.uni.add");
    Optional<UnifiedAsset> mapperAssetOpt =
        YamlToolkit.parseYaml(
            readFileToString(
                "deployment-config/api-targets-mappers/api-target-mapper.order.eline.add.yaml"),
            UnifiedAsset.class);
    if (mapperAssetOpt.isPresent()) {
      UnifiedAsset targetMapperAsset = mapperAssetOpt.get();
      ComponentAPITargetFacets newFacets =
          UnifiedAsset.getFacets(targetMapperAsset, ComponentAPITargetFacets.class);
      ComponentAPITargetFacets.Endpoint newEndpoints = newFacets.getEndpoints().get(0);

      ComponentAPITargetFacets existFacets =
          UnifiedAsset.getFacets(assetDto, ComponentAPITargetFacets.class);
      ComponentAPITargetFacets.Endpoint existEndpoints = existFacets.getEndpoints().get(0);
      FacetsMapper.INSTANCE.toEndpoint(newEndpoints, existEndpoints);
    }
    log.info(JsonToolkit.toJson(assetDto));
    webTestClient
        .mutate()
        .responseTimeout(Duration.ofSeconds(600))
        .build()
        .patch()
        .uri(
            uriBuilder ->
                uriBuilder
                    .path(UPDATE_COMPONENT)
                    .build("mef.sonata.api-target-mapper.order.eline.add"))
        .bodyValue(assetDto)
        .exchange()
        .expectBody()
        .consumeWith(
            response -> {
              String bodyStr = new String(Objects.requireNonNull(response.getResponseBody()));
              assertThat(bodyStr, hasJsonPath("$.code", is("invalidBody")));
            });
  }

  @Test
  void givenProductId_whenListAllApiUseCases_thenReturnData() {
    webTestClient
        .get()
        .uri(uriBuilder -> uriBuilder.path(LIST_API_USE_CASES).build())
        .exchange()
        .expectBody()
        .consumeWith(
            response -> {
              String bodyStr = new String(Objects.requireNonNull(response.getResponseBody()));
              assertThat(bodyStr, hasJsonPath("$.data", hasSize(greaterThanOrEqualTo(2))));
            });
  }

  @SneakyThrows
  @Test
  @Order(11)
  void givenKeyOfAPISpec_whenQuerySpecDetail_thenResponseOK() {
    String serverKey = "mef.sonata.api-target-spec.con1718940696857";
    createAPIServer(PRODUCT_ID, COMPONENT_ID, serverKey);

    Environment envStage = createStage(PRODUCT_ID);
    Environment envProduction = createProduction(PRODUCT_ID);

    String mapperKey = "mef.sonata.api-target-mapper.quote.uni.add.sync";
    String assetPath =
        "/deployment-config/api-targets-mappers/api-target-mapper.quote.uni.add.sync.yaml";
    boolean exist = unifiedAssetService.existed(mapperKey);
    if (!exist) {
      createAsset(assetPath);
    }

    ClientEvent event = mockServerAPIEvent(serverKey, mapperKey);

    Map<String, String> headers = new HashMap<>();
    sendServerAPIEvents(event, envStage.getId(), headers);
    sendServerAPIEvents(event, envProduction.getId(), headers);

    String path = String.format("/products/%s/components/%s/spec-details", PRODUCT_ID, serverKey);
    getTestClientHelper()
        .getAndVerify(
            (uriBuilder -> uriBuilder.path(path).build()),
            bodyStr -> {
              log.info(bodyStr);
              assertThat(bodyStr, hasJsonPath("$.data.endpointUsage", notNullValue()));
              assertThat(
                  bodyStr, hasJsonPath("$.data.endpointUsage", hasKey("dataPlaneProduction")));
              assertThat(bodyStr, hasJsonPath("$.data.endpointUsage", hasKey("controlPlane")));
              assertThat(bodyStr, hasJsonPath("$.data.endpointUsage", hasKey("dataPlaneStage")));
            });
  }

  private void sendServerAPIEvents(ClientEvent event, String envId, Map<String, String> headers) {
    getTestClientHelper()
        .requestAndVerify(
            HttpMethod.POST,
            uriBuilder -> uriBuilder.path("/client/events").queryParam("env", envId).build(),
            headers,
            HttpStatus.OK.value(),
            event,
            Assertions::assertNotNull);
  }

  private ClientEvent mockServerAPIEvent(String serverKey, String mapperKey) {
    ClientEvent event = new ClientEvent();
    event.setClientId(UUID.randomUUID().toString());
    event.setEventType(ClientEventTypeEnum.CLIENT_SERVER_API);

    ServerAPIDto serverAPIDto = new ServerAPIDto();
    serverAPIDto.setServerKey(serverKey);
    serverAPIDto.setPath("/api/pricing/calculate");
    serverAPIDto.setMethod("post");
    serverAPIDto.setMapperKey(mapperKey);
    List<ServerAPIDto> serverAPIDtoList = List.of(serverAPIDto);
    event.setEventPayload(JsonToolkit.toJson(serverAPIDtoList));
    return event;
  }

  private void createAsset(String assetPath) throws IOException {
    Optional<UnifiedAsset> unifiedAsset =
        YamlToolkit.parseYaml(readFileToString(assetPath), UnifiedAsset.class);
    if (unifiedAsset.isPresent()) {
      UnifiedAsset data = unifiedAsset.get();
      SyncMetadata syncMetadata =
          new SyncMetadata("", "", DateTime.nowInUTCString(), UserContext.ANONYMOUS);
      unifiedAssetService.syncAsset(PRODUCT_ID, data, syncMetadata, true);
    }
  }

  @Test
  @Order(11)
  void givenProductId_whenQueryComponentProductCategories_thenReturnOK() {
    String path = String.format("/products/%s/product-categories", PRODUCT_ID);
    getTestClientHelper()
        .getAndVerify(
            (uriBuilder -> uriBuilder.path(path).build()),
            bodyStr -> {
              log.info(bodyStr);
              assertThat(bodyStr, hasJsonPath("$.data.componentKeys", notNullValue()));
              assertThat(bodyStr, hasJsonPath("$.data.productCategories", notNullValue()));
            });
  }
}
