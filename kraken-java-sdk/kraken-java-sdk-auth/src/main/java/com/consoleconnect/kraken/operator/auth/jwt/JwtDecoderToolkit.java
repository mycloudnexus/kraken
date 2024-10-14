package com.consoleconnect.kraken.operator.auth.jwt;

import com.consoleconnect.kraken.operator.auth.model.AuthDataProperty;
import com.consoleconnect.kraken.operator.auth.security.JwtTokenVerifier;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import javax.crypto.spec.SecretKeySpec;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class JwtDecoderToolkit {

  public static final String ALGORITHM_HMAC = "HMACSHA256";

  private JwtDecoderToolkit() {}

  @SneakyThrows
  public static NimbusJwtDecoder createJwtDecoderInstance(
      AuthDataProperty.JwtDecoderProperty decodeConfig) {
    log.info(
        "createJwtDecoderInstance,issuer:{},jwks:{},publicKey:{}, secretKey:{}",
        decodeConfig.getIssuer(),
        decodeConfig.getJwksUri(),
        decodeConfig.getPublicKey() != null,
        decodeConfig.getSecret() != null);
    NimbusJwtDecoder nimbusJwtDecoder = null;
    if (decodeConfig.getJwksUri() != null && !decodeConfig.getJwksUri().trim().isEmpty()) {
      log.info("jwksUri: {}", decodeConfig.getJwksUri());
      nimbusJwtDecoder = NimbusJwtDecoder.withJwkSetUri(decodeConfig.getJwksUri()).build();
    } else if (decodeConfig.getPublicKey() != null
        && !decodeConfig.getPublicKey().trim().isEmpty()) {
      log.info("publicKey: <>");
      nimbusJwtDecoder =
          NimbusJwtDecoder.withPublicKey(
                  (RSAPublicKey)
                      KeyFactory.getInstance("RSA")
                          .generatePublic(
                              new X509EncodedKeySpec(
                                  Base64.decodeBase64(decodeConfig.getPublicKey()))))
              .build();
    } else if (decodeConfig.getSecret() != null && !decodeConfig.getSecret().trim().isEmpty()) {
      log.info("secretKey,<>");
      nimbusJwtDecoder =
          NimbusJwtDecoder.withSecretKey(
                  new SecretKeySpec(decodeConfig.getSecret().getBytes(), ALGORITHM_HMAC))
              .build();
    }

    if (nimbusJwtDecoder != null
        && decodeConfig.getVerifier() != null
        && !decodeConfig.getVerifier().isEmpty()) {
      nimbusJwtDecoder.setJwtValidator(
          new DelegatingOAuth2TokenValidator<>(
              JwtValidators.createDefault(), new JwtTokenVerifier(decodeConfig.getVerifier())));
    }
    log.info("jwtDecoderInstance created,issuer:{}", decodeConfig.getIssuer());
    return nimbusJwtDecoder;
  }
}
