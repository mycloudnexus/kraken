package com.consoleconnect.kraken.operator.sync;

import com.consoleconnect.kraken.operator.sync.model.SyncProperty;
import com.consoleconnect.kraken.operator.test.MockServer;
import java.io.IOException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

public abstract class MockServerTest {
  protected static MockServer mockServer;
  protected static SyncProperty syncProperty;

  public static final String ACCESS_TOKEN = "Bearer 123456";

  @BeforeAll
  static void beforeAll() {
    mockServer = MockServer.create();
    syncProperty = new SyncProperty();
    SyncProperty.ControlPlane controlPlane = new SyncProperty.ControlPlane();
    controlPlane.setUrl(mockServer.getMockServerUrl());
    controlPlane.setToken(ACCESS_TOKEN);

    syncProperty.setControlPlane(controlPlane);
  }

  @AfterAll
  static void afterAll() throws IOException {
    mockServer.dispose();
  }
}
