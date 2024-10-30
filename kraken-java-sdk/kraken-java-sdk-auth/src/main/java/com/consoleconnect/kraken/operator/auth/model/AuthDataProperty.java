package com.consoleconnect.kraken.operator.auth.model;

import com.consoleconnect.kraken.operator.auth.entity.UserEntity;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpMethod;

@Data
public class AuthDataProperty {

  private AuthServer authServer = new AuthServer();
  private ResourceServer resourceServer = new ResourceServer();

  private Login login = new Login();

  @NoArgsConstructor
  @AllArgsConstructor
  @Data
  public static class PathPermission {
    private List<HttpMethod> httpMethods;
    private String path;
    private List<String> roles;
  }

  @Data
  public static class AuthServer {
    private boolean enabled = false;
    private JwtEncoderProperty jwt = new JwtEncoderProperty();
    private Jwks jwks = new Jwks();
  }

  @Data
  public static class Jwks {
    private boolean enabled;
    private String publicKey;
    private String keyId;
  }

  @Data
  public static class ResourceServer {
    private boolean enabled = false;
    private List<JwtDecoderProperty> jwt;

    private List<String> corsAllowedHeaders = List.of("*");
    private List<String> corsAllowedOrigins = List.of("*");
    private List<String> corsAllowedMethods = List.of("*");

    private List<String> defaultAllowedPaths =
        List.of(
            "/images/**",
            "/js/**",
            "/css/**",
            "/webjars/**",
            "/actuator/health",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/",
            "/favicon.ico",
            "/.well-known/**",
            "/login",
            "/auth/token");

    private List<String> allowedPaths = List.of();
    private List<PathPermission> pathPermissions = List.of();

    private String bearerTokenHeaderName = "Authorization";
    private String userId = "x-kraken-userId";
    private SecurityFilter securityFilter = new SecurityFilter();
  }

  @Data
  public static class Login {
    private boolean enabled = false;
    private String hmacSecret;
    private long tokenExpiresInSeconds = 3600; // 1 hour
    private List<UserEntity> userList = List.of();
    private JwtEncoderProperty jwt;

    private RefreshToken refreshToken = new RefreshToken();
  }

  @Data
  public static class RefreshToken {
    private boolean enabled = true;
    private long tokenExpiresInSeconds = 86400; // 24 hours
    private long maxLifeEndInSeconds = 2592000; // 30 days
  }

  @Data
  public static class JwtEncoderProperty {
    private String privateKey;
    private String secret;
    private long tokenExpiresInSeconds = 3600;
    private String keyId = "kraken";
    private String issuer = "https://kraken.consoleconnnect.com/issuer";
  }

  @Data
  public static class JwtDecoderProperty {
    private String issuer;
    private String keyId;
    private String jwksUri;
    private String publicKey;
    private String secret;
    private Introspection introspection = new Introspection();

    private Map<String, Object> verifier;
  }

  @Data
  public static class Introspection {
    private boolean enabled;
    private String url;
    private String clientId;
    private String clientSecret;
  }

  @Data
  public static class SecurityFilter {
    private boolean enabled;
    private List<FilterConfig> filterConfigs;
  }

  @Data
  public static class FilterConfig {
    private List<String> paths = List.of();
    private String filterName;
  }
}
