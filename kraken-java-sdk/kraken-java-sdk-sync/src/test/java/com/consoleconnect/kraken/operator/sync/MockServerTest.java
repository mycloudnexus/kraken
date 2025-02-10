package com.consoleconnect.kraken.operator.sync;

import com.consoleconnect.kraken.operator.sync.model.SyncProperty;
import com.consoleconnect.kraken.operator.sync.service.security.ExternalSystemTokenProvider;
import com.consoleconnect.kraken.operator.test.AbstractIntegrationTest;
import com.consoleconnect.kraken.operator.test.MockIntegrationTest;
import com.consoleconnect.kraken.operator.test.MockServer;
import java.io.IOException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test-internal-token")
@MockIntegrationTest
public abstract class MockServerTest extends AbstractIntegrationTest {

  @Autowired protected ExternalSystemTokenProvider externalSystemTokenProvider;

  @Autowired protected SyncProperty syncProperty;

  protected static MockServer mockServer;

  @BeforeAll
  static void beforeAll() {
    mockServer = MockServer.create();
  }

  @BeforeEach
  public void before() {
    syncProperty.getControlPlane().setUrl(mockServer.getMockServerUrl());
  }

  @AfterAll
  static void afterAll() throws IOException {
    mockServer.dispose();
  }
}
