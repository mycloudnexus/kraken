package com.consoleconnect.kraken.operator.core.config;

import com.consoleconnect.kraken.operator.core.enums.AchieveScopeEnum;
import com.consoleconnect.kraken.operator.core.enums.PlaneTypeEnum;
import com.consoleconnect.kraken.operator.core.model.AppProperty;
import com.consoleconnect.kraken.operator.core.toolkit.JsonToolkit;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
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

  @Data
  public static class AchieveApiActivityLogConf {

    public static final String ACHIEVE_LOG_CONFIG = "ACHIEVE_LOG_CONFIG";

    private PlaneTypeEnum logKind;

    private int month;
    private String protocol;
    private AchieveScopeEnum achieveScope;

    public ZonedDateTime toAchieve() {
      return ZonedDateTime.now().truncatedTo(ChronoUnit.DAYS).minusMonths(this.month);
    }

    public static boolean needAchieveMigrate(AchieveApiActivityLogConf conf) {
      log.info("{},{}", ACHIEVE_LOG_CONFIG, JsonToolkit.toJson(conf));
      if (conf == null) {
        return false;
      }

      var logKind = conf.getLogKind();
      if (logKind == null) {
        return false;
      }

      return logKind == PlaneTypeEnum.DATA_PLANE || logKind == PlaneTypeEnum.CONTROL_PLANE;
    }
  }
}
