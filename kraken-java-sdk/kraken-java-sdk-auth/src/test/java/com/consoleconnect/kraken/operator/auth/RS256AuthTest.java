package com.consoleconnect.kraken.operator.auth;

import com.consoleconnect.kraken.operator.TestApplication;
import com.consoleconnect.kraken.operator.TestContextConstants;
import com.consoleconnect.kraken.operator.auth.dto.AuthRequest;
import com.consoleconnect.kraken.operator.auth.dto.AuthResponse;
import com.consoleconnect.kraken.operator.auth.helper.WebTestClientHelper;
import com.consoleconnect.kraken.operator.auth.model.AuthDataProperty;
import com.consoleconnect.kraken.operator.auth.security.UserContext;
import com.consoleconnect.kraken.operator.auth.service.JwtDecoderService;
import com.consoleconnect.kraken.operator.core.model.HttpResponse;
import com.consoleconnect.kraken.operator.core.toolkit.JsonToolkit;
import com.consoleconnect.kraken.operator.test.AbstractIntegrationTest;
import com.consoleconnect.kraken.operator.test.MockIntegrationTest;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.Map;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;

@ActiveProfiles("test-rs256")
@MockIntegrationTest
@ContextConfiguration(classes = {TestApplication.class})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Slf4j
class RS256AuthTest extends AbstractIntegrationTest {

  private final WebTestClientHelper testClientHelper;
  private final JwtDecoderService decoderService;

  private final AuthDataProperty.ResourceServer resourceServer;

  @Autowired
  public RS256AuthTest(
      WebTestClient webTestClient,
      JwtDecoderService decoderService,
      AuthDataProperty.ResourceServer resourceServer) {
    this.testClientHelper = new WebTestClientHelper(webTestClient);
    this.decoderService = decoderService;
    this.resourceServer = resourceServer;
  }

  @Order(1)
  @Test
  void givenNoLogin_whenAccess_thenUnauthorized() {
    testClientHelper.requestAndVerify(
        HttpMethod.GET,
        (uriBuilder -> uriBuilder.path("/assets").build()),
        HttpStatus.UNAUTHORIZED.value(),
        Assertions::assertNull);
  }

  @Order(2)
  @Test
  void givenWrongPassword_whenLogin_thenUnauthorized() {
    AuthRequest jwtRequest = new AuthRequest();
    jwtRequest.setEmail("admin");
    jwtRequest.setPassword(UUID.randomUUID().toString());
    testClientHelper.requestAndVerify(
        HttpMethod.POST,
        (uriBuilder -> uriBuilder.path("/login").build()),
        null,
        jwtRequest,
        HttpStatus.UNAUTHORIZED.value(),
        Assertions::assertNotNull);
  }

  @Order(1)
  @Test
  void givenCorrectPassword_whenLogin_thenAccessTokenGenerated() {
    AuthRequest jwtRequest = new AuthRequest();
    jwtRequest.setEmail(TestContextConstants.LOGIN_USERNAME);
    jwtRequest.setPassword(TestContextConstants.LOGIN_PASSWORD);

    testClientHelper.requestAndVerify(
        HttpMethod.POST,
        (uriBuilder -> uriBuilder.path("/login").build()),
        null,
        jwtRequest,
        HttpStatus.OK.value(),
        bodyStr -> {
          HttpResponse<AuthResponse> jwtTokenResponseHttpResponse =
              JsonToolkit.fromJson(bodyStr, new TypeReference<HttpResponse<AuthResponse>>() {});
          Assertions.assertNotNull(jwtTokenResponseHttpResponse.getData().getAccessToken());

          Jwt decodedJwt =
              this.decoderService
                  .getDecoder(resourceServer.getJwt().get(0))
                  .decode(jwtTokenResponseHttpResponse.getData().getAccessToken());

          Assertions.assertEquals(
              jwtTokenResponseHttpResponse.getData().getId(), decodedJwt.getSubject());
          Assertions.assertTrue(decodedJwt.getClaimAsStringList("roles").contains("USER"));

          testClientHelper.requestAndVerify(
              HttpMethod.GET,
              (uriBuilder -> uriBuilder.path("/assets").build()),
              Map.of(
                  UserContext.AUTHORIZATION_HEADER,
                  UserContext.AUTHORIZATION_HEADER_PREFIX
                      + jwtTokenResponseHttpResponse.getData().getAccessToken()),
              null,
              HttpStatus.OK.value(),
              Assertions::assertNotNull);
        });
  }

  @Test
  @Order(1)
  void givenRS256Auth_whenAccessJwks_thenResponseOk() {
    testClientHelper.requestAndVerify(
        HttpMethod.GET,
        (uriBuilder -> uriBuilder.path("/.well-known/jwks.json").build()),
        HttpStatus.OK.value(),
        bodyStr -> {
          MatcherAssert.assertThat(bodyStr, Matchers.notNullValue());
        });
  }
}
