package com.consoleconnect.kraken.operator.auth;

import com.consoleconnect.kraken.operator.TestApplication;
import com.consoleconnect.kraken.operator.auth.helper.WebTestClientHelper;
import com.consoleconnect.kraken.operator.test.AbstractIntegrationTest;
import com.consoleconnect.kraken.operator.test.MockIntegrationTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;

@ActiveProfiles("test-auth-disabled")
@MockIntegrationTest
@ContextConfiguration(classes = {TestApplication.class})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Slf4j
class UserAuthDisabledTest extends AbstractIntegrationTest {

  private final WebTestClientHelper testClientHelper;

  @Autowired
  public UserAuthDisabledTest(WebTestClient webTestClient) {
    this.testClientHelper = new WebTestClientHelper(webTestClient);
  }

  @Order(1)
  @Test
  void givenNoLogin_whenAccess_thenOK() {
    testClientHelper.requestAndVerify(
        HttpMethod.GET,
        (uriBuilder -> uriBuilder.path("/assets").build()),
        HttpStatus.OK.value(),
        Assertions::assertNotNull);
  }
}
