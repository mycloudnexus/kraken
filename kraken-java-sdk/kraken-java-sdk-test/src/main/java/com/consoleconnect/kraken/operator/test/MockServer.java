package com.consoleconnect.kraken.operator.test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClient;

public class MockServer {

  private final ObjectWriter objectWriter;
  private final WebClient webClient;
  private final MockWebServer server;

  private MockServer() {
    this.objectWriter = new ObjectMapper().writer();
    this.server = new MockWebServer();
    this.webClient = WebClient.builder().baseUrl(getMockServerUrl()).build();
  }

  public static MockServer create() {
    return new MockServer();
  }

  public void dispose() throws IOException {
    server.shutdown();
  }

  public WebClient getWebClient() {
    return webClient;
  }

  public String getMockServerUrl() {
    return server.url("").toString();
  }

  public MockServer responseWith(HttpStatus status) {
    server.enqueue(new MockResponse().setResponseCode(status.value()));
    return this;
  }

  public <T> MockServer responseWith(
      HttpStatus status, @NotNull T responseBody, Map<String, String> headers) {
    MockResponse response =
        new MockResponse()
            .setResponseCode(status.value())
            .setBody(Objects.requireNonNull(toJson(responseBody)));
    response.addHeader("Content-Type", "application/json");
    if (headers != null) headers.forEach(response::addHeader);

    server.enqueue(response);

    return this;
  }

  private <T> String toJson(T value) {
    try {
      return objectWriter.writeValueAsString(value);
    } catch (JsonProcessingException e) {
      return null;
    }
  }

  public <T> ResponseVerifier call(ClientDelegate<T> clientDelegate) {
    return new ResponseVerifier(this, clientDelegate.call());
  }

  public RequestVerifier takeRequest() {
    try {
      RecordedRequest request = server.takeRequest(1000, TimeUnit.MILLISECONDS);
      return new RequestVerifier(request);
    } catch (InterruptedException ex) {
      throw new IllegalStateException(ex);
    }
  }

  public void clearRequest() {
    try {
      server.takeRequest(1000, TimeUnit.MILLISECONDS);
    } catch (InterruptedException ex) {
      throw new IllegalStateException(ex);
    }
  }
}
