package com.consoleconnect.kraken.operator.service;

import static org.mockito.Mockito.when;

import com.consoleconnect.kraken.operator.auth.jwt.JwtDecoderToolkit;
import com.consoleconnect.kraken.operator.auth.model.AuthDataProperty;
import com.consoleconnect.kraken.operator.config.TestApplication;
import com.consoleconnect.kraken.operator.config.TestContextConstants;
import com.consoleconnect.kraken.operator.controller.dto.CreateAPITokenRequest;
import com.consoleconnect.kraken.operator.controller.model.APIToken;
import com.consoleconnect.kraken.operator.controller.service.APITokenService;
import com.consoleconnect.kraken.operator.core.exception.KrakenException;
import com.consoleconnect.kraken.operator.test.AbstractIntegrationTest;
import com.consoleconnect.kraken.operator.test.MockIntegrationTest;
import com.nimbusds.jose.util.Base64;
import java.util.Objects;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.context.ContextConfiguration;

@MockIntegrationTest
@ContextConfiguration(classes = {TestApplication.class})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class APITokenServiceTest extends AbstractIntegrationTest {

  @MockBean AuthDataProperty.AuthServer authServer;
  @Autowired private APITokenService tokenService;

  @Test
  void givenAuthDisabled_thenCreateToken_shouldThrowBadRequest() {
    // given
    when(authServer.isEnabled()).thenReturn(false);
    // when
    CreateAPITokenRequest request = new CreateAPITokenRequest();
    request.setEnvId(TestApplication.envId);
    request.setName("test");

    KrakenException krakenException =
        Assertions.assertThrows(
            KrakenException.class,
            () -> tokenService.createToken(TestContextConstants.PRODUCT_ID, request, null));
    // then
    Assertions.assertEquals(400, krakenException.getCode());
    Assertions.assertEquals(
        "Auth server is disabled, enable auth server to support api token",
        krakenException.getMessage());
  }

  private APIToken createAccessToken() {
    // given
    when(authServer.isEnabled()).thenReturn(true);

    AuthDataProperty.JwtEncoderProperty jwtEncoderProperty =
        new AuthDataProperty.JwtEncoderProperty();
    jwtEncoderProperty.setIssuer("test-issuer");
    jwtEncoderProperty.setSecret(Base64.encode(UUID.randomUUID().toString()).toString());
    jwtEncoderProperty.setKeyId("test-keyId");

    when(authServer.getJwt()).thenReturn(jwtEncoderProperty);
    // when
    CreateAPITokenRequest request = new CreateAPITokenRequest();
    request.setEnvId(TestApplication.envId);
    request.setName("test");
    request.setTokenExpiresInSeconds(1000L);

    return tokenService.createToken(TestContextConstants.PRODUCT_ID, request, null);
  }

  @Test
  void givenTokenExpiresInSeconds_thenCreateToken_shouldReturnExpectedToken() {
    // given
    when(authServer.isEnabled()).thenReturn(true);

    AuthDataProperty.JwtEncoderProperty jwtEncoderProperty =
        new AuthDataProperty.JwtEncoderProperty();
    jwtEncoderProperty.setIssuer("test-issuer");
    jwtEncoderProperty.setSecret(Base64.encode(UUID.randomUUID().toString()).toString());
    jwtEncoderProperty.setKeyId("test-keyId");

    when(authServer.getJwt()).thenReturn(jwtEncoderProperty);
    // when
    CreateAPITokenRequest request = new CreateAPITokenRequest();
    request.setEnvId(TestApplication.envId);
    request.setName("test");
    request.setTokenExpiresInSeconds(1000L);

    APIToken createdToken =
        tokenService.createToken(TestContextConstants.PRODUCT_ID, request, null);

    // then
    Assertions.assertNotNull(createdToken);
    Assertions.assertEquals(TestApplication.envId, createdToken.getEnvId());
    Assertions.assertEquals(TestContextConstants.PRODUCT_ID, createdToken.getProductId());
    Assertions.assertNotNull(createdToken.getToken());

    AuthDataProperty.JwtDecoderProperty decoderProperty = new AuthDataProperty.JwtDecoderProperty();
    decoderProperty.setIssuer(jwtEncoderProperty.getIssuer());
    decoderProperty.setSecret(jwtEncoderProperty.getSecret());

    System.out.println("createdToken.getToken() = " + createdToken.getToken());
    Jwt jwt =
        JwtDecoderToolkit.createJwtDecoderInstance(decoderProperty).decode(createdToken.getToken());
    Assertions.assertNotNull(jwt);

    Assertions.assertEquals(
        1000L,
        Objects.requireNonNull(jwt.getExpiresAt()).getEpochSecond()
            - Objects.requireNonNull(jwt.getIssuedAt()).getEpochSecond());
  }

  @Test
  void givenWrongTokenId_thenRevoke_shouldThrowNotFound() {
    String tokenId = UUID.randomUUID().toString();
    KrakenException krakenException =
        Assertions.assertThrows(
            KrakenException.class, () -> tokenService.revokeToken(tokenId, null));
    Assertions.assertEquals(404, krakenException.getCode());
    Assertions.assertEquals("Token not found", krakenException.getMessage());
  }

  @Test
  void givenCorrectTokenId_thenRevoke_shouldReturnOk() {
    APIToken apiToken = createAccessToken();
    APIToken revokedToken = tokenService.revokeToken(apiToken.getId(), null);
    Assertions.assertTrue(revokedToken.isRevoked());
  }

  @Test
  void givenRevokedTokenId_thenRevoke_shouldThrowBadRequest() {
    APIToken apiToken = createAccessToken();
    String tokenId = apiToken.getId();
    APIToken revokedToken = tokenService.revokeToken(tokenId, null);
    Assertions.assertTrue(revokedToken.isRevoked());
    KrakenException krakenException =
        Assertions.assertThrows(
            KrakenException.class, () -> tokenService.revokeToken(tokenId, null));
    Assertions.assertEquals(400, krakenException.getCode());
    Assertions.assertEquals("Token is expired", krakenException.getMessage());
  }

  @Test
  void givenAccessToken_thenFindEnvId_shouldReturnExpectedEnvId() {
    APIToken apiToken = createAccessToken();
    Jwt jwt =
        Jwt.withTokenValue(apiToken.getToken())
            .claim(APITokenService.ENV_ID, apiToken.getEnvId())
            .claim(APITokenService.PRODUCT_ID, apiToken.getProductId())
            .subject(apiToken.getUserId())
            .header("alg", "HS256")
            .issuer("http://mock-issuer")
            .build();
    JwtAuthenticationToken authenticationToken = new JwtAuthenticationToken(jwt);
    String envId = tokenService.findEnvId(authenticationToken, null);
    Assertions.assertEquals(TestApplication.envId, envId);
  }

  @Test
  void givenEnvIdOrName_thenFindEnvId_shouldReturnExpectedEnvId() {
    // find by envId
    String envId = tokenService.findEnvId(null, TestApplication.envId);
    Assertions.assertEquals(TestApplication.envId, envId);

    // find by envName
    envId = tokenService.findEnvId(null, TestApplication.ENV_NAME);
    Assertions.assertEquals(TestApplication.envId, envId);
  }
}
