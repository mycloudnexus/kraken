package com.consoleconnect.kraken.operator.core.service;

import static com.consoleconnect.kraken.operator.core.enums.AssetKindEnum.PRODUCT_BUYER;
import static com.consoleconnect.kraken.operator.core.toolkit.LabelConstants.LABEL_BUYER_ID;

import com.consoleconnect.kraken.operator.core.dto.ApiActivityLog;
import com.consoleconnect.kraken.operator.core.dto.ComposedHttpRequest;
import com.consoleconnect.kraken.operator.core.dto.UnifiedAssetDto;
import com.consoleconnect.kraken.operator.core.entity.ApiActivityLogEntity;
import com.consoleconnect.kraken.operator.core.entity.UnifiedAssetEntity;
import com.consoleconnect.kraken.operator.core.mapper.ApiActivityLogMapper;
import com.consoleconnect.kraken.operator.core.model.UnifiedAsset;
import com.consoleconnect.kraken.operator.core.model.facet.BuyerOnboardFacets;
import com.consoleconnect.kraken.operator.core.repo.ApiActivityLogRepository;
import com.consoleconnect.kraken.operator.core.repo.UnifiedAssetRepository;
import com.consoleconnect.kraken.operator.core.request.LogSearchRequest;
import com.consoleconnect.kraken.operator.core.toolkit.Paging;
import com.consoleconnect.kraken.operator.core.toolkit.PagingHelper;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.*;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
@Slf4j
public class ApiActivityLogService {
  public static final String CREATED_AT = "createdAt";
  private final ApiActivityLogRepository repository;
  private final UnifiedAssetRepository unifiedAssetRepository;

  @Transactional(readOnly = true)
  public Paging<ApiActivityLog> search(LogSearchRequest logSearchRequest, Pageable pageable) {
    Specification<ApiActivityLogEntity> spec = buildSearchQuery(logSearchRequest);
    Page<ApiActivityLogEntity> page = repository.findAll(spec, pageable);

    Map<String, UnifiedAssetDto> buyerIdEntityMap = searchBuyers(logSearchRequest.getEnv());
    return PagingHelper.toPaging(
        page,
        entity -> {
          ApiActivityLog apiActivityLog = ApiActivityLogMapper.INSTANCE.map(entity);
          UnifiedAssetDto buyerAssetDto =
              buyerIdEntityMap.getOrDefault(apiActivityLog.getBuyer(), null);
          if (Objects.nonNull(buyerAssetDto)) {
            BuyerOnboardFacets facets =
                UnifiedAsset.getFacets(buyerAssetDto, BuyerOnboardFacets.class);
            apiActivityLog.setBuyerName(
                facets.getBuyerInfo() == null ? "" : facets.getBuyerInfo().getCompanyName());
          }
          return apiActivityLog;
        });
  }

  private Specification<ApiActivityLogEntity> buildSearchQuery(LogSearchRequest logSearchRequest) {
    return (root, query, criteriaBuilder) -> {
      List<Predicate> predicateList = new ArrayList<>();
      addEq("env", logSearchRequest.getEnv(), predicateList, criteriaBuilder, root);
      addEq("requestId", logSearchRequest.getRequestId(), predicateList, criteriaBuilder, root);
      addEq("method", logSearchRequest.getMethod(), predicateList, criteriaBuilder, root);
      addEq("path", logSearchRequest.getPath(), predicateList, criteriaBuilder, root);
      addEq("callSeq", "0", predicateList, criteriaBuilder, root);
      if (logSearchRequest.getQueryStart() != null) {
        predicateList.add(
            criteriaBuilder.greaterThan(root.get(CREATED_AT), logSearchRequest.getQueryStart()));
      }
      if (logSearchRequest.getQueryEnd() != null) {
        predicateList.add(
            criteriaBuilder.lessThan(root.get(CREATED_AT), logSearchRequest.getQueryEnd()));
      }
      if (logSearchRequest.getStatusCode() != null) {
        predicateList.add(
            criteriaBuilder.equal(root.get("httpStatusCode"), logSearchRequest.getStatusCode()));
      }
      Predicate[] predicateListArray = predicateList.toArray(new Predicate[0]);
      return query.where(predicateListArray).getRestriction();
    };
  }

  public Map<String, UnifiedAssetDto> searchBuyers(String envId) {
    Page<UnifiedAssetEntity> buyerPage =
        unifiedAssetRepository.findBuyers(
            null, PRODUCT_BUYER.getKind(), envId, null, null, null, PageRequest.of(0, 100));
    Map<String, UnifiedAssetDto> buyerIdAssetMap = new HashMap<>();
    if (CollectionUtils.isEmpty(buyerPage.getContent())) {
      return buyerIdAssetMap;
    }
    return buyerPage.getContent().stream()
        .collect(
            Collectors.toMap(
                entity -> entity.getLabels().get(LABEL_BUYER_ID),
                entity -> UnifiedAssetService.toAsset(entity, true)));
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
