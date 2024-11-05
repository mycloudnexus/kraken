package com.consoleconnect.kraken.operator.auth.controller;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasNoJsonPath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;

import com.consoleconnect.kraken.operator.TestApplication;
import com.consoleconnect.kraken.operator.TestContextConstants;
import com.consoleconnect.kraken.operator.auth.dto.*;
import com.consoleconnect.kraken.operator.auth.enums.AuthGrantTypeEnum;
import com.consoleconnect.kraken.operator.auth.helper.WebTestClientHelper;
import com.consoleconnect.kraken.operator.auth.model.AuthDataProperty;
import com.consoleconnect.kraken.operator.auth.model.User;
import com.consoleconnect.kraken.operator.auth.security.UserContext;
import com.consoleconnect.kraken.operator.auth.service.UserService;
import com.consoleconnect.kraken.operator.core.model.HttpResponse;
import com.consoleconnect.kraken.operator.core.toolkit.JsonToolkit;
import com.consoleconnect.kraken.operator.test.AbstractIntegrationTest;
import com.consoleconnect.kraken.operator.test.MockIntegrationTest;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.*;
import java.util.function.Consumer;
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
class UserMgmtControllerTest extends AbstractIntegrationTest {
  private final WebTestClientHelper testClientHelper;

  @SpyBean private AuthDataProperty.Login login;
  @SpyBean private AuthDataProperty.ResourceServer resourceServer;

  @Autowired private UserService userService;

  @Autowired
  public UserMgmtControllerTest(WebTestClient webTestClient) {
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

  private String generateAdminAccessToken() {
    return generateAccessToken(
        TestContextConstants.ADMIN_LOGIN_USERNAME, TestContextConstants.LOGIN_PASSWORD);
  }

  private String generateUserAccessToken() {
    return generateAccessToken(
        TestContextConstants.LOGIN_USERNAME, TestContextConstants.LOGIN_PASSWORD);
  }

  private String generateAccessToken(String username, String password) {
    // login and generate a access token
    AuthRequest loginRequest = new AuthRequest();
    loginRequest.setEmail(username);
    loginRequest.setPassword(password);
    loginRequest.setGrantType(AuthGrantTypeEnum.USERNAME_PASSWORD);
    AuthResponse authResponse = userService.login(loginRequest);
    return authResponse.getAccessToken();
  }

  private Optional<String> createUser(
      String token, String email, int statusCode, Consumer<String> verify) {
    Map<String, String> headers = new HashMap<>();
    headers.put(UserContext.AUTHORIZATION_HEADER, UserContext.AUTHORIZATION_HEADER_PREFIX + token);

    CreateUserRequest createUserRequest = new CreateUserRequest();
    createUserRequest.setEmail(email);
    createUserRequest.setPassword(UUID.randomUUID().toString());
    createUserRequest.setRole("USER");
    createUserRequest.setName(UUID.randomUUID().toString());

    return testClientHelper.requestAndVerify(
        HttpMethod.POST,
        (uriBuilder -> uriBuilder.path("/users").build()),
        headers,
        createUserRequest,
        statusCode,
        verify);
  }

  @Test
  void givenCorrectAccessToken_whenAddNewUser_thenReturnOk() {

    createUser(
        generateAdminAccessToken(),
        UUID.randomUUID().toString(),
        HttpStatus.OK.value(),
        bodyStr -> {
          Assertions.assertNotNull(bodyStr);
          assertThat(bodyStr, hasJsonPath("$.data", notNullValue()));
          assertThat(bodyStr, hasJsonPath("$.data.id", notNullValue()));
          assertThat(bodyStr, hasJsonPath("$.data.email", notNullValue()));
          assertThat(bodyStr, hasJsonPath("$.data.role", notNullValue()));
        });
  }

  @Test
  void givenCorrectAccessToken_whenAddExistingUser_thenReturn400() {

    String email = UUID.randomUUID().toString();
    String accessToken = generateAdminAccessToken();
    // add a unique user should OK
    createUser(accessToken, email, HttpStatus.OK.value(), Assertions::assertNotNull);

    // add an existing user should return 400
    createUser(accessToken, email, HttpStatus.BAD_REQUEST.value(), Assertions::assertNotNull);
  }

  @Test
  void givenCorrectAccessTokenAndUserId_whenFindOne_thenReturnOk() {

    String accessToken = generateAdminAccessToken();
    // add a unique user should OK
    createUser(
        accessToken,
        UUID.randomUUID().toString(),
        HttpStatus.OK.value(),
        bodyStr -> {
          Assertions.assertNotNull(bodyStr);
          assertThat(bodyStr, hasJsonPath("$.data", notNullValue()));
          assertThat(bodyStr, hasJsonPath("$.data.id", notNullValue()));

          HttpResponse<User> userResponse = JsonToolkit.fromJson(bodyStr, new TypeReference<>() {});

          Map<String, String> headers = new HashMap<>();
          headers.put(
              UserContext.AUTHORIZATION_HEADER,
              UserContext.AUTHORIZATION_HEADER_PREFIX + accessToken);

          testClientHelper.requestAndVerify(
              HttpMethod.GET,
              (uriBuilder -> uriBuilder.path("/users/" + userResponse.getData().getId()).build()),
              headers,
              null,
              HttpStatus.OK.value(),
              bodyStr2 -> {
                Assertions.assertNotNull(bodyStr2);
                assertThat(bodyStr2, hasJsonPath("$.data", notNullValue()));
                assertThat(bodyStr2, hasJsonPath("$.data.id", notNullValue()));
                assertThat(bodyStr2, hasJsonPath("$.data.email", notNullValue()));
                assertThat(bodyStr2, hasJsonPath("$.data.role", notNullValue()));
              });
        });
  }

  @Test
  void givenCorrectAccessTokenAndUserId_whenUpdateState_thenReturnOk() {

    String accessToken = generateAdminAccessToken();
    // add a unique user should OK
    createUser(
        accessToken,
        UUID.randomUUID().toString(),
        HttpStatus.OK.value(),
        bodyStr -> {
          Assertions.assertNotNull(bodyStr);
          assertThat(bodyStr, hasJsonPath("$.data", notNullValue()));
          assertThat(bodyStr, hasJsonPath("$.data.id", notNullValue()));

          HttpResponse<User> userResponse = JsonToolkit.fromJson(bodyStr, new TypeReference<>() {});

          Map<String, String> headers = new HashMap<>();
          headers.put(
              UserContext.AUTHORIZATION_HEADER,
              UserContext.AUTHORIZATION_HEADER_PREFIX + accessToken);

          String disableEndpoint = "/users/" + userResponse.getData().getId() + "/disable";
          testClientHelper.requestAndVerify(
              HttpMethod.PATCH,
              (uriBuilder -> uriBuilder.path(disableEndpoint).build()),
              headers,
              null,
              HttpStatus.OK.value(),
              bodyStr2 -> {
                Assertions.assertNotNull(bodyStr2);
                assertThat(bodyStr2, hasJsonPath("$.data", notNullValue()));
                assertThat(bodyStr2, hasJsonPath("$.data.id", notNullValue()));
                assertThat(bodyStr2, hasJsonPath("$.data.state", is("DISABLED")));
              });

          String enableEndpoint = "/users/" + userResponse.getData().getId() + "/enable";
          testClientHelper.requestAndVerify(
              HttpMethod.PATCH,
              (uriBuilder -> uriBuilder.path(enableEndpoint).build()),
              headers,
              null,
              HttpStatus.OK.value(),
              bodyStr2 -> {
                Assertions.assertNotNull(bodyStr2);
                assertThat(bodyStr2, hasJsonPath("$.data", notNullValue()));
                assertThat(bodyStr2, hasJsonPath("$.data.id", notNullValue()));
                assertThat(bodyStr2, hasJsonPath("$.data.state", is("ENABLED")));
              });
        });
  }

  @Test
  void givenCorrectAccessTokenAndUserId_whenUpdatePassword_thenReturnOk() {

    String accessToken = generateAdminAccessToken();
    // add a unique user should OK
    createUser(
        accessToken,
        UUID.randomUUID().toString(),
        HttpStatus.OK.value(),
        bodyStr -> {
          Assertions.assertNotNull(bodyStr);
          assertThat(bodyStr, hasJsonPath("$.data", notNullValue()));
          assertThat(bodyStr, hasJsonPath("$.data.id", notNullValue()));

          HttpResponse<User> userResponse = JsonToolkit.fromJson(bodyStr, new TypeReference<>() {});

          Map<String, String> headers = new HashMap<>();
          headers.put(
              UserContext.AUTHORIZATION_HEADER,
              UserContext.AUTHORIZATION_HEADER_PREFIX + accessToken);

          ResetPasswordRequest resetPasswordRequest = new ResetPasswordRequest();
          resetPasswordRequest.setPassword(UUID.randomUUID().toString());

          String disableEndpoint = "/users/" + userResponse.getData().getId() + "/resetPassword";
          testClientHelper.requestAndVerify(
              HttpMethod.POST,
              (uriBuilder -> uriBuilder.path(disableEndpoint).build()),
              headers,
              resetPasswordRequest,
              HttpStatus.OK.value(),
              Assertions::assertNotNull);
        });
  }

  @Test
  void givenCorrectAccessTokenAndUserId_whenUpdate_thenReturnOk() {

    String accessToken = generateAdminAccessToken();
    // add a unique user should OK
    Optional<String> userHttpResponseOptional =
        createUser(
            accessToken,
            UUID.randomUUID().toString(),
            HttpStatus.OK.value(),
            bodyStr -> {
              Assertions.assertNotNull(bodyStr);
              assertThat(bodyStr, hasJsonPath("$.data", notNullValue()));
              assertThat(bodyStr, hasJsonPath("$.data.id", notNullValue()));
            });

    Assertions.assertTrue(userHttpResponseOptional.isPresent());
    HttpResponse<User> userResponse =
        JsonToolkit.fromJson(userHttpResponseOptional.get(), new TypeReference<>() {});
    Map<String, String> headers = new HashMap<>();
    headers.put(
        UserContext.AUTHORIZATION_HEADER, UserContext.AUTHORIZATION_HEADER_PREFIX + accessToken);

    UpdateUserRequest updateRequest = new UpdateUserRequest();
    updateRequest.setName("Updated-" + userResponse.getData().getName());
    updateRequest.setRole("Updated-" + userResponse.getData().getRole());
    updateRequest.setEmail("Updated-" + userResponse.getData().getEmail());

    String updateEndpoint = "/users/" + userResponse.getData().getId();
    testClientHelper.requestAndVerify(
        HttpMethod.PATCH,
        (uriBuilder -> uriBuilder.path(updateEndpoint).build()),
        headers,
        updateRequest,
        HttpStatus.OK.value(),
        updatedBodyStr -> {
          Assertions.assertNotNull(updatedBodyStr);
          assertThat(updatedBodyStr, hasJsonPath("$.data", notNullValue()));
          assertThat(updatedBodyStr, hasJsonPath("$.data.id", is(userResponse.getData().getId())));
          assertThat(updatedBodyStr, hasJsonPath("$.data.name", is(updateRequest.getName())));
          assertThat(updatedBodyStr, hasJsonPath("$.data.email", is(updateRequest.getEmail())));
          assertThat(updatedBodyStr, hasJsonPath("$.data.role", is(updateRequest.getRole())));
          assertThat(updatedBodyStr, hasJsonPath("$.data.updatedBy", notNullValue()));
          assertThat(updatedBodyStr, hasJsonPath("$.data.updatedAt", notNullValue()));
        });
  }

  @Test
  void givenCorrectAccessTokenAndUserId_whenUpdateEmail2ExistOne_thenReturn400() {

    String accessToken = generateAdminAccessToken();
    // add a unique user should OK
    Optional<String> userHttpResponseOptional =
        createUser(
            accessToken,
            UUID.randomUUID().toString(),
            HttpStatus.OK.value(),
            bodyStr -> {
              Assertions.assertNotNull(bodyStr);
              assertThat(bodyStr, hasJsonPath("$.data", notNullValue()));
              assertThat(bodyStr, hasJsonPath("$.data.id", notNullValue()));
            });

    Optional<String> user2HttpResponseOptional =
        createUser(
            accessToken,
            UUID.randomUUID().toString(),
            HttpStatus.OK.value(),
            bodyStr -> {
              Assertions.assertNotNull(bodyStr);
              assertThat(bodyStr, hasJsonPath("$.data", notNullValue()));
              assertThat(bodyStr, hasJsonPath("$.data.id", notNullValue()));
            });

    Assertions.assertTrue(userHttpResponseOptional.isPresent());
    Assertions.assertTrue(user2HttpResponseOptional.isPresent());
    HttpResponse<User> user1 =
        JsonToolkit.fromJson(userHttpResponseOptional.get(), new TypeReference<>() {});
    HttpResponse<User> user2 =
        JsonToolkit.fromJson(user2HttpResponseOptional.get(), new TypeReference<>() {});
    Map<String, String> headers = new HashMap<>();
    headers.put(
        UserContext.AUTHORIZATION_HEADER, UserContext.AUTHORIZATION_HEADER_PREFIX + accessToken);

    UpdateUserRequest updateRequest = new UpdateUserRequest();
    updateRequest.setEmail(user2.getData().getEmail());

    String updateEndpoint = "/users/" + user1.getData().getId();
    testClientHelper.requestAndVerify(
        HttpMethod.PATCH,
        (uriBuilder -> uriBuilder.path(updateEndpoint).build()),
        headers,
        updateRequest,
        HttpStatus.BAD_REQUEST.value(),
        Assertions::assertNotNull);
  }

  @Test
  void givenCorrectAccessTokenAndNotExistUserId_whenFindOne_thenReturn404() {

    String accessToken = generateAdminAccessToken();
    Map<String, String> headers = new HashMap<>();
    headers.put(
        UserContext.AUTHORIZATION_HEADER, UserContext.AUTHORIZATION_HEADER_PREFIX + accessToken);

    testClientHelper.requestAndVerify(
        HttpMethod.GET,
        (uriBuilder -> uriBuilder.path("/users/" + UUID.randomUUID().toString()).build()),
        headers,
        null,
        HttpStatus.NOT_FOUND.value(),
        Assertions::assertNotNull);
  }

  @Test
  void givenInCorrectAccessToken_whenAddNewUser_thenReturn401() {

    Map<String, String> headers = new HashMap<>();
    headers.put(
        UserContext.AUTHORIZATION_HEADER,
        UserContext.AUTHORIZATION_HEADER_PREFIX + UUID.randomUUID().toString());

    // generate a unique email
    CreateUserRequest createUserRequest = new CreateUserRequest();
    createUserRequest.setEmail(UUID.randomUUID().toString());
    createUserRequest.setPassword(UUID.randomUUID().toString());
    createUserRequest.setRole("USER");
    createUserRequest.setName(UUID.randomUUID().toString());

    testClientHelper.requestAndVerify(
        HttpMethod.POST,
        (uriBuilder -> uriBuilder.path("/users").build()),
        headers,
        createUserRequest,
        HttpStatus.UNAUTHORIZED.value(),
        Assertions::assertNull);
  }

  @Test
  void givenUserCreated_whenSearch_thenReturnOk() {
    String accessToken = generateAdminAccessToken();
    createUser(accessToken, "abc@test.com", HttpStatus.OK.value(), Assertions::assertNotNull);
    createUser(accessToken, "123@test.com", HttpStatus.OK.value(), Assertions::assertNotNull);
    createUser(accessToken, "iam3@test.com", HttpStatus.OK.value(), Assertions::assertNotNull);

    Map<String, String> headers = new HashMap<>();
    headers.put(
        UserContext.AUTHORIZATION_HEADER, UserContext.AUTHORIZATION_HEADER_PREFIX + accessToken);
    // list all
    testClientHelper.requestAndVerify(
        HttpMethod.GET,
        (uriBuilder -> uriBuilder.path("/users").build()),
        headers,
        null,
        HttpStatus.OK.value(),
        bodyStr -> {
          Assertions.assertNotNull(bodyStr);
          assertThat(bodyStr, hasJsonPath("$.data.data", notNullValue()));
          assertThat(bodyStr, hasJsonPath("$.data.data", hasSize(greaterThanOrEqualTo(3))));
        });

    // search by email or name
    testClientHelper.requestAndVerify(
        HttpMethod.GET,
        (uriBuilder -> uriBuilder.path("/users").queryParam("q", "abc").build()),
        headers,
        null,
        HttpStatus.OK.value(),
        bodyStr -> {
          Assertions.assertNotNull(bodyStr);
          assertThat(bodyStr, hasJsonPath("$.data.data", notNullValue()));
          assertThat(bodyStr, hasJsonPath("$.data.data", hasSize(greaterThanOrEqualTo(1))));
          assertThat(bodyStr, hasJsonPath("$.data.data[0].email", equalTo("abc@test.com")));
        });
  }

  @Test
  void givenUserRole_whenAccessUserEndpoints_thenReturn403() {

    String accessToken = generateUserAccessToken();
    System.out.println(accessToken);
    Map<String, String> headers = new HashMap<>();
    headers.put(
        UserContext.AUTHORIZATION_HEADER, UserContext.AUTHORIZATION_HEADER_PREFIX + accessToken);

    createUser(
        accessToken,
        UUID.randomUUID().toString(),
        HttpStatus.FORBIDDEN.value(),
        Assertions::assertNull);

    // retrieve user
    testClientHelper.requestAndVerify(
        HttpMethod.GET,
        (uriBuilder -> uriBuilder.path("/users/" + UUID.randomUUID().toString()).build()),
        headers,
        null,
        HttpStatus.FORBIDDEN.value(),
        Assertions::assertNull);

    // enable a user
    testClientHelper.requestAndVerify(
        HttpMethod.PATCH,
        (uriBuilder ->
            uriBuilder.path("/users/" + UUID.randomUUID().toString() + "/enable").build()),
        headers,
        null,
        HttpStatus.FORBIDDEN.value(),
        Assertions::assertNull);
    // disable a user
    testClientHelper.requestAndVerify(
        HttpMethod.PATCH,
        (uriBuilder ->
            uriBuilder.path("/users/" + UUID.randomUUID().toString() + "/disable").build()),
        headers,
        null,
        HttpStatus.FORBIDDEN.value(),
        Assertions::assertNull);

    // reset password
    ResetPasswordRequest resetPasswordRequest = new ResetPasswordRequest();
    resetPasswordRequest.setPassword(UUID.randomUUID().toString());
    testClientHelper.requestAndVerify(
        HttpMethod.POST,
        (uriBuilder ->
            uriBuilder.path("/users/" + UUID.randomUUID().toString() + "/resetPassword").build()),
        headers,
        resetPasswordRequest,
        HttpStatus.FORBIDDEN.value(),
        Assertions::assertNull);
  }

  @Test
  void givenCorrectAccessTokenAndUserId_whenFindOne_thenReturnHasTokenData() {

    AuthRequest loginRequest = new AuthRequest();
    loginRequest.setEmail(TestContextConstants.ADMIN_LOGIN_USERNAME);
    loginRequest.setPassword(TestContextConstants.LOGIN_PASSWORD);
    AuthResponse authResponse = userService.login(loginRequest);

    Map<String, String> headers = new HashMap<>();
    headers.put(
        UserContext.AUTHORIZATION_HEADER,
        UserContext.AUTHORIZATION_HEADER_PREFIX + authResponse.getAccessToken());

    String endpoint = "/users/" + authResponse.getId();

    testClientHelper.requestAndVerify(
        HttpMethod.GET,
        (uriBuilder -> uriBuilder.path(endpoint).build()),
        headers,
        null,
        HttpStatus.OK.value(),
        bodyStr -> {
          Assertions.assertNotNull(bodyStr);
          assertThat(bodyStr, hasJsonPath("$.data", notNullValue()));
          assertThat(bodyStr, hasJsonPath("$.data.tokens", hasSize(greaterThanOrEqualTo(1))));
          assertThat(bodyStr, hasJsonPath("$.data.tokens[0].id", notNullValue()));
          assertThat(bodyStr, hasJsonPath("$.data.tokens[0].createdAt", notNullValue()));
          assertThat(bodyStr, hasJsonPath("$.data.tokens[0].expiresAt", notNullValue()));
          assertThat(bodyStr, hasJsonPath("$.data.tokens[0].userId", is(authResponse.getId())));
          assertThat(bodyStr, hasJsonPath("$.data.tokens[0].createdBy", is(authResponse.getId())));
          assertThat(bodyStr, hasJsonPath("$.data.tokens[0].revoked", is(false)));
          assertThat(bodyStr, hasJsonPath("$.data.tokens[0].metadata", notNullValue()));
          assertThat(bodyStr, hasJsonPath("$.data.tokens[0].claims", notNullValue()));
          assertThat(bodyStr, hasNoJsonPath("$.data.tokens[0].revokedAt"));
          assertThat(bodyStr, hasNoJsonPath("$.data.tokens[0].revokedBy"));
          assertThat(bodyStr, hasNoJsonPath("$.data.tokens[0].token"));
        });
  }

  @Test
  void givenRefreshTokenRevoked_whenRenew_thenReturn400() {

    AuthRequest loginRequest = new AuthRequest();
    loginRequest.setEmail(TestContextConstants.ADMIN_LOGIN_USERNAME);
    loginRequest.setPassword(TestContextConstants.LOGIN_PASSWORD);
    AuthResponse authResponse = userService.login(loginRequest);

    Map<String, String> headers = new HashMap<>();
    headers.put(
        UserContext.AUTHORIZATION_HEADER,
        UserContext.AUTHORIZATION_HEADER_PREFIX + authResponse.getAccessToken());

    // revoke the refresh token
    String revokeEndpoint = "/users/" + authResponse.getId() + "/revokeTokens";
    testClientHelper.requestAndVerify(
        HttpMethod.POST,
        (uriBuilder -> uriBuilder.path(revokeEndpoint).build()),
        headers,
        null,
        HttpStatus.OK.value(),
        Assertions::assertNotNull);

    // renew the refresh token should fail
    String renewEndpoint = "/auth/token";
    AuthRequest authRequest = new AuthRequest();
    authRequest.setRefreshToken(authResponse.getRefreshToken());
    authRequest.setGrantType(AuthGrantTypeEnum.REFRESH_TOKEN);
    testClientHelper.requestAndVerify(
        HttpMethod.POST,
        (uriBuilder -> uriBuilder.path(renewEndpoint).build()),
        null,
        authRequest,
        HttpStatus.BAD_REQUEST.value(),
        Assertions::assertNotNull);
  }
}
