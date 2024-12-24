package com.consoleconnect.kraken.operator.sync.service;

import com.consoleconnect.kraken.operator.sync.model.SyncProperty;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service

// @ConditionalOnProperty(
//    prefix = "app.delete-log-conf",
//    value = "switch",
//    havingValue = "true")
@Slf4j
public class DeleteLogService {

  private final SyncProperty.DeleteLogConf deleteLogConf;

  public DeleteLogService(SyncProperty syncProperty) {
    this.deleteLogConf = syncProperty.getDeleteLogConf();
  }

  @Scheduled(cron = "${app.cron-job.delete-api-activity-log:-}")
  public void scanEvent() {
    if (this.deleteLogConf.getLogKind() != null) {
      log.info("xxxxxxxxxxxxxxxxxxx, {}", this.deleteLogConf.getLogKind().name());
    }
  }
}
