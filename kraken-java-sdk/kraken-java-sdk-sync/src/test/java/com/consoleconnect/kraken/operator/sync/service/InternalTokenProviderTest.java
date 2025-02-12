package com.consoleconnect.kraken.operator.sync.service;

import com.consoleconnect.kraken.operator.sync.MockServerTest;
import com.consoleconnect.kraken.operator.sync.service.security.ExternalSystemTokenProvider;
import org.junit.jupiter.api.*;

class InternalTokenProviderTest extends MockServerTest {

  @Test
  void givenInternalTokenEnabled_whenGet_thenRespondInternalToken() {
    String resp = externalSystemTokenProvider.getToken();
    Assertions.assertEquals(
        resp,
        ExternalSystemTokenProvider.BEARER_TOKEN_PREFIX
            + syncProperty.getControlPlane().getAuth().getInternalToken().getAccessToken());
  }
}
