package com.consoleconnect.kraken.operator.gateway;

import com.consoleconnect.kraken.operator.test.AbstractIntegrationTest;
import lombok.SneakyThrows;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

public class AbstractMockServerIntegration extends AbstractIntegrationTest {
  public static String mockUrl = "";
  public static volatile MockWebServer mockWebServer;

  @BeforeAll
  @SneakyThrows
  static void setUp() {
    mockWebServer = new MockWebServer();
    mockWebServer.start();
    mockUrl = mockWebServer.url("").toString();
  }

  @AfterAll
  @SneakyThrows
  static void tearDown() {
    if (mockWebServer != null) {
      mockWebServer.shutdown();
    }
  }
}
