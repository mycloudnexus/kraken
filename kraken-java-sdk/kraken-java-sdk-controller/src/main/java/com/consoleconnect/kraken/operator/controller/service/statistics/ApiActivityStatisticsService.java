package com.consoleconnect.kraken.operator.controller.service.statistics;

import com.consoleconnect.kraken.operator.controller.dto.statistics.ApiRequestActivityStatistics;
import com.consoleconnect.kraken.operator.controller.dto.statistics.EndpointUsage;
import com.consoleconnect.kraken.operator.controller.dto.statistics.ErrorApiRequestStatistics;
import com.consoleconnect.kraken.operator.controller.dto.statistics.ErrorBreakdown;
import com.consoleconnect.kraken.operator.controller.dto.statistics.MostPopularEndpointStatistics;
import com.consoleconnect.kraken.operator.controller.dto.statistics.RequestStatistics;
import com.consoleconnect.kraken.operator.core.entity.AbstractHttpEntity;
import com.consoleconnect.kraken.operator.core.entity.ApiActivityLogEntity;
import com.consoleconnect.kraken.operator.core.repo.ApiActivityLogRepository;
import com.consoleconnect.kraken.operator.core.request.ApiStatisticsSearchRequest;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class ApiActivityStatisticsService {

  public static final String CREATED_AT = "createdAt";
  public static final String ENV = "env";
  public static final String CALL_SEQ = "callSeq";
  public static final String HTTP_STATUS_CODE = "httpStatusCode";
  public static final String CALL_SEQ_ZERO = "0";
  public static final String BUYER = "buyer";
  public static final int NUMBER_OF_MOST_POPULAR_ENDPOINT_LIMIT = 7;

  private final ApiActivityLogRepository repository;

  public ApiRequestActivityStatistics loadRequestStatistics(
      ApiStatisticsSearchRequest searchRequest) {
    var zoneId = searchRequest.getQueryStart().getZone();
    var logs = getApiActivityLogEntities(searchRequest);
    var logsGroupedByDay = groupByDayAndSuccessError(zoneId, logs);
    return createApiRequestActivityStatistics(logsGroupedByDay);
  }

  private List<ApiActivityLogEntity> getApiActivityLogEntities(
      ApiStatisticsSearchRequest searchRequest) {
    Specification<ApiActivityLogEntity> spec =
        (root, query, criteriaBuilder) -> {
          var predicateList = predicates(searchRequest, root, criteriaBuilder);
          if (searchRequest.getBuyerId() != null) {
            predicateList.add(criteriaBuilder.equal(root.get(BUYER), searchRequest.getBuyerId()));
          }
          return query.where(predicateList.toArray(new Predicate[0])).getRestriction();
        };
    return repository.findAll(spec);
  }

  private Map<ZonedDateTime, Map<RequestStatus, Long>> groupByDayAndSuccessError(
      ZoneId zoneId, List<ApiActivityLogEntity> logs) {
    return logs.stream()
        .collect(
            Collectors.groupingBy(
                entity ->
                    entity.getCreatedAt().withZoneSameInstant(zoneId).truncatedTo(ChronoUnit.DAYS),
                Collectors.groupingBy(
                    entity -> status(entity.getHttpStatusCode()), Collectors.counting())));
  }

  private ApiRequestActivityStatistics createApiRequestActivityStatistics(
      Map<ZonedDateTime, Map<RequestStatus, Long>> logsGroupedByDayAndHttpStatus) {
    var stats =
        logsGroupedByDayAndHttpStatus.entrySet().stream()
            .map(
                dateEntry ->
                    new RequestStatistics(
                        dateEntry.getKey().toLocalDate(),
                        dateEntry.getValue().get(RequestStatus.SUCCESS),
                        dateEntry.getValue().get(RequestStatus.ERROR)))
            .sorted(Comparator.comparing(RequestStatistics::getDate))
            .toList();
    return new ApiRequestActivityStatistics(stats);
  }

  private RequestStatus status(Integer statusCode) {
    return (HttpStatus.valueOf(statusCode).is2xxSuccessful())
        ? RequestStatus.SUCCESS
        : RequestStatus.ERROR;
  }

  public ErrorApiRequestStatistics loadErrorsStatistics(ApiStatisticsSearchRequest searchRequest) {
    var zoneId = searchRequest.getQueryStart().getZone();
    var errorLogs = getApiActivityLogErrorEntities(searchRequest);
    var logsGroupedByDayAndErrors = groupByDayAndStatus(zoneId, errorLogs);
    return createErrorApiRequestStatistics(logsGroupedByDayAndErrors);
  }

  private List<ApiActivityLogEntity> getApiActivityLogErrorEntities(
      ApiStatisticsSearchRequest searchRequest) {
    Specification<ApiActivityLogEntity> spec =
        (root, query, criteriaBuilder) -> {
          var predicateList = predicates(searchRequest, root, criteriaBuilder);
          if (searchRequest.getBuyerId() != null) {
            predicateList.add(criteriaBuilder.equal(root.get(BUYER), searchRequest.getBuyerId()));
          }
          predicateList.add(criteriaBuilder.greaterThanOrEqualTo(root.get(HTTP_STATUS_CODE), 400));
          predicateList.add(criteriaBuilder.lessThan(root.get(HTTP_STATUS_CODE), 600));
          return query.where(predicateList.toArray(new Predicate[0])).getRestriction();
        };
    return repository.findAll(spec);
  }

  private Map<ZonedDateTime, Map<Integer, Long>> groupByDayAndStatus(
      ZoneId zoneId, List<ApiActivityLogEntity> logs) {
    return logs.stream()
        .collect(
            Collectors.groupingBy(
                entity ->
                    entity.getCreatedAt().withZoneSameInstant(zoneId).truncatedTo(ChronoUnit.DAYS),
                Collectors.groupingBy(
                    AbstractHttpEntity::getHttpStatusCode, Collectors.counting())));
  }

  private ErrorApiRequestStatistics createErrorApiRequestStatistics(
      Map<ZonedDateTime, Map<Integer, Long>> stats) {
    var data =
        stats.entrySet().stream()
            .map(
                dateEntry ->
                    new ErrorBreakdown(dateEntry.getKey().toLocalDate(), dateEntry.getValue()))
            .sorted(Comparator.comparing(ErrorBreakdown::getDate))
            .toList();
    return new ErrorApiRequestStatistics(data);
  }

  public MostPopularEndpointStatistics loadMostPopularEndpointStatistics(
      ApiStatisticsSearchRequest searchRequest) {
    List<Object[]> endpointPerUsage =
        repository.findTopEndpoints(
            searchRequest.getEnv(),
            searchRequest.getQueryStart(),
            searchRequest.getQueryEnd(),
            CALL_SEQ_ZERO,
            searchRequest.getBuyerId(),
            NUMBER_OF_MOST_POPULAR_ENDPOINT_LIMIT);
    long numberOfAllRequests =
        repository.count(
            (root, query, criteriaBuilder) -> {
              var predicateList = predicates(searchRequest, root, criteriaBuilder);
              return query.where(predicateList.toArray(new Predicate[0])).getRestriction();
            });

    var data =
        endpointPerUsage.stream()
            .map(endpoint -> createEndpointUsage(endpoint, numberOfAllRequests))
            .toList();
    return new MostPopularEndpointStatistics(data);
  }

  private EndpointUsage createEndpointUsage(Object[] endpoint, long numberOfAllRequests) {
    var path = (String) endpoint[0];
    var method = (String) endpoint[1];
    var number = (Long) endpoint[2];
    var percentage =
        new BigDecimal(number)
            .divide(new BigDecimal(numberOfAllRequests), 5, RoundingMode.DOWN)
            .multiply(new BigDecimal(100))
            .setScale(2, RoundingMode.DOWN);
    return new EndpointUsage(method, path, number, percentage.doubleValue());
  }

  private List<Predicate> predicates(
      ApiStatisticsSearchRequest searchRequest,
      Root<ApiActivityLogEntity> root,
      CriteriaBuilder criteriaBuilder) {
    var predicateList = new ArrayList<Predicate>();
    predicateList.add(criteriaBuilder.equal(root.get(ENV), searchRequest.getEnv()));
    predicateList.add(criteriaBuilder.equal(root.get(CALL_SEQ), CALL_SEQ_ZERO));
    predicateList.add(
        criteriaBuilder.greaterThanOrEqualTo(root.get(CREATED_AT), searchRequest.getQueryStart()));
    predicateList.add(
        criteriaBuilder.lessThanOrEqualTo(root.get(CREATED_AT), searchRequest.getQueryEnd()));
    return predicateList;
  }

  enum RequestStatus {
    SUCCESS,
    ERROR
  }
}
