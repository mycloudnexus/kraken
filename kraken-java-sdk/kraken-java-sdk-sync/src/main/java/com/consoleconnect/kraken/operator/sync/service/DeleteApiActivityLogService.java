package com.consoleconnect.kraken.operator.sync.service;

import static com.consoleconnect.kraken.operator.core.config.AppConfig.AchieveApiActivityLogConf.ACHIEVE_LOG_CONFIG;

import com.consoleconnect.kraken.operator.core.config.AppConfig;
import com.consoleconnect.kraken.operator.core.service.ApiActivityLogService;
import com.consoleconnect.kraken.operator.core.toolkit.JsonToolkit;
import com.consoleconnect.kraken.operator.sync.model.SyncProperty;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DeleteApiActivityLogService {

  private final AppConfig.AchieveApiActivityLogConf deleteLogConf;
  private ApiActivityLogService apiActivityLogService;

  public DeleteApiActivityLogService(SyncProperty syncProperty) {
    this.deleteLogConf = syncProperty.getAchieveLogConf();
    log.info("{}, {}", ACHIEVE_LOG_CONFIG, JsonToolkit.toJson(this.deleteLogConf));
  }

  @Scheduled(cron = "${app.cron-job.delete-api-activity-log:-}")
  public void runIt() {

    this.apiActivityLogService.achieveApiActivityLog(this.deleteLogConf);
  }
}
