package com.consoleconnect.kraken.operator.auth.config;

import com.consoleconnect.kraken.operator.auth.model.AuthDataProperty;
import lombok.AllArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@AllArgsConstructor
@Configuration
public class AuthDataConfig {
  @Bean
  @ConfigurationProperties(prefix = "app.security")
  public AuthDataProperty authDataProperty() {
    return new AuthDataProperty();
  }

  @Bean
  @ConfigurationProperties(prefix = "app.security.auth-server")
  public AuthDataProperty.AuthServer authServer() {
    return new AuthDataProperty.AuthServer();
  }

  @Bean
  @ConfigurationProperties(prefix = "app.security.resource-server")
  public AuthDataProperty.ResourceServer resourceServer() {
    return new AuthDataProperty.ResourceServer();
  }

  @Bean
  @ConfigurationProperties(prefix = "app.security.login")
  public AuthDataProperty.Login login() {
    return new AuthDataProperty.Login();
  }
}
