package com.consoleconnect.kraken.operator.core.service;

import com.consoleconnect.kraken.operator.core.toolkit.Constants;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ConfigurationMonitorService {

  @Component
  @Slf4j
  @ConfigurationProperties(prefix = "app")
  @Getter
  @Setter
  public static class CronJobProperties {

    private Map<String, Object> cronJob;
  }

  private final Set<String> monitorList =
      Set.of(
          "lock",
          "push-heartbeat",
          "check-release",
          "pull-api-server-info",
          "pull-server-assets",
          "push-heartbeat-collector",
          "sync-system-info-from-control-plane",
          "push-log",
          "push-mgmt-event",
          "push-running-mapper",
          "push-workflow-status",
          "pull-reset-event",
          "push-log-external-system",
          "pull-latest-release");

  public ConfigurationMonitorService(CronJobProperties cronJobProperties) {
    if (Objects.nonNull(cronJobProperties.getCronJob())) {

      log.info("[{}] cron job configurations:", Constants.LOG_FIELD_CRON_JOB);

      cronJobProperties.getCronJob().entrySet().stream()
          .filter(entry -> monitorList.contains(entry.getKey()))
          .forEach(
              entry ->
                log.info(
                    "[{}] Configuration app.cron-job.{}: {}",
                    Constants.LOG_FIELD_CRON_JOB,
                    entry.getKey(),
                    entry.getValue())
              );

      checkLock(cronJobProperties.getCronJob());
    } else {
      log.error("[{}] no cron job configured", Constants.LOG_FIELD_CRON_JOB);
    }
  }

  private void checkLock(Map<String, Object> cronJob) {
    if (!cronJob.containsKey("lock")) {
      log.error("[{}] no cron job lock specified", Constants.LOG_FIELD_CRON_JOB);
      return;
    }

    try {
      final Map<String, String> lock = (Map<String, String>) cronJob.get("lock");
      if (!lock.containsKey("at-most-for")) {
        log.error("[{}] app.cron-job.lock.at-most-for not found", Constants.LOG_FIELD_CRON_JOB);
      }
    } catch (Exception e) {
      log.error("[{}] invalid cron job lock", Constants.LOG_FIELD_CRON_JOB);
    }
  }
}
