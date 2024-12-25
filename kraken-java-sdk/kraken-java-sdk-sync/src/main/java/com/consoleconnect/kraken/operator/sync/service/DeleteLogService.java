package com.consoleconnect.kraken.operator.sync.service;

import com.consoleconnect.kraken.operator.core.entity.ApiActivityLogEntity;
import com.consoleconnect.kraken.operator.core.enums.LogKindEnum;
import com.consoleconnect.kraken.operator.core.repo.ApiActivityLogBodyRepository;
import com.consoleconnect.kraken.operator.core.repo.ApiActivityLogRepository;
import com.consoleconnect.kraken.operator.sync.model.SyncProperty;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DeleteLogService {

  private final SyncProperty.DeleteLogConf deleteLogConf;
  private final ApiActivityLogRepository apiActivityLogRepository;
  private final ApiActivityLogBodyRepository apiActivityLogBodyRepository;
  private static final String DELETE_API_ACTIVITY_LOG = "DELETE_API_ACTIVITY_LOG";

  public DeleteLogService(
      SyncProperty syncProperty,
      ApiActivityLogRepository apiActivityLogRepository,
      ApiActivityLogBodyRepository apiActivityLogBodyRepository) {
    this.deleteLogConf = syncProperty.getDeleteLogConf();
    this.apiActivityLogRepository = apiActivityLogRepository;
    this.apiActivityLogBodyRepository = apiActivityLogBodyRepository;
  }

  @Scheduled(cron = "${app.cron-job.delete-api-activity-log:-}")
  public void runIt() {

    if (LogKindEnum.CONTROL_PLANE == this.deleteLogConf.getLogKind()) {

      this.deleteApiLogAtDataPlane();
    }
  }

  private void deleteApiLogAtDataPlane() {
    ZonedDateTime toDelete =
        ZonedDateTime.now()
            .truncatedTo(ChronoUnit.DAYS)
            .minusMonths(this.deleteLogConf.getControlPlane().getMonth());

    log.info(
        "{}, {}, {}, start",
        DELETE_API_ACTIVITY_LOG,
        this.deleteLogConf.getLogKind().name(),
        toDelete);
    do {
      var list = this.apiActivityLogRepository.deleteExpiredLog(toDelete, PageRequest.of(0, 20));
      if (list.isEmpty()) {
        break;
      }
      this.apiActivityLogRepository.deleteAll(list);
      this.apiActivityLogBodyRepository.deleteAll(
          list.stream().map(ApiActivityLogEntity::getApiLogBodyEntity).toList());

    } while (true);

    log.info("{}, {}, end", DELETE_API_ACTIVITY_LOG, this.deleteLogConf.getLogKind().name());
  }
}
