package com.consoleconnect.kraken.operator.auth.jwt;

import com.consoleconnect.kraken.operator.auth.model.AuthDataProperty;
import io.jsonwebtoken.Jwts;
import java.security.Key;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;

@Slf4j
@Getter
public abstract class JwtEncoderToolkit {

  public static final String ALG_RS256 = "RS256";
  public static final String ALG_HS512 = "HS512";

  protected long tokenExpiredInSeconds = 3600;

  protected final AuthDataProperty.JwtEncoderProperty jwtConfig;

  public static JwtEncoderToolkit get(AuthDataProperty.JwtEncoderProperty config) {
    if (config.getPrivateKey() != null) {
      return new PairKeyEncodeProvider(config);
    } else if (config.getSecret() != null && !config.getSecret().trim().isEmpty()) {
      return new SecretEncodeProvider(config);
    }
    throw new IllegalArgumentException("Unsupported Encoder Config");
  }

  protected JwtEncoderToolkit(AuthDataProperty.JwtEncoderProperty jwtConfig) {
    this.jwtConfig = jwtConfig;
  }

  public final String generateToken(String userId) {
    return generateToken(userId, null);
  }

  public final String generateToken(String userId, Map<String, Object> claims) {
    return generateToken(userId, claims, tokenExpiredInSeconds);
  }

  public String generateToken(String userId, Map<String, Object> claims, long expiredInSeconds) {
    log.info(
        "Generating token for user: userId:{},claims:{},expiredInSeconds:{}",
        userId,
        claims,
        expiredInSeconds);
    if (claims == null) {
      claims = new HashMap<>();
    }

    long current = System.currentTimeMillis();
    return Jwts.builder()
        .header()
        .add("kid", jwtConfig.getKeyId())
        .and()
        .subject(userId)
        .claims(claims)
        .issuedAt(new Date(current))
        .expiration(new Date(current + expiredInSeconds * 1000))
        .issuer(jwtConfig.getIssuer())
        .signWith(getSignInKey())
        .compact();
  }

  public static String hashToken(String token) {
    return DigestUtils.sha256Hex(token);
  }

  public abstract Key getSignInKey();

  public abstract String getAlgorithm();

  public static class PairKeyEncodeProvider extends JwtEncoderToolkit {

    private final PrivateKey privateKey;
    public final KeyFactory keyFactory;

    @SneakyThrows
    public PairKeyEncodeProvider(AuthDataProperty.JwtEncoderProperty jwt) {
      super(jwt);

      this.tokenExpiredInSeconds = jwt.getTokenExpiresInSeconds();
      this.keyFactory = KeyFactory.getInstance("RSA");
      this.privateKey = generatePrivateKey(jwt.getPrivateKey());
    }

    private PrivateKey generatePrivateKey(String secret) throws InvalidKeySpecException {
      byte[] keyBytes = Base64.decodeBase64(secret);
      PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(keyBytes);
      return keyFactory.generatePrivate(pkcs8EncodedKeySpec);
    }

    @Override
    public Key getSignInKey() {
      return privateKey;
    }

    @Override
    public String getAlgorithm() {
      return ALG_RS256;
    }
  }

  public static class SecretEncodeProvider extends JwtEncoderToolkit {

    private final SecretKey secretKey;

    @SneakyThrows
    public SecretEncodeProvider(AuthDataProperty.JwtEncoderProperty jwt) {
      super(jwt);
      this.secretKey = this.generateSignInKey(jwt.getSecret());
      this.tokenExpiredInSeconds = jwt.getTokenExpiresInSeconds();
    }

    private SecretKey generateSignInKey(String secret) {
      return new SecretKeySpec(secret.getBytes(), "HMACSHA256");
    }

    public Key getSignInKey() {
      return secretKey;
    }

    @Override
    public String getAlgorithm() {
      return ALG_HS512;
    }
  }
}
