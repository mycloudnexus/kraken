package com.consoleconnect.kraken.operator.sync.service.push;

import static com.consoleconnect.kraken.operator.core.toolkit.JsonToolkit.fromJson;
import static com.consoleconnect.kraken.operator.core.toolkit.PagingHelper.toPageNoSubList;
import static java.util.Collections.emptyList;

import com.consoleconnect.kraken.operator.core.dto.ApiActivityLog;
import com.consoleconnect.kraken.operator.core.dto.ComposedHttpRequest;
import com.consoleconnect.kraken.operator.core.entity.AbstractHttpEntity;
import com.consoleconnect.kraken.operator.core.entity.ApiActivityLogEntity;
import com.consoleconnect.kraken.operator.core.entity.MgmtEventEntity;
import com.consoleconnect.kraken.operator.core.enums.EventStatusType;
import com.consoleconnect.kraken.operator.core.enums.MgmtEventType;
import com.consoleconnect.kraken.operator.core.exception.KrakenException;
import com.consoleconnect.kraken.operator.core.mapper.ApiActivityLogMapper;
import com.consoleconnect.kraken.operator.core.model.HttpResponse;
import com.consoleconnect.kraken.operator.core.repo.ApiActivityLogRepository;
import com.consoleconnect.kraken.operator.core.repo.MgmtEventRepository;
import com.consoleconnect.kraken.operator.sync.model.SyncProperty;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(
    value = "app.control-plane.push-activity-log-external.enabled",
    havingValue = "true")
public class PushAPIActivityLogScheduler {

  public static final String CALL_SEQ = "callSeq";
  public static final String CALL_SEQ_ZERO = "0";
  public static final String ENV = "env";
  public static final String CREATED_AT = "createdAt";

  private final MgmtEventRepository mgmtEventRepository;
  private final ApiActivityLogRepository apiActivityLogRepository;
  private final SyncProperty appProperty;
  private final WebClient webClient;

  @Scheduled(cron = "${app.cron-job.push-log-external-system:-}")
  List<PushExternalSystemPayload> pushApiActivityLogToExternalSystem() {
    Optional<MgmtEventEntity> entity =
        mgmtEventRepository.findFirstByEventTypeAndStatus(
            MgmtEventType.PUSH_API_ACTIVITY_LOG.name(), EventStatusType.ACK.name());
    if (entity.isPresent()) {
      log.info("Start pushing log to external system for event id: {}", entity.get().getId());
      var start = ZonedDateTime.now();
      var sent = pushLogs(entity.get());
      log.info(
          "End pushing log to external system for event id: {} in {} seconds.",
          entity.get().getId(),
          Duration.between(start, ZonedDateTime.now()).getSeconds());
      return sent;
    } else {
      log.info("No push api activity log event found.");
      return emptyList();
    }
  }

  private List<PushExternalSystemPayload> pushLogs(MgmtEventEntity mgmtEvent) {
    mgmtEvent.setStatus(EventStatusType.IN_PROGRESS.name());
    mgmtEventRepository.save(mgmtEvent);
    var logInfo = fromJson(mgmtEvent.getPayload(), PushLogActivityLogInfo.class);
    try {
      var sent = pushLogsInBatches(logInfo, mgmtEvent.getId());
      mgmtEvent.setStatus(EventStatusType.DONE.name());
      mgmtEventRepository.save(mgmtEvent);
      return sent;
    } catch (Exception ex) {
      mgmtEvent.setStatus(EventStatusType.FAILED.name());
      mgmtEventRepository.save(mgmtEvent);
      return emptyList();
    }
  }

  private List<PushExternalSystemPayload> pushLogsInBatches(
      PushLogActivityLogInfo logInfo, UUID eventId) {
    int page = 0;
    var sent = new ArrayList<PushExternalSystemPayload>();
    while (true) {
      var pageable =
          PageRequest.of(
              page, appProperty.getControlPlane().getPushActivityLogExternal().getBatchSize());
      var entities = getApiActivityLogRequestIds(logInfo, pageable);
      var composedLogs = getComposedHttpRequests(entities.get());
      if (composedLogs.isEmpty()) {
        log.info("No API activity logs found to push to external system.");
        break;
      }
      var payload =
          new PushExternalSystemPayload(
              eventId,
              logInfo.getStartTime(),
              logInfo.getEndTime(),
              logInfo.getEnvName(),
              toPageNoSubList(
                  composedLogs,
                  entities.getNumber(),
                  entities.getSize(),
                  entities.getTotalElements()));
      var res = sendLogsToExternalSystem(payload);
      if (res.getCode() != 200) {
        throw new KrakenException(
            400, "Pushing logs to external system filed with status: " + res.getCode());
      }
      page++;
      sent.add(payload);
    }
    return sent;
  }

  private HttpResponse<String> sendLogsToExternalSystem(PushExternalSystemPayload payload) {
    return webClient
        .method(HttpMethod.POST)
        .uri(appProperty.getControlPlane().getUrl())
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .header(
            appProperty.getControlPlane().getTokenHeader(),
            appProperty.getControlPlane().getToken())
        .bodyValue(payload)
        .retrieve()
        .bodyToMono(new ParameterizedTypeReference<HttpResponse<String>>() {})
        .block();
  }

  private List<ComposedHttpRequest> getComposedHttpRequests(Stream<ApiActivityLogEntity> logs) {
    var requestIds = logs.map(AbstractHttpEntity::getRequestId).toList();
    return this.apiActivityLogRepository.findAllByRequestIdIn(requestIds).stream()
        .map(ApiActivityLogMapper.INSTANCE::map)
        .collect(Collectors.groupingBy(ApiActivityLog::getRequestId))
        .values()
        .stream()
        .map(
            apiActivityLogs -> {
              apiActivityLogs.sort(Comparator.comparing(ApiActivityLog::getCallSeq));
              ComposedHttpRequest composedHttpRequest = new ComposedHttpRequest();
              composedHttpRequest.setMain(apiActivityLogs.get(0));
              if (apiActivityLogs.size() > 1) {
                composedHttpRequest.setBranches(apiActivityLogs.subList(1, apiActivityLogs.size()));
              }
              return composedHttpRequest;
            })
        .toList();
  }

  private Page<ApiActivityLogEntity> getApiActivityLogRequestIds(
      PushLogActivityLogInfo logInfo, Pageable pageable) {
    Specification<ApiActivityLogEntity> spec =
        (root, query, criteriaBuilder) -> {
          var predicateList = predicates(logInfo, root, criteriaBuilder);
          return query.where(predicateList.toArray(new Predicate[0])).getRestriction();
        };
    return apiActivityLogRepository.findAll(spec, pageable);
  }

  private List<Predicate> predicates(
      PushLogActivityLogInfo logInfo,
      Root<ApiActivityLogEntity> root,
      CriteriaBuilder criteriaBuilder) {
    var predicateList = new ArrayList<Predicate>();
    predicateList.add(criteriaBuilder.equal(root.get(ENV), logInfo.getEnvId()));
    predicateList.add(criteriaBuilder.equal(root.get(CALL_SEQ), CALL_SEQ_ZERO));
    predicateList.add(
        criteriaBuilder.greaterThanOrEqualTo(root.get(CREATED_AT), logInfo.getStartTime()));
    predicateList.add(
        criteriaBuilder.lessThanOrEqualTo(root.get(CREATED_AT), logInfo.getEndTime()));
    return predicateList;
  }
}