package com.consoleconnect.kraken.operator.sync.service;

import com.consoleconnect.kraken.operator.core.service.ApiActivityLogService;
import com.consoleconnect.kraken.operator.sync.model.SyncProperty;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class MigrateApiActivityLogService {

  private final SyncProperty.DeleteLogConf deleteLogConf;
  private ApiActivityLogService apiActivityLogService;

  public MigrateApiActivityLogService(SyncProperty syncProperty) {
    this.deleteLogConf = syncProperty.getDeleteLogConf();
  }

  @Scheduled(cron = "${app.cron-job.migrate-api-activity-log:-}")
  public void runIt() {

    this.apiActivityLogService.migrateApiLog(this.deleteLogConf.getLogKind());
  }
}