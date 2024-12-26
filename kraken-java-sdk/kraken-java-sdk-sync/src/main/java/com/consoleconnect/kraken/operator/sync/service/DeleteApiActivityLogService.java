package com.consoleconnect.kraken.operator.sync.service;

import com.consoleconnect.kraken.operator.core.service.ApiActivityLogService;
import com.consoleconnect.kraken.operator.sync.model.SyncProperty;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DeleteApiActivityLogService {

  private final SyncProperty.DeleteLogConf deleteLogConf;
  private ApiActivityLogService apiActivityLogService;

  public DeleteApiActivityLogService(SyncProperty syncProperty) {
    this.deleteLogConf = syncProperty.getDeleteLogConf();
  }

  @Scheduled(cron = "${app.cron-job.delete-api-activity-log:-}")
  public void runIt() {
    ZonedDateTime toDelete =
        ZonedDateTime.now()
            .truncatedTo(ChronoUnit.DAYS)
            .minusMonths(this.deleteLogConf.getControlPlane().getMonth());
    this.apiActivityLogService.achieveApiActivityLog(this.deleteLogConf.getLogKind(), toDelete);
  }
}
