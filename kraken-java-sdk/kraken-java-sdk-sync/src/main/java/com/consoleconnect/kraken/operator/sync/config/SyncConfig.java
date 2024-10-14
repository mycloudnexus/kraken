package com.consoleconnect.kraken.operator.sync.config;

import com.consoleconnect.kraken.operator.sync.model.SyncProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class SyncConfig {
  private static final int SIZE = 16 * 1024 * 1024;

  @Bean
  @ConfigurationProperties(prefix = "app")
  public SyncProperty syncProperty() {
    return new SyncProperty();
  }

  @Bean
  public WebClient webClient(SyncProperty syncProperty) {
    ExchangeStrategies strategies =
        ExchangeStrategies.builder()
            .codecs(codecs -> codecs.defaultCodecs().maxInMemorySize(SIZE))
            .build();
    return WebClient.builder()
        .exchangeStrategies(strategies)
        .baseUrl(syncProperty.getControlPlane().getUrl())
        .build();
  }
}
