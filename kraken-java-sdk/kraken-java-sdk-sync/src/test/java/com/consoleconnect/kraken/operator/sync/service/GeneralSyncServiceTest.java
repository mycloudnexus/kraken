package com.consoleconnect.kraken.operator.sync.service;

import static com.consoleconnect.kraken.operator.core.enums.AssetKindEnum.PRODUCT_BUYER;
import static org.hamcrest.MatcherAssert.assertThat;

import com.consoleconnect.kraken.operator.core.dto.UnifiedAssetDto;
import com.consoleconnect.kraken.operator.core.ingestion.DataIngestionJob;
import com.consoleconnect.kraken.operator.core.model.HttpResponse;
import com.consoleconnect.kraken.operator.core.repo.UnifiedAssetRepository;
import com.consoleconnect.kraken.operator.core.toolkit.JsonToolkit;
import com.consoleconnect.kraken.operator.sync.CustomConfig;
import com.consoleconnect.kraken.operator.sync.model.SyncProperty;
import com.consoleconnect.kraken.operator.test.AbstractIntegrationTest;
import com.consoleconnect.kraken.operator.test.MockIntegrationTest;
import com.fasterxml.jackson.core.type.TypeReference;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import lombok.SneakyThrows;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.*;
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
  @Autowired private DataIngestionJob dataIngestionJob;
  @Autowired GeneralSyncService generalSyncService;
  @Autowired private SyncProperty syncProperty;
  @Autowired private UnifiedAssetRepository unifiedAssetRepository;

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
    MockResponse mockResponse2 = new MockResponse();
    String mockData = readFileToString("data/buyer-01.json");
    UnifiedAssetDto mockAsset =
        JsonToolkit.fromJson(mockData, new TypeReference<UnifiedAssetDto>() {});
    mockResponse2.setBody(
        JsonToolkit.toJson(HttpResponse.ok(Collections.singletonList(mockAsset))));
    mockResponse2.addHeader("Content-Type", "application/json");
    mockWebServer.enqueue(mockResponse2);
    generalSyncService1.syncServerAssets();
    Mono.delay(Duration.ofMillis(300)).blockOptional();
    RecordedRequest recordedRequest = mockWebServer.takeRequest();
    assertThat(
        recordedRequest.getPath(),
        Matchers.is("/v2/callback/audits/sync-server-asset?kind=kraken.product-buyer&updatedAt"));
  }

  @Test
  @Order(2)
  @SneakyThrows
  void givenBuyerInfo_whenIngestion_thenOK() {
    String mockData = readFileToString("data/buyer-02.json");
    UnifiedAssetDto mockAsset =
        JsonToolkit.fromJson(mockData, new TypeReference<UnifiedAssetDto>() {});
    HttpResponse<Object> httpResponse = HttpResponse.ok(List.of(mockAsset));
    Assertions.assertDoesNotThrow(
        () -> generalSyncService.ingestData(httpResponse, PRODUCT_BUYER.getKind()));
  }

  private GeneralSyncService get(WebClient webClient) {
    return new GeneralSyncService(
        syncProperty, webClient, dataIngestionJob, unifiedAssetRepository, applicationContext);
  }
}
