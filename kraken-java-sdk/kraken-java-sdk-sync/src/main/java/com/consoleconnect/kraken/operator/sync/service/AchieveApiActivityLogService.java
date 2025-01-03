package com.consoleconnect.kraken.operator.sync.service;

import static com.consoleconnect.kraken.operator.core.config.AppConfig.AchieveApiActivityLogConf.ACHIEVE_LOG_CONFIG;

import com.consoleconnect.kraken.operator.core.config.AppConfig;
import com.consoleconnect.kraken.operator.core.service.ApiActivityLogService;
import com.consoleconnect.kraken.operator.core.toolkit.JsonToolkit;
import com.consoleconnect.kraken.operator.sync.model.SyncProperty;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AchieveApiActivityLogService {

  private final AppConfig.AchieveApiActivityLogConf deleteLogConf;
  private final ApiActivityLogService apiActivityLogService;

  public AchieveApiActivityLogService(
      SyncProperty syncProperty, ApiActivityLogService apiActivityLogService) {
    this.deleteLogConf = syncProperty.getAchieveLogConf();
    this.apiActivityLogService = apiActivityLogService;
    log.info("{}, {}", ACHIEVE_LOG_CONFIG, JsonToolkit.toJson(this.deleteLogConf));
  }

  @SchedulerLock(
      name = "achieveLogLock",
      lockAtMostFor = "${app.cron-job.lock.at-most-for}",
      lockAtLeastFor = "${app.cron-job.lock.at-least-for}")
  @Scheduled(cron = "${app.cron-job.achieve-api-activity-log:-}")
  public void runIt() {
    log.info("{}, {}, run it", ACHIEVE_LOG_CONFIG, JsonToolkit.toJson(this.deleteLogConf));
    this.achieveApiActivityLog(this.deleteLogConf);
  }

  public void achieveApiActivityLog(AppConfig.AchieveApiActivityLogConf activityLogConf) {

    log.info("{}, start", ACHIEVE_LOG_CONFIG);
    for (int page = 0; page < 100; page++) {
      if (this.apiActivityLogService.achieveOnePage(activityLogConf)) {
        break;
      }
    }
    log.info("{}, end", ACHIEVE_LOG_CONFIG);
  }
}
