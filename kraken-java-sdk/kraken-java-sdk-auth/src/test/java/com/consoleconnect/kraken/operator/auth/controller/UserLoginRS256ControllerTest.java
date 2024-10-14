package com.consoleconnect.kraken.operator.auth.controller;

import static org.mockito.Mockito.when;

import com.consoleconnect.kraken.operator.TestApplication;
import com.consoleconnect.kraken.operator.TestContextConstants;
import com.consoleconnect.kraken.operator.auth.dto.AuthRequest;
import com.consoleconnect.kraken.operator.auth.helper.WebTestClientHelper;
import com.consoleconnect.kraken.operator.auth.model.AuthDataProperty;
import com.consoleconnect.kraken.operator.test.AbstractIntegrationTest;
import com.consoleconnect.kraken.operator.test.MockIntegrationTest;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;

@ActiveProfiles("test-login-enabled")
@MockIntegrationTest
@ContextConfiguration(classes = {TestApplication.class})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Slf4j
class UserLoginRS256ControllerTest extends AbstractIntegrationTest {
  private final WebTestClientHelper testClientHelper;

  @SpyBean private AuthDataProperty.Login login;

  @Autowired
  public UserLoginRS256ControllerTest(WebTestClient webTestClient) {
    this.testClientHelper = new WebTestClientHelper(webTestClient);
  }

  @BeforeEach
  void mockJwtTokenEncoder() {
    AuthDataProperty.JwtEncoderProperty encoderProperty = new AuthDataProperty.JwtEncoderProperty();
    encoderProperty.setIssuer("kraken-operator-sdk");
    encoderProperty.setKeyId("test-keyId");
    encoderProperty.setPrivateKey(TestContextConstants.JWT_RSA_PRIVATE_KEY);
    when(login.getJwt()).thenReturn(encoderProperty);
  }

  @Test
  void givenCorrectCredential_whenLogin_thenGenerateAccessToken() {

    mockJwtTokenEncoder();
    AuthRequest loginRequest = new AuthRequest();
    loginRequest.setEmail(TestContextConstants.LOGIN_USERNAME);
    loginRequest.setPassword(TestContextConstants.LOGIN_PASSWORD);
    testClientHelper.requestAndVerify(
        HttpMethod.POST,
        (uriBuilder -> uriBuilder.path("/login").build()),
        null,
        loginRequest,
        HttpStatus.OK.value(),
        Assertions::assertNotNull);
  }

  @Test
  void givenInCorrectCredential_whenLogin_thenReturn400() {
    AuthRequest loginRequest = new AuthRequest();
    loginRequest.setEmail(UUID.randomUUID().toString());
    loginRequest.setPassword(UUID.randomUUID().toString());
    testClientHelper.requestAndVerify(
        HttpMethod.POST,
        (uriBuilder -> uriBuilder.path("/login").build()),
        null,
        loginRequest,
        HttpStatus.UNAUTHORIZED.value(),
        Assertions::assertNotNull);
  }
}
