package com.consoleconnect.kraken.operator.auth.jwt;

import com.consoleconnect.kraken.operator.auth.model.AuthDataProperty;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import lombok.SneakyThrows;
import org.apache.commons.codec.binary.Base64;

public class JWKSetGenerator {

  private JWKSetGenerator() {}

  @SneakyThrows
  public static JWKSet generate(AuthDataProperty.Jwks jwks) {
    return new JWKSet(
        new RSAKey.Builder((RSAPublicKey) generatePublicKey(jwks.getPublicKey()))
            .keyUse(KeyUse.SIGNATURE)
            .algorithm(JWSAlgorithm.RS256)
            .keyID(jwks.getKeyId())
            .build());
  }

  @SneakyThrows
  private static PublicKey generatePublicKey(String secret) throws InvalidKeySpecException {
    byte[] keyBytes = Base64.decodeBase64(secret);
    X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(keyBytes);
    return KeyFactory.getInstance("RSA").generatePublic(x509EncodedKeySpec);
  }
}
