package com.consoleconnect.kraken.operator.sync.service;

import static org.hamcrest.MatcherAssert.assertThat;

import com.consoleconnect.kraken.operator.core.dto.UnifiedAssetDto;
import com.consoleconnect.kraken.operator.core.enums.AssetKindEnum;
import com.consoleconnect.kraken.operator.core.model.HttpResponse;
import com.consoleconnect.kraken.operator.core.service.UnifiedAssetService;
import com.consoleconnect.kraken.operator.core.toolkit.JsonToolkit;
import com.consoleconnect.kraken.operator.sync.CustomConfig;
import com.consoleconnect.kraken.operator.sync.model.SyncProperty;
import com.consoleconnect.kraken.operator.sync.service.security.ExternalSystemTokenProvider;
import com.consoleconnect.kraken.operator.test.AbstractIntegrationTest;
import com.consoleconnect.kraken.operator.test.MockIntegrationTest;
import com.fasterxml.jackson.core.type.TypeReference;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import lombok.SneakyThrows;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@MockIntegrationTest
@ContextConfiguration(classes = {CustomConfig.class})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class GeneralSyncServiceTest extends AbstractIntegrationTest {

  public static String url = "";
  static MockWebServer mockWebServer = new MockWebServer();

  @Autowired GeneralSyncService generalSyncService;
  @Autowired private SyncProperty syncProperty;
  @Autowired private UnifiedAssetService unifiedAssetService;
  @Autowired private List<ClientSyncHandler> clientSyncHandlers;
  @Autowired private ExternalSystemTokenProvider externalSystemTokenProvider;

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
  void givenAssetKind_whenSyncTrue_thenOK() {
    startSync(false);
  }

  @Test
  @Order(2)
  void givenAssetKind_whenSyncFalse_thenOK() {
    startSync(true);
  }

  @SneakyThrows
  private void startSync(boolean isEmpty) {
    WebClient webClient = WebClient.builder().baseUrl(url).build();
    GeneralSyncService generalSyncService1 = get(webClient);
    final Dispatcher dispatcher =
        new Dispatcher() {
          @SneakyThrows
          @Override
          public MockResponse dispatch(RecordedRequest request) throws InterruptedException {
            String kind = request.getRequestUrl().queryParameter("kind");
            AssetKindEnum assetKindEnum = AssetKindEnum.kindOf(kind);
            if (null == assetKindEnum) {
              assetKindEnum = AssetKindEnum.PRODUCT;
            }
            return switch (assetKindEnum) {
              case PRODUCT_BUYER -> mockerBuyer();
              case COMPONENT_SELLER_CONTACT -> mockerSellerContact(isEmpty);
              default -> new MockResponse().setResponseCode(404);
            };
          }
        };
    mockWebServer.setDispatcher(dispatcher);
    generalSyncService1.syncServerAssets();
    Mono.delay(Duration.ofMillis(300)).blockOptional();
    for (ClientSyncHandler clientSinkService : clientSyncHandlers) {
      RecordedRequest recordedRequest = mockWebServer.takeRequest();
      assertThat(
          recordedRequest.getPath(),
          Matchers.containsString(clientSinkService.getKind().getKind()));
    }
  }

  @SneakyThrows
  private MockResponse mockerBuyer() {
    MockResponse mockResponse = new MockResponse();
    String mockData = readFileToString("data/buyer-01.json");
    UnifiedAssetDto mockAsset = JsonToolkit.fromJson(mockData, new TypeReference<>() {});
    mockResponse.setBody(JsonToolkit.toJson(HttpResponse.ok(Collections.singletonList(mockAsset))));
    mockResponse.addHeader("Content-Type", "application/json");
    return mockResponse;
  }

  @SneakyThrows
  private MockResponse mockerSellerContact(boolean isEmpty) {
    List<UnifiedAssetDto> assets;
    if (isEmpty) {
      assets = Collections.emptyList();
    } else {
      String mockData = readFileToString("data/seller-contact-01.json");
      UnifiedAssetDto mockAsset = JsonToolkit.fromJson(mockData, new TypeReference<>() {});
      assets = Collections.singletonList(mockAsset);
    }
    MockResponse mockResponse = new MockResponse();
    mockResponse.setBody(JsonToolkit.toJson(HttpResponse.ok(assets)));
    mockResponse.addHeader("Content-Type", "application/json");
    return mockResponse;
  }

  private GeneralSyncService get(WebClient webClient) {
    return new GeneralSyncService(
        syncProperty,
        webClient,
        externalSystemTokenProvider,
        unifiedAssetService,
        clientSyncHandlers);
  }
}
