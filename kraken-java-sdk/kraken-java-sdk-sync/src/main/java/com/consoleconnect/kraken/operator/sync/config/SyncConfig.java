package com.consoleconnect.kraken.operator.sync.config;

import com.consoleconnect.kraken.operator.sync.model.SyncProperty;
import javax.sql.DataSource;
import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.jdbctemplate.JdbcTemplateLockProvider;
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@EnableScheduling
@EnableSchedulerLock(defaultLockAtMostFor = "${app.cron-job.lock.at-most-for}")
public class SyncConfig {
  private static final int SIZE = 16 * 1024 * 1024;

  @Bean
  @ConfigurationProperties(prefix = "app")
  public SyncProperty syncProperty() {
    return new SyncProperty();
  }

  @Bean
  public LockProvider lockProvider(DataSource dataSource) {
    return new JdbcTemplateLockProvider(
        JdbcTemplateLockProvider.Configuration.builder()
            .withJdbcTemplate(new JdbcTemplate(dataSource))
            .withTableName("kraken_shed_lock")
            .usingDbTime()
            .build());
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
