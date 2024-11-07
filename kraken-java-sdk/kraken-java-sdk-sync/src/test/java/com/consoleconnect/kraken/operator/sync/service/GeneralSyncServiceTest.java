package com.consoleconnect.kraken.operator.sync.service;

import static org.hamcrest.MatcherAssert.assertThat;

import com.consoleconnect.kraken.operator.core.dto.UnifiedAssetDto;
import com.consoleconnect.kraken.operator.core.enums.AssetKindEnum;
import com.consoleconnect.kraken.operator.core.model.HttpResponse;
import com.consoleconnect.kraken.operator.core.model.UnifiedAsset;
import com.consoleconnect.kraken.operator.core.model.facet.CompatibilityFacets;
import com.consoleconnect.kraken.operator.core.model.facet.SystemInfoFacets;
import com.consoleconnect.kraken.operator.core.service.UnifiedAssetService;
import com.consoleconnect.kraken.operator.core.toolkit.JsonToolkit;
import com.consoleconnect.kraken.operator.sync.CustomConfig;
import com.consoleconnect.kraken.operator.sync.model.SyncProperty;
import com.consoleconnect.kraken.operator.sync.service.sink.ClientSinkService;
import com.consoleconnect.kraken.operator.test.AbstractIntegrationTest;
import com.consoleconnect.kraken.operator.test.MockIntegrationTest;
import com.fasterxml.jackson.core.type.TypeReference;
import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import lombok.SneakyThrows;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@MockIntegrationTest
@ContextConfiguration(classes = {CustomConfig.class})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class GeneralSyncServiceTest extends AbstractIntegrationTest {

  public static String url = "";
  static MockWebServer mockWebServer = new MockWebServer();

  @Autowired ApplicationContext applicationContext;
  @Autowired GeneralSyncService generalSyncService;
  @Autowired private SyncProperty syncProperty;
  @Autowired private UnifiedAssetService unifiedAssetService;
  @Autowired List<ClientSinkService> clientSinkServiceList;

  @BeforeAll
  @SneakyThrows
  static void setUp() {
    mockWebServer.start();
    url = mockWebServer.url("").toString();
  }

  @AfterAll
  @SneakyThrows
  static void tearDown() {
    mockWebServer.shutdown();
  }

  @Test
  @Order(1)
  @SneakyThrows
  void givenBuyerInfo_whenSync_thenOK() {
    WebClient webClient = WebClient.builder().baseUrl(url).build();
    GeneralSyncService generalSyncService1 = get(webClient);
    final Dispatcher dispatcher =
        new Dispatcher() {

          @SneakyThrows
          @Override
          public MockResponse dispatch(RecordedRequest request) throws InterruptedException {
            String kind = request.getRequestUrl().queryParameter("kind");
            AssetKindEnum assetKindEnum =
                Arrays.stream(AssetKindEnum.values())
                    .filter(t -> t.getKind().equals(kind))
                    .findFirst()
                    .orElse(AssetKindEnum.PRODUCT);
            switch (assetKindEnum) {
              case PRODUCT_BUYER:
                return mockerBuyer();
              case PRODUCT_COMPATIBILITY:
                return mockCompatibility();
              case SYSTEM_INFO:
                return mockSystemInfo();
            }
            return new MockResponse().setResponseCode(404);
          }
        };
    mockWebServer.setDispatcher(dispatcher);
    generalSyncService1.syncServerAssets();
    Mono.delay(Duration.ofMillis(300)).blockOptional();
    for (ClientSinkService clientSinkService : clientSinkServiceList) {
      RecordedRequest recordedRequest = mockWebServer.takeRequest();
      assertThat(
          recordedRequest.getPath(),
          Matchers.containsString(clientSinkService.getKind().getKind()));
    }
  }

  private MockResponse mockCompatibility() {
    UnifiedAsset unifiedAsset =
        UnifiedAsset.of(
            AssetKindEnum.PRODUCT_COMPATIBILITY.getKind(),
            AssetKindEnum.PRODUCT_COMPATIBILITY.getKind(),
            AssetKindEnum.PRODUCT_COMPATIBILITY.getKind());
    UnifiedAssetDto unifiedAssetDto = new UnifiedAssetDto();
    BeanUtils.copyProperties(unifiedAsset, unifiedAssetDto);
    CompatibilityFacets compatibilityFacets = new CompatibilityFacets();
    compatibilityFacets.setCompatibility(Map.of("1.0.0", List.of("1.2.0")));
    unifiedAssetDto.setFacets(
        JsonToolkit.fromJson(
            JsonToolkit.toJson(compatibilityFacets), new TypeReference<Map<String, Object>>() {}));
    MockResponse mockResponse2 = new MockResponse();
    mockResponse2.setBody(
        JsonToolkit.toJson(HttpResponse.ok(Collections.singletonList(unifiedAssetDto))));
    mockResponse2.addHeader("Content-Type", "application/json");
    return mockResponse2;
  }

  private MockResponse mockSystemInfo() {
    UnifiedAsset unifiedAsset =
        UnifiedAsset.of(
            AssetKindEnum.SYSTEM_INFO.getKind(),
            AssetKindEnum.SYSTEM_INFO.getKind(),
            AssetKindEnum.SYSTEM_INFO.getKind());
    UnifiedAssetDto unifiedAssetDto = new UnifiedAssetDto();
    BeanUtils.copyProperties(unifiedAsset, unifiedAssetDto);
    SystemInfoFacets systemInfoFacets = new SystemInfoFacets();
    systemInfoFacets.setProductVersion("1.2.0");
    systemInfoFacets.setStatus(SystemInfoFacets.SystemStatus.RUNNING);
    unifiedAssetDto.setFacets(
        JsonToolkit.fromJson(
            JsonToolkit.toJson(systemInfoFacets), new TypeReference<Map<String, Object>>() {}));
    MockResponse mockResponse2 = new MockResponse();
    mockResponse2.setBody(
        JsonToolkit.toJson(HttpResponse.ok(Collections.singletonList(unifiedAssetDto))));
    mockResponse2.addHeader("Content-Type", "application/json");
    return mockResponse2;
  }

  private MockResponse mockerBuyer() throws IOException {
    MockResponse mockResponse2 = new MockResponse();
    String mockData = readFileToString("data/buyer-01.json");
    UnifiedAssetDto mockAsset =
        JsonToolkit.fromJson(mockData, new TypeReference<UnifiedAssetDto>() {});
    mockResponse2.setBody(
        JsonToolkit.toJson(HttpResponse.ok(Collections.singletonList(mockAsset))));
    mockResponse2.addHeader("Content-Type", "application/json");
    return mockResponse2;
  }

  private GeneralSyncService get(WebClient webClient) {
    return new GeneralSyncService(
        syncProperty, webClient, unifiedAssetService, clientSinkServiceList);
  }
}
