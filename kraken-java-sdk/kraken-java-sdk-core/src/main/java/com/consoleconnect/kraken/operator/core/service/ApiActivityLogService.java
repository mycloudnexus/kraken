package com.consoleconnect.kraken.operator.core.service;

import com.consoleconnect.kraken.operator.core.dto.ApiActivityLog;
import com.consoleconnect.kraken.operator.core.dto.ComposedHttpRequest;
import com.consoleconnect.kraken.operator.core.entity.ApiActivityLogEntity;
import com.consoleconnect.kraken.operator.core.mapper.ApiActivityLogMapper;
import com.consoleconnect.kraken.operator.core.repo.ApiActivityLogRepository;
import com.consoleconnect.kraken.operator.core.request.LogSearchRequest;
import com.consoleconnect.kraken.operator.core.toolkit.Paging;
import com.consoleconnect.kraken.operator.core.toolkit.PagingHelper;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class ApiActivityLogService {
  public static final String CREATED_AT = "createdAt";
  private final ApiActivityLogRepository repository;

  public Paging<ApiActivityLog> search(LogSearchRequest logSearchRequest, Pageable pageable) {

    Specification<ApiActivityLogEntity> spec =
        (root, query, criteriaBuilder) -> {
          List<Predicate> predicateList = new ArrayList<>();
          addEq("env", logSearchRequest.getEnv(), predicateList, criteriaBuilder, root);
          addEq("requestId", logSearchRequest.getRequestId(), predicateList, criteriaBuilder, root);
          addEq("method", logSearchRequest.getMethod(), predicateList, criteriaBuilder, root);
          addEq("path", logSearchRequest.getPath(), predicateList, criteriaBuilder, root);
          addEq("callSeq", "0", predicateList, criteriaBuilder, root);
          if (logSearchRequest.getQueryStart() != null) {
            predicateList.add(
                criteriaBuilder.greaterThan(
                    root.get(CREATED_AT), logSearchRequest.getQueryStart()));
          }
          if (logSearchRequest.getQueryEnd() != null) {
            predicateList.add(
                criteriaBuilder.lessThan(root.get(CREATED_AT), logSearchRequest.getQueryEnd()));
          }
          if (logSearchRequest.getStatusCode() != null) {
            predicateList.add(
                criteriaBuilder.equal(
                    root.get("httpStatusCode"), logSearchRequest.getStatusCode()));
          }
          Predicate[] predicateListArray = predicateList.toArray(new Predicate[0]);
          return query.where(predicateListArray).getRestriction();
        };
    Page<ApiActivityLogEntity> page = repository.findAll(spec, pageable);

    return PagingHelper.toPaging(page, ApiActivityLogMapper.INSTANCE::map);
  }

  private void addEq(
      String fieldName,
      String value,
      List<Predicate> predicateList,
      CriteriaBuilder criteriaBuilder,
      Root<ApiActivityLogEntity> root) {
    if (StringUtils.isNotBlank(value)) {
      predicateList.add(criteriaBuilder.equal(root.get(fieldName), value));
    }
  }

  public Optional<ComposedHttpRequest> getDetail(String requestId) {
    List<ApiActivityLog> httpRequests =
        this.repository.findAllByRequestId(requestId).stream()
            .map(ApiActivityLogMapper.INSTANCE::map)
            .sorted(Comparator.comparing(ApiActivityLog::getCallSeq))
            .toList();
    if (CollectionUtils.isEmpty(httpRequests)) {
      return Optional.empty();
    }
    ComposedHttpRequest composedHttpRequest = new ComposedHttpRequest();
    composedHttpRequest.setMain(httpRequests.get(0));
    if (httpRequests.size() > 1) {
      composedHttpRequest.setBranches(httpRequests.subList(1, httpRequests.size()));
    }
    return Optional.of(composedHttpRequest);
  }
}
