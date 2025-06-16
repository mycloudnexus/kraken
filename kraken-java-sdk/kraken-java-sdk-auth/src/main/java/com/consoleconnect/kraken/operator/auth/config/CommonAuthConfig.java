package com.consoleconnect.kraken.operator.auth.config;

import com.consoleconnect.kraken.operator.auth.model.AuthDataProperty;
import com.consoleconnect.kraken.operator.auth.model.BasicUserLoginEnabled;
import com.consoleconnect.kraken.operator.auth.model.ResourceServerEnabled;
import com.consoleconnect.kraken.operator.auth.model.UserLoginEnabled;
import com.consoleconnect.kraken.operator.auth.security.JWTSecurityGlobalFilter;
import com.consoleconnect.kraken.operator.auth.security.KrakenPasswordEncoder;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.server.WebFilter;

@Configuration
@Slf4j
public class CommonAuthConfig {
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

  @ConditionalOnBean(UserLoginEnabled.class)
  @Bean
  public KrakenPasswordEncoder passwordEncoder(AuthDataProperty.Login login) {
    return new KrakenPasswordEncoder(login);
  }

  @Bean(name = "securityGlobalFilter")
  @ConditionalOnBean(BasicUserLoginEnabled.class)
  public WebFilter securityGlobalFilter(AuthDataProperty.ResourceServer resourceServer) {
    return new JWTSecurityGlobalFilter(resourceServer);
  }

  public static CorsConfigurationSource corsConfigurationSource(
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
}
