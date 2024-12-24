package com.consoleconnect.kraken.operator.sync.service;

import com.consoleconnect.kraken.operator.core.enums.LogKindEnum;
import com.consoleconnect.kraken.operator.core.repo.ApiActivityLogRepository;
import com.consoleconnect.kraken.operator.sync.model.SyncProperty;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DeleteLogService {

  private final SyncProperty.DeleteLogConf deleteLogConf;
  private final ApiActivityLogRepository apiActivityLogRepository;

  public DeleteLogService(
      SyncProperty syncProperty, ApiActivityLogRepository apiActivityLogRepository) {
    this.deleteLogConf = syncProperty.getDeleteLogConf();
    this.apiActivityLogRepository = apiActivityLogRepository;
  }

  @Scheduled(cron = "${app.cron-job.delete-api-activity-log:-}")
  public void scanEvent() {
    if (LogKindEnum.CONTROL_PLANE == this.deleteLogConf.getLogKind()) {

      log.info("xxxxxxxxxxxxxxxxxxx, {}", this.deleteLogConf.getLogKind().name());
      ZonedDateTime toDelete =
          ZonedDateTime.now()
              .truncatedTo(ChronoUnit.DAYS)
              .minusMonths(this.deleteLogConf.getControlPlane().getMonth());

      log.info("xxxxxxxxxxxxxxxxxx, {}", toDelete);
      this.apiActivityLogRepository.deleteExpiredLog(toDelete);
      log.info("xxxxxxxxxxxxxxxxxxxx, end");
    }
  }
}
