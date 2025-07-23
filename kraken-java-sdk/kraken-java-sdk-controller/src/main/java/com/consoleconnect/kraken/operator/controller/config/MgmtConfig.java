package com.consoleconnect.kraken.operator.controller.config;

import com.consoleconnect.kraken.operator.controller.config.vault.VaultClient;
import com.consoleconnect.kraken.operator.controller.config.vault.VaultProperty;
import com.consoleconnect.kraken.operator.controller.model.MgmtEndpointEnabled;
import com.consoleconnect.kraken.operator.controller.model.MgmtProperty;
import com.consoleconnect.kraken.operator.controller.model.TokenStorageEnabled;
import com.consoleconnect.kraken.operator.controller.repo.TokenStorageRepository;
import com.consoleconnect.kraken.operator.controller.service.PostgresTokenStorageServiceImpl;
import com.consoleconnect.kraken.operator.controller.service.TokenStorageService;
import com.consoleconnect.kraken.operator.controller.service.VaultTokenStorageServiceImpl;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MgmtConfig {

  @Bean
  @ConditionalOnProperty(value = "app.mgmt.enabled", havingValue = "true", matchIfMissing = false)
  public MgmtEndpointEnabled mgmtEndpointEnabled() {
    return new MgmtEndpointEnabled();
  }

  @Bean
  @ConfigurationProperties(prefix = "app.mgmt")
  public MgmtProperty mgmtProperty() {
    return new MgmtProperty();
  }

  @Bean(name = "postgresTokenStorage")
  @ConditionalOnProperty(value = "app.mgmt.token-storage.engine", havingValue = "postgres")
  public TokenStorageService postgresStorageService(TokenStorageRepository tokenStorageRepository) {
    return new PostgresTokenStorageServiceImpl(tokenStorageRepository);
  }

  @Bean(name = "vaultTokenStorage")
  @ConditionalOnProperty(value = "app.mgmt.token-storage.engine", havingValue = "vault")
  public TokenStorageService vaultStorageService(
      VaultClient vaultClient, VaultProperty vaultProperty) {
    return new VaultTokenStorageServiceImpl(vaultClient, vaultProperty);
  }

  @Bean
  @ConfigurationProperties(prefix = "app.mgmt.token-storage")
  public TokenStorageEnabled mgmtTokenStorageEnabled() {
    return new TokenStorageEnabled();
  }

  @Bean
  @ConfigurationProperties(prefix = "app.mgmt.vault")
  public VaultProperty vaultProperty() {
    return new VaultProperty();
  }
}
