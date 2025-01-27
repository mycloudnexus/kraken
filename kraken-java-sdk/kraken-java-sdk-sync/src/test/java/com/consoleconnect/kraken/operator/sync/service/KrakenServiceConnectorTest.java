package com.consoleconnect.kraken.operator.sync.service;

import com.consoleconnect.kraken.operator.core.client.ClientEvent;
import com.consoleconnect.kraken.operator.core.client.ClientEventTypeEnum;
import com.consoleconnect.kraken.operator.core.model.HttpResponse;
import com.consoleconnect.kraken.operator.core.toolkit.JsonToolkit;
import com.consoleconnect.kraken.operator.sync.MockServerTest;
import com.consoleconnect.kraken.operator.sync.model.SyncProperty;
import java.util.UUID;
import lombok.SneakyThrows;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

class KrakenServiceConnectorTest extends MockServerTest {

  static MockWebServer mockWebServer = new MockWebServer();

  @BeforeAll
  @SneakyThrows
  static void setUp() {
    mockWebServer.start();
  }

  @AfterAll
  @SneakyThrows
  static void tearDown() {
    mockWebServer.shutdown();
  }

  @SneakyThrows
  @Test
  void givenCorrectRequestPayload_whenPost_thenResponseOK() {
    String mockServerUrl = mockWebServer.url("").toString();

    WebClient webClient = WebClient.builder().baseUrl(mockServerUrl).build();

    KrakenServerConnector krakenServerConnector =
        new KrakenServerConnector(syncProperty, webClient);

    MockResponse mockResponse = new MockResponse();
    mockResponse.setResponseCode(200);
    mockResponse.setBody(JsonToolkit.toJson(HttpResponse.ok(null)));
    mockWebServer.enqueue(mockResponse);
    ClientEvent event =
        ClientEvent.of(UUID.randomUUID().toString(), ClientEventTypeEnum.CLIENT_HEARTBEAT, null);
    HttpResponse<Void> res = krakenServerConnector.pushEvent(event);
    Assertions.assertEquals(200, res.getCode());
    RecordedRequest recordedRequest = mockWebServer.takeRequest();
    Assertions.assertEquals(
        syncProperty.getControlPlane().getPushEventEndpoint(), recordedRequest.getPath());
    Assertions.assertEquals("POST", recordedRequest.getMethod());
    Assertions.assertEquals("Bearer 123456", recordedRequest.getHeader("Authorization"));
  }

  @SneakyThrows
  @Test
  void givenInCorrectRequestPayload_whenPost_thenResponseError() {
    String mockServerUrl = mockWebServer.url("").toString();
    SyncProperty syncProperty = new SyncProperty();
    syncProperty.getControlPlane().setUrl(mockServerUrl);

    WebClient webClient = WebClient.builder().baseUrl(mockServerUrl).build();

    KrakenServerConnector krakenServerConnector =
        new KrakenServerConnector(syncProperty, webClient);

    MockResponse mockResponse = new MockResponse();
    mockResponse.setResponseCode(400);
    mockResponse.setBody(JsonToolkit.toJson(HttpResponse.error(400, "Bad Request")));
    mockWebServer.enqueue(mockResponse);
    ClientEvent event =
        ClientEvent.of(UUID.randomUUID().toString(), ClientEventTypeEnum.CLIENT_HEARTBEAT, null);
    HttpResponse<Void> res = krakenServerConnector.pushEvent(event);
    Assertions.assertEquals(400, res.getCode());
    RecordedRequest recordedRequest = mockWebServer.takeRequest();
    Assertions.assertEquals(
        syncProperty.getControlPlane().getPushEventEndpoint(), recordedRequest.getPath());
    Assertions.assertEquals("POST", recordedRequest.getMethod());
    Assertions.assertEquals("Bearer 123456", recordedRequest.getHeader("Authorization"));
  }
}
