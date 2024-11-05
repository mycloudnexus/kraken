package com.consoleconnect.kraken.operator.auth.jwt;

import com.consoleconnect.kraken.operator.auth.dto.JwtTokenDto;
import com.consoleconnect.kraken.operator.auth.model.AuthDataProperty;
import com.consoleconnect.kraken.operator.auth.security.JwtTokenVerifier;
import com.consoleconnect.kraken.operator.core.toolkit.JsonToolkit;
import com.fasterxml.jackson.core.type.TypeReference;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Optional;
import javax.crypto.spec.SecretKeySpec;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
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

  public static Optional<JwtTokenDto> decodeJWTToken(String tokenStr) {
    if (StringUtils.isBlank(tokenStr)) {
      return Optional.empty();
    }
    String token = tokenStr.replaceAll("^.*\\s+", "");
    String[] chunks = token.split("\\.");
    if (chunks.length < 2) {
      return Optional.empty();
    }
    java.util.Base64.Decoder decoder = java.util.Base64.getUrlDecoder();
    try {
      String headerStr = new String(decoder.decode(chunks[0]));
      String payloadStr = new String(decoder.decode(chunks[1]));

      JwtTokenDto.Header header =
          JsonToolkit.fromJson(headerStr, new TypeReference<JwtTokenDto.Header>() {});
      JwtTokenDto.Payload payload =
          JsonToolkit.fromJson(payloadStr, new TypeReference<JwtTokenDto.Payload>() {});
      JwtTokenDto jwtTokenDto = new JwtTokenDto();
      jwtTokenDto.setHeader(header);
      jwtTokenDto.setPayload(payload);
      return Optional.of(jwtTokenDto);
    } catch (Exception e) {
      log.error("Failed to decode token", e);
    }
    return Optional.empty();
  }
}
