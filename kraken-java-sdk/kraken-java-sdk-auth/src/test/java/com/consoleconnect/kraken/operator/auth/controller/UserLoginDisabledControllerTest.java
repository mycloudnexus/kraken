package com.consoleconnect.kraken.operator.auth.controller;

import com.consoleconnect.kraken.operator.TestApplication;
import com.consoleconnect.kraken.operator.TestContextConstants;
import com.consoleconnect.kraken.operator.auth.dto.AuthRequest;
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

@ActiveProfiles("test-login-disabled")
@MockIntegrationTest
@ContextConfiguration(classes = {TestApplication.class})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Slf4j
class UserLoginDisabledControllerTest extends AbstractIntegrationTest {

  private final WebTestClientHelper testClientHelper;

  @Autowired
  public UserLoginDisabledControllerTest(WebTestClient webTestClient) {
    this.testClientHelper = new WebTestClientHelper(webTestClient);
  }

  @Order(1)
  @Test
  void givenCorrectPassword_whenLogin_thenNotFound() {
    AuthRequest jwtRequest = new AuthRequest();
    jwtRequest.setEmail(TestContextConstants.LOGIN_USERNAME);
    jwtRequest.setPassword(TestContextConstants.LOGIN_PASSWORD);

    testClientHelper.requestAndVerify(
        HttpMethod.POST,
        (uriBuilder -> uriBuilder.path("/login").build()),
        null,
        jwtRequest,
        HttpStatus.NOT_FOUND.value(),
        Assertions::assertNotNull);
  }
}
