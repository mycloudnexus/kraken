package com.consoleconnect.kraken.operator.auth.config;

import com.consoleconnect.kraken.operator.auth.model.AuthDataProperty;
import com.consoleconnect.kraken.operator.auth.model.DefaultUserLoginEnabled;
import com.consoleconnect.kraken.operator.auth.model.ResourceServerEnabled;
import com.consoleconnect.kraken.operator.auth.model.UserLoginEnabled;
import com.consoleconnect.kraken.operator.auth.security.*;
import com.consoleconnect.kraken.operator.auth.service.JwtDecoderService;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.server.resource.authentication.JwtIssuerReactiveAuthenticationManagerResolver;
import org.springframework.security.oauth2.server.resource.web.server.authentication.ServerBearerTokenAuthenticationConverter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

@Configuration
@Slf4j
@EnableWebFluxSecurity
@ConditionalOnBean(DefaultUserLoginEnabled.class)
public class WebSecurityConfig {

  @ConditionalOnMissingBean(ResourceServerEnabled.class)
  @Bean
  public SecurityWebFilterChain noAuthSecurityWebFilterChain(
      ServerHttpSecurity http, AuthDataProperty.ResourceServer resourceServer) {
    log.warn("Webflux security with no auth activated");
    return http.authorizeExchange(auth -> auth.anyExchange().permitAll())
        .csrf(ServerHttpSecurity.CsrfSpec::disable)
        .cors(
            cors ->
                cors.configurationSource(
                    corsConfigurationSource(
                        resourceServer.getCorsAllowedHeaders(),
                        resourceServer.getCorsAllowedOrigins(),
                        resourceServer.getCorsAllowedMethods())))
        .build();
  }

  @ConditionalOnBean(ResourceServerEnabled.class)
  @Bean
  @Order(1)
  public SecurityWebFilterChain securityWebFilterChain(
      ServerHttpSecurity http,
      AuthDataProperty.ResourceServer resourceServer,
      JwtIssuerReactiveAuthenticationManagerResolver resolver,
      ServerAuthenticationConverter authenticationConverter,
      Map<String, SecurityChecker> securityCheckerMap,
      ApplicationContext applicationContext) {
    http.csrf(ServerHttpSecurity.CsrfSpec::disable)
        .cors(
            cors ->
                cors.configurationSource(
                    corsConfigurationSource(
                        resourceServer.getCorsAllowedHeaders(),
                        resourceServer.getCorsAllowedOrigins(),
                        resourceServer.getCorsAllowedMethods())))
        .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
        .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
        .authorizeExchange(
            exchanges -> {
              // permitAll() is used to allow access to the specified paths
              resourceServer
                  .getDefaultAllowedPaths()
                  .forEach(path -> exchanges.pathMatchers(path).permitAll());
              resourceServer
                  .getAllowedPaths()
                  .forEach(path -> exchanges.pathMatchers(path).permitAll());
              // authenticated() is used to require authentication for the specified paths
              resourceServer
                  .getPathPermissions()
                  .forEach(
                      pathPermission ->
                          pathPermission
                              .getHttpMethods()
                              .forEach(
                                  httpMethod ->
                                      exchanges
                                          .pathMatchers(httpMethod, pathPermission.getPath())
                                          .hasAnyRole(
                                              pathPermission.getRoles().toArray(String[]::new))));

              // anyExchange() is used to require authentication for all other paths
              exchanges.anyExchange().authenticated();
            })
        .oauth2ResourceServer(
            oauth2 ->
                oauth2
                    .authenticationManagerResolver(resolver)
                    .bearerTokenConverter(authenticationConverter))
        .addFilterAfter(
            new JWTSecurityGlobalFilter(resourceServer), SecurityWebFiltersOrder.AUTHORIZATION);
    addSecurityFilter(http, resourceServer, securityCheckerMap);
    return http.build();
  }

  @ConditionalOnBean(ResourceServerEnabled.class)
  @Bean
  public ServerAuthenticationConverter customBearerTokenAuthenticationConverter(
      AuthDataProperty.ResourceServer resourceServer) {
    ServerBearerTokenAuthenticationConverter tokenAuthenticationConverter =
        new ServerBearerTokenAuthenticationConverter();
    tokenAuthenticationConverter.setBearerTokenHeaderName(
        resourceServer.getBearerTokenHeaderName());
    return tokenAuthenticationConverter;
  }

  @ConditionalOnBean(ResourceServerEnabled.class)
  @Bean
  public JwtIssuerReactiveAuthenticationManagerResolver resolver(
      TenantAuthenticationManagerResolver tenantAuthenticationManagerResolver) {
    return new JwtIssuerReactiveAuthenticationManagerResolver(tenantAuthenticationManagerResolver);
  }

  @ConditionalOnBean(ResourceServerEnabled.class)
  @Bean
  public TenantAuthenticationManagerResolver tenantAuthenticationManagerResolver(
      AuthDataProperty.ResourceServer resourceServer, JwtDecoderService decoderProvider) {
    return new TenantAuthenticationManagerResolver(resourceServer, decoderProvider);
  }

  @ConditionalOnBean(UserLoginEnabled.class)
  @Bean
  public KrakenPasswordEncoder passwordEncoder(AuthDataProperty.Login login) {
    return new KrakenPasswordEncoder(login);
  }

  CorsConfigurationSource corsConfigurationSource(
      List<String> allowedHeaders, List<String> allowedOrigins, List<String> allowedMethods) {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedHeaders(allowedHeaders);
    configuration.setAllowedOrigins(allowedOrigins);
    configuration.setAllowedMethods(allowedMethods);
    UrlBasedCorsConfigurationSource urlBasedCorsConfigurationSource =
        new UrlBasedCorsConfigurationSource();
    urlBasedCorsConfigurationSource.registerCorsConfiguration("/**", configuration);
    return urlBasedCorsConfigurationSource;
  }

  private void addSecurityFilter(
      ServerHttpSecurity http,
      AuthDataProperty.ResourceServer resourceServer,
      Map<String, SecurityChecker> securityCheckerMap) {
    AuthDataProperty.SecurityFilter securityFilter = resourceServer.getSecurityFilter();
    if (!securityFilter.isEnabled()) {
      return;
    }
    securityFilter.getFilterConfigs().stream()
        .filter(t -> securityCheckerMap.containsKey(t.getFilterName()))
        .map(
            config -> {
              SecurityChecker securityChecker = securityCheckerMap.get(config.getFilterName());
              return new SecurityCheckHolderFilter(
                  config.getFilterName(), config.getPaths(), securityChecker);
            })
        .sorted(Comparator.comparing(SecurityCheckHolderFilter::getOrder))
        .forEach(filter -> http.addFilterAfter(filter, SecurityWebFiltersOrder.AUTHORIZATION));
  }
}
