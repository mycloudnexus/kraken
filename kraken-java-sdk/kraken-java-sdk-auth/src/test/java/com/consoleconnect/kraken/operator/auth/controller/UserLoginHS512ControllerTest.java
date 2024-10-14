package com.consoleconnect.kraken.operator.auth.controller;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.when;

import com.consoleconnect.kraken.operator.TestApplication;
import com.consoleconnect.kraken.operator.TestContextConstants;
import com.consoleconnect.kraken.operator.auth.dto.AuthRequest;
import com.consoleconnect.kraken.operator.auth.dto.AuthResponse;
import com.consoleconnect.kraken.operator.auth.dto.ResetPasswordRequest;
import com.consoleconnect.kraken.operator.auth.enums.AuthGrantTypeEnum;
import com.consoleconnect.kraken.operator.auth.helper.WebTestClientHelper;
import com.consoleconnect.kraken.operator.auth.model.AuthDataProperty;
import com.consoleconnect.kraken.operator.auth.security.UserContext;
import com.consoleconnect.kraken.operator.auth.service.UserService;
import com.consoleconnect.kraken.operator.core.exception.KrakenException;
import com.consoleconnect.kraken.operator.core.model.HttpResponse;
import com.consoleconnect.kraken.operator.core.toolkit.JsonToolkit;
import com.consoleconnect.kraken.operator.test.AbstractIntegrationTest;
import com.consoleconnect.kraken.operator.test.MockIntegrationTest;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.*;
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
class UserLoginHS512ControllerTest extends AbstractIntegrationTest {
  private final WebTestClientHelper testClientHelper;

  @SpyBean private AuthDataProperty.Login login;
  @SpyBean private AuthDataProperty.ResourceServer resourceServer;

  @Autowired private UserService userService;

  public static final String AUTH_ENDPOINT = "/auth/token";
  public static final String USERINFO_ENDPOINT = "/userinfo";
  public static final String RESET_PASSWORD_ENDPOINT = "/auth/resetPassword";

  @Autowired
  public UserLoginHS512ControllerTest(WebTestClient webTestClient) {
    this.testClientHelper = new WebTestClientHelper(webTestClient);
  }

  @BeforeEach
  void mockJwtTokenEncoder() {
    AuthDataProperty.JwtEncoderProperty encoderProperty = new AuthDataProperty.JwtEncoderProperty();
    encoderProperty.setIssuer("kraken-operator-sdk");
    encoderProperty.setKeyId("test-keyId");
    encoderProperty.setSecret(TestContextConstants.JWT_SECRET);
    when(login.getJwt()).thenReturn(encoderProperty);

    AuthDataProperty.JwtDecoderProperty decoderProperty = new AuthDataProperty.JwtDecoderProperty();
    decoderProperty.setIssuer("kraken-operator-sdk");
    decoderProperty.setKeyId("test-keyId");
    decoderProperty.setSecret(TestContextConstants.JWT_SECRET);
    when(resourceServer.getJwt()).thenReturn(List.of(decoderProperty));
  }

  @Test
  void givenCorrectCredential_whenLogin_thenGenerateAccessToken() {

    AuthRequest loginRequest = new AuthRequest();
    loginRequest.setEmail(TestContextConstants.LOGIN_USERNAME);
    loginRequest.setPassword(TestContextConstants.LOGIN_PASSWORD);
    loginRequest.setGrantType(AuthGrantTypeEnum.USERNAME_PASSWORD);
    testClientHelper.requestAndVerify(
        HttpMethod.POST,
        (uriBuilder -> uriBuilder.path(AUTH_ENDPOINT).build()),
        null,
        loginRequest,
        HttpStatus.OK.value(),
        bodyStr -> {
          Assertions.assertNotNull(bodyStr);
          assertThat(bodyStr, hasJsonPath("$.data", notNullValue()));
          assertThat(bodyStr, hasJsonPath("$.data.accessToken", notNullValue()));
          assertThat(bodyStr, hasJsonPath("$.data.refreshToken", notNullValue()));
        });
  }

  @Test
  void givenCorrectRefreshToken_whenRenew_thenGenerateAccessToken() {

    AuthRequest loginRequest = new AuthRequest();
    loginRequest.setEmail(TestContextConstants.LOGIN_USERNAME);
    loginRequest.setPassword(TestContextConstants.LOGIN_PASSWORD);
    loginRequest.setGrantType(AuthGrantTypeEnum.USERNAME_PASSWORD);
    Optional<String> resOptional =
        testClientHelper.requestAndVerify(
            HttpMethod.POST,
            (uriBuilder -> uriBuilder.path(AUTH_ENDPOINT).build()),
            null,
            loginRequest,
            HttpStatus.OK.value(),
            Assertions::assertNotNull);
    Assertions.assertTrue(resOptional.isPresent());
    HttpResponse<AuthResponse> userLoginResponse =
        JsonToolkit.fromJson(resOptional.get(), new TypeReference<>() {});

    AuthRequest refreshTokenRequest = new AuthRequest();
    refreshTokenRequest.setGrantType(AuthGrantTypeEnum.REFRESH_TOKEN);
    refreshTokenRequest.setRefreshToken(userLoginResponse.getData().getRefreshToken());
    testClientHelper.requestAndVerify(
        HttpMethod.POST,
        (uriBuilder -> uriBuilder.path(AUTH_ENDPOINT).build()),
        null,
        refreshTokenRequest,
        HttpStatus.OK.value(),
        bodyStr -> {
          Assertions.assertNotNull(bodyStr);
          assertThat(bodyStr, hasJsonPath("$.data", notNullValue()));
          assertThat(bodyStr, hasJsonPath("$.data.accessToken", notNullValue()));
          assertThat(bodyStr, hasJsonPath("$.data.refreshToken", notNullValue()));
        });
  }

  @Test
  void givenInCorrectCredential_whenLogin_thenReturn400() {
    AuthRequest loginRequest = new AuthRequest();
    loginRequest.setEmail(UUID.randomUUID().toString());
    loginRequest.setPassword(UUID.randomUUID().toString());
    testClientHelper.requestAndVerify(
        HttpMethod.POST,
        (uriBuilder -> uriBuilder.path(AUTH_ENDPOINT).build()),
        null,
        loginRequest,
        HttpStatus.UNAUTHORIZED.value(),
        Assertions::assertNotNull);
  }

  @Test
  void givenCorrectAccessToken_whenRetrieveMe_thenReturnOk() {

    // login and generate a access token
    AuthRequest loginRequest = new AuthRequest();
    loginRequest.setEmail(TestContextConstants.LOGIN_USERNAME);
    loginRequest.setPassword(TestContextConstants.LOGIN_PASSWORD);
    AuthResponse authResponse = userService.login(loginRequest);

    Map<String, String> headers = new HashMap<>();
    headers.put(
        UserContext.AUTHORIZATION_HEADER,
        UserContext.AUTHORIZATION_HEADER_PREFIX + authResponse.getAccessToken());
    testClientHelper.requestAndVerify(
        HttpMethod.GET,
        (uriBuilder -> uriBuilder.path(USERINFO_ENDPOINT).build()),
        headers,
        null,
        HttpStatus.OK.value(),
        bodyStr -> {
          Assertions.assertNotNull(bodyStr);
          assertThat(bodyStr, hasJsonPath("$.data", notNullValue()));
          assertThat(bodyStr, hasJsonPath("$.data.id", notNullValue()));
          assertThat(bodyStr, hasJsonPath("$.data.email", notNullValue()));
          assertThat(bodyStr, hasJsonPath("$.data.role", notNullValue()));
          assertThat(bodyStr, hasJsonPath("$.data.state", notNullValue()));
          assertThat(bodyStr, hasJsonPath("$.data.createdAt", notNullValue()));
        });
  }

  @Test
  void givenCorrectAccessToken_whenResetPassword_thenReturnOk() {

    // login and generate a access token
    AuthRequest loginRequest = new AuthRequest();
    loginRequest.setEmail(TestContextConstants.LOGIN_USERNAME);
    loginRequest.setPassword(TestContextConstants.LOGIN_PASSWORD);
    AuthResponse authResponse = userService.login(loginRequest);

    Map<String, String> headers = new HashMap<>();
    headers.put(
        UserContext.AUTHORIZATION_HEADER,
        UserContext.AUTHORIZATION_HEADER_PREFIX + authResponse.getAccessToken());

    // reset the password
    ResetPasswordRequest resetPasswordRequest = new ResetPasswordRequest();
    resetPasswordRequest.setPassword(UUID.randomUUID().toString());
    testClientHelper.requestAndVerify(
        HttpMethod.POST,
        (uriBuilder -> uriBuilder.path(RESET_PASSWORD_ENDPOINT).build()),
        headers,
        resetPasswordRequest,
        HttpStatus.OK.value(),
        Assertions::assertNotNull);

    // login via the old password should fail
    KrakenException unauthorized =
        Assertions.assertThrowsExactly(
            KrakenException.class, () -> userService.login(loginRequest));
    Assertions.assertEquals(HttpStatus.UNAUTHORIZED.value(), unauthorized.getCode());
    Assertions.assertEquals("Invalid password", unauthorized.getMessage());

    // login via the new password should work
    loginRequest.setPassword(resetPasswordRequest.getPassword());
    authResponse = userService.login(loginRequest);
    Assertions.assertNotNull(authResponse.getAccessToken());

    // reset the password back to the original password
    resetPasswordRequest.setPassword(TestContextConstants.LOGIN_PASSWORD);
    testClientHelper.requestAndVerify(
        HttpMethod.POST,
        (uriBuilder -> uriBuilder.path(RESET_PASSWORD_ENDPOINT).build()),
        headers,
        resetPasswordRequest,
        HttpStatus.OK.value(),
        Assertions::assertNotNull);

    // login via the original password should work
    loginRequest.setPassword(resetPasswordRequest.getPassword());
    authResponse = userService.login(loginRequest);
    Assertions.assertNotNull(authResponse.getAccessToken());
  }
}
