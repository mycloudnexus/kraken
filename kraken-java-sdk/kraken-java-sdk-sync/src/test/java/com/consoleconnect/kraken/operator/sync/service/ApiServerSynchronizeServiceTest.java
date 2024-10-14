package com.consoleconnect.kraken.operator.sync.service;

import static org.hamcrest.MatcherAssert.assertThat;

import com.consoleconnect.kraken.operator.core.dto.SimpleApiServerDto;
import com.consoleconnect.kraken.operator.core.ingestion.DataIngestionJob;
import com.consoleconnect.kraken.operator.core.model.HttpResponse;
import com.consoleconnect.kraken.operator.core.repo.UnifiedAssetRepository;
import com.consoleconnect.kraken.operator.core.toolkit.JsonToolkit;
import com.consoleconnect.kraken.operator.sync.CustomConfig;
import com.consoleconnect.kraken.operator.sync.model.SyncProperty;
import com.consoleconnect.kraken.operator.test.AbstractIntegrationTest;
import com.consoleconnect.kraken.operator.test.MockIntegrationTest;
import java.time.Duration;
import java.util.Arrays;
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
public class ApiServerSynchronizeServiceTest extends AbstractIntegrationTest {
  public static String url = "";
  static MockWebServer mockWebServer = new MockWebServer();
  @Autowired private SyncProperty syncProperty;
  @Autowired private DataIngestionJob dataIngestionJob;
  @Autowired private UnifiedAssetRepository unifiedAssetRepository;
  @Autowired ApplicationContext applicationContext;
  @Autowired ApiServerSynchronizeService apiServerSynchronizeService;

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
  void testSynApiServerInfo() {
    WebClient webClient = WebClient.builder().baseUrl(url).build();
    ApiServerSynchronizeService newdApiServerSynchronizeService = get(webClient);
    MockResponse mockResponse2 = new MockResponse();
    SimpleApiServerDto simpleApiServerDto = new SimpleApiServerDto();
    simpleApiServerDto.setApiServerKey("kraken.component.api-target-spec.001");
    simpleApiServerDto.setUrl("http://dev.com");
    mockResponse2.setBody(JsonToolkit.toJson(HttpResponse.ok(Arrays.asList(simpleApiServerDto))));
    mockResponse2.addHeader("Content-Type", "application/json");
    mockWebServer.enqueue(mockResponse2);
    newdApiServerSynchronizeService.synApiServerInfo();
    Mono.delay(Duration.ofMillis(200)).blockOptional();
    // enqueue again
    mockWebServer.enqueue(mockResponse2);
    newdApiServerSynchronizeService.synApiServerInfo();
    Mono.delay(Duration.ofMillis(300)).blockOptional();
    RecordedRequest recordedRequest = mockWebServer.takeRequest();
    assertThat(recordedRequest.getPath(), Matchers.is("/v2/callback/audits/api-servers"));
  }

  @Test
  @Order(2)
  @SneakyThrows
  void testApiServerSynchronizeService() {
    SimpleApiServerDto simpleApiServerDto = new SimpleApiServerDto();
    simpleApiServerDto.setApiServerKey("kraken.component.api-target-spec.001");
    simpleApiServerDto.setUrl("http://dev.com");
    HttpResponse<List<SimpleApiServerDto>> httpResponse =
        HttpResponse.ok(List.of(simpleApiServerDto));
    apiServerSynchronizeService.ingestData(httpResponse);
    // second
    apiServerSynchronizeService.ingestData(httpResponse);
    assertThat(simpleApiServerDto, Matchers.notNullValue());
  }

  private ApiServerSynchronizeService get(WebClient webClient) {
    return new ApiServerSynchronizeService(
        syncProperty, webClient, dataIngestionJob, unifiedAssetRepository, applicationContext);
  }
}
