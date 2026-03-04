package com.consoleconnect.kraken.operator.core.config;

import com.consoleconnect.kraken.operator.core.enums.ArchiveScopeEnum;
import com.consoleconnect.kraken.operator.core.model.AppProperty;
import com.consoleconnect.kraken.operator.core.toolkit.JsonToolkit;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class AppConfig {

  @Bean
  @ConfigurationProperties(prefix = "app")
  public AppProperty appProperty() {
    return new AppProperty();
  }

  @Bean
  @ConditionalOnProperty(value = "app.dual-version-config.enabled", havingValue = "true")
  AppProperty.DualVersionConfig dualVersionConfig(AppProperty appProperty) {
    return appProperty.getDualVersionConfig();
  }

  @Data
  public static class ArchiveApiActivityLogConf {

    public static final String ARCHIVE_LOG_CONFIG = "ARCHIVE_LOG_CONFIG";

    private int month;

    private String protocol; // for example: GET、POST、PATCH、DELETE
    private ArchiveScopeEnum archiveScope;
    private int page;

    public ZonedDateTime toArchive() {
      return ZonedDateTime.now().truncatedTo(ChronoUnit.DAYS).minusMonths(this.month);
    }

    public static boolean needArchiveMigrate(ArchiveApiActivityLogConf conf) {
      log.info("{},{}", ARCHIVE_LOG_CONFIG, JsonToolkit.toJson(conf));
      return conf != null;
    }
  }
}
