package com.consoleconnect.kraken.operator.auth;

import com.consoleconnect.kraken.operator.auth.security.JwtTokenVerifier;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

class JwtTokenVerifierTest {

  private static final String RS512_SIGNED_JWT =
      "eyJhbGciOiJSUzUxMiJ9.eyJzdWIiOiJ0ZXN0LXN1YmplY3QiLCJleHAiOjE5NzQzMjYxMTl9.LKAx-60EBfD7jC1jb1eKcjO4uLvf3ssISV-8tN-qp7gAjSvKvj4YA9-V2mIb6jcS1X_xGmNy6EIimZXpWaBR3nJmeu-jpe85u4WaW2Ztr8ecAi-dTO7ZozwdtljKuBKKvj4u1nF70zyCNl15AozSG0W1ASrjUuWrJtfyDG6WoZ8VfNMuhtU-xUYUFvscmeZKUYQcJ1KS-oV5tHeF8aNiwQoiPC_9KXCOZtNEJFdq6-uzFdHxvOP2yex5Gbmg5hXonauIFXG2ZPPGdXzm-5xkhBpgM8U7A_6wb3So8wBvLYYm2245QUump63AJRAy8tQpwt4n9MvQxQgS3z9R-NK92A";

  private static final String VERIFY_KEY =
      "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAq4yKxb6SNePdDmQi9xFCrP6QvHosErQzryknQTTTffs0t3cy3Er3lIceuhZ7yQNSCDfPFqG8GoyoKhuChRiA5D+J2ab7bqTa1QJKfnCyERoscftgN2fXPHjHoiKbpGV2tMVw8mXl//tePOAiKbMJaBUnlAvJgkk1rVm08dSwpLC1sr2M19euf9jwnRGkMRZuhp9iCPgECRke5T8Ixpv0uQjSmGHnWUKTFlbj8sM83suROR1Ue64JSGScANc5vk3huJ/J97qTC+K2oKj6L8d9O8dpc4obijEOJwpydNvTYDgbiivYeSB00KS9jlBkQ5B2QqLvLVEygDl3dp59nGx6YQIDAQAB";

  private static KeyFactory kf;

  @BeforeAll
  static void keyFactory() throws NoSuchAlgorithmException {
    kf = KeyFactory.getInstance("RSA");
  }

  @Test
  void givenEmptyClaims_whenValidate_thenReturnSuccess() {
    JwtTokenVerifier verifier = new JwtTokenVerifier(new HashMap<>());
    Jwt token = Mockito.mock(Jwt.class);
    OAuth2TokenValidatorResult result = verifier.validate(token);
    Assertions.assertTrue(result.getErrors().isEmpty());
  }

  @SneakyThrows
  @Test
  void givenNonEmptyClaims_whenValidate_thenReturnErrors() {
    NimbusJwtDecoder decoder =
        NimbusJwtDecoder.withPublicKey(key()).signatureAlgorithm(SignatureAlgorithm.RS512).build();
    Jwt token = decoder.decode(RS512_SIGNED_JWT);

    Map<String, Object> claims = new HashMap<>();
    claims.put("subject", "test-subject");
    JwtTokenVerifier verifier = new JwtTokenVerifier(claims);
    OAuth2TokenValidatorResult result = verifier.validate(token);
    Assertions.assertFalse(result.getErrors().isEmpty());
  }

  @SneakyThrows
  @Test
  void givenNonEmptyClaims_whenValidate_thenReturnSuccess() {
    NimbusJwtDecoder decoder =
        NimbusJwtDecoder.withPublicKey(key()).signatureAlgorithm(SignatureAlgorithm.RS512).build();
    Jwt token = decoder.decode(RS512_SIGNED_JWT);

    Map<String, Object> claims = new HashMap<>();
    claims.put("sub", "test-subject");
    JwtTokenVerifier verifier = new JwtTokenVerifier(claims);
    OAuth2TokenValidatorResult result = verifier.validate(token);
    Assertions.assertTrue(result.getErrors().isEmpty());
  }

  private RSAPublicKey key() throws InvalidKeySpecException {
    byte[] decoded = Base64.getDecoder().decode(VERIFY_KEY.getBytes());
    EncodedKeySpec spec = new X509EncodedKeySpec(decoded);
    return (RSAPublicKey) kf.generatePublic(spec);
  }
}
