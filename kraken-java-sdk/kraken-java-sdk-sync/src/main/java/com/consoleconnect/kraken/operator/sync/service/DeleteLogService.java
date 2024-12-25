package com.consoleconnect.kraken.operator.sync.service;

import com.consoleconnect.kraken.operator.core.entity.ApiActivityLogEntity;
import com.consoleconnect.kraken.operator.core.enums.LogKindEnum;
import com.consoleconnect.kraken.operator.core.repo.ApiActivityLogBodyRepository;
import com.consoleconnect.kraken.operator.core.repo.ApiActivityLogRepository;
import com.consoleconnect.kraken.operator.sync.model.SyncProperty;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
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
    this.deleteApiLogAtDataPlane(this.deleteLogConf.getLogKind());
  }

  public void deleteApiLogAtDataPlane(LogKindEnum logKind) {
    ZonedDateTime toDelete =
        ZonedDateTime.now()
            .truncatedTo(ChronoUnit.DAYS)
            .minusMonths(this.deleteLogConf.getControlPlane().getMonth());

    log.info(
        "{}, {}, {}, start",
        DELETE_API_ACTIVITY_LOG,
        this.deleteLogConf.getLogKind().name(),
        toDelete);

    if (logKind != LogKindEnum.DATA_PLANE && logKind != LogKindEnum.CONTROL_PLANE) {
      log.info("{}, {}, skip", DELETE_API_ACTIVITY_LOG, this.deleteLogConf.getLogKind().name());
      return;
    }

    for (int page = 0; ; page++) {
      var list = this.apiActivityLogRepository.deleteExpiredLog(toDelete, PageRequest.of(page, 20));
      if (list.isEmpty()) {
        break;
      }
      var bodySet =
          list.stream()
              .map(ApiActivityLogEntity::getApiLogBodyEntity)
              .filter(Objects::nonNull)
              .toList();
      if (logKind == LogKindEnum.DATA_PLANE) {
        this.apiActivityLogRepository.deleteAll(list);
      } else {

        list.forEach(x -> x.setApiLogBodyEntity(null));
        this.apiActivityLogRepository.saveAll(list);
      }
      this.apiActivityLogBodyRepository.deleteAll(bodySet);
    }

    log.info("{}, {}, end", DELETE_API_ACTIVITY_LOG, this.deleteLogConf.getLogKind().name());
  }
}
