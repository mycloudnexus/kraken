package com.consoleconnect.kraken.operator.core.service;

import static com.consoleconnect.kraken.operator.core.enums.AssetKindEnum.PRODUCT_BUYER;
import static com.consoleconnect.kraken.operator.core.toolkit.LabelConstants.LABEL_BUYER_ID;

import com.consoleconnect.kraken.operator.core.client.ClientEvent;
import com.consoleconnect.kraken.operator.core.dto.ApiActivityLog;
import com.consoleconnect.kraken.operator.core.dto.ComposedHttpRequest;
import com.consoleconnect.kraken.operator.core.dto.UnifiedAssetDto;
import com.consoleconnect.kraken.operator.core.entity.ApiActivityLogBodyEntity;
import com.consoleconnect.kraken.operator.core.entity.ApiActivityLogEntity;
import com.consoleconnect.kraken.operator.core.entity.UnifiedAssetEntity;
import com.consoleconnect.kraken.operator.core.enums.LogKindEnum;
import com.consoleconnect.kraken.operator.core.enums.SyncStatusEnum;
import com.consoleconnect.kraken.operator.core.mapper.ApiActivityLogMapper;
import com.consoleconnect.kraken.operator.core.model.HttpResponse;
import com.consoleconnect.kraken.operator.core.model.UnifiedAsset;
import com.consoleconnect.kraken.operator.core.model.facet.BuyerOnboardFacets;
import com.consoleconnect.kraken.operator.core.repo.ApiActivityLogBodyRepository;
import com.consoleconnect.kraken.operator.core.repo.ApiActivityLogRepository;
import com.consoleconnect.kraken.operator.core.repo.UnifiedAssetRepository;
import com.consoleconnect.kraken.operator.core.request.LogSearchRequest;
import com.consoleconnect.kraken.operator.core.toolkit.JsonToolkit;
import com.consoleconnect.kraken.operator.core.toolkit.Paging;
import com.consoleconnect.kraken.operator.core.toolkit.PagingHelper;
import com.fasterxml.jackson.core.type.TypeReference;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.time.ZonedDateTime;
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
  private final ApiActivityLogBodyRepository apiActivityLogBodyRepository;

  private static final String DELETE_API_ACTIVITY_LOG = "DELETE_API_ACTIVITY_LOG";

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
            apiActivityLog.setBuyerId(buyerAssetDto.getId());
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

  public void deleteApiLogAtDataPlane(LogKindEnum logKind, ZonedDateTime toDelete) {

    log.info("{}, {}, {}, start", DELETE_API_ACTIVITY_LOG, logKind.name(), toDelete);

    if (logKind != LogKindEnum.DATA_PLANE && logKind != LogKindEnum.CONTROL_PLANE) {
      log.info("{}, {}, skip", DELETE_API_ACTIVITY_LOG, logKind.name());
      return;
    }

    for (int page = 0; ; page++) {
      var list = this.repository.deleteExpiredLog(toDelete, PageRequest.of(page, 20));
      if (list.isEmpty()) {
        break;
      }
      var bodySet =
          list.stream()
              .map(ApiActivityLogEntity::getApiLogBodyEntity)
              .filter(Objects::nonNull)
              .toList();
      if (logKind == LogKindEnum.DATA_PLANE) {
        this.repository.deleteAll(list);
      } else {

        list.forEach(x -> x.setApiLogBodyEntity(null));
        this.repository.saveAll(list);
      }
      this.apiActivityLogBodyRepository.deleteAll(bodySet);
    }

    log.info("{}, {}, end", DELETE_API_ACTIVITY_LOG, logKind.name());
  }

  @Transactional
  public HttpResponse<Void> receiveClientLog(String envId, String userId, ClientEvent event) {
    if (event.getEventPayload() == null) {
      return HttpResponse.ok(null);
    }
    List<ApiActivityLog> requestList =
        JsonToolkit.fromJson(event.getEventPayload(), new TypeReference<>() {});

    if (CollectionUtils.isEmpty(requestList)) {
      return HttpResponse.ok(null);
    }
    Set<ApiActivityLogEntity> newActivities = new HashSet<>();
    Set<ApiActivityLogBodyEntity> newLogActivities = new HashSet<>();
    for (ApiActivityLog dto : requestList) {
      Optional<ApiActivityLogEntity> db =
          repository.findByRequestIdAndCallSeq(dto.getRequestId(), dto.getCallSeq());
      if (db.isEmpty()) {
        ApiActivityLogEntity entity = ApiActivityLogMapper.INSTANCE.map(dto);
        entity.setEnv(envId);
        entity.setCreatedBy(userId);
        newActivities.add(entity);

        if (entity.getApiLogBodyEntity() != null) {
          newLogActivities.add(entity.getApiLogBodyEntity());
        }
      }
    }
    apiActivityLogBodyRepository.saveAll(newLogActivities);
    repository.saveAll(newActivities);
    return HttpResponse.ok(null);
  }

  public ApiActivityLogEntity save(ApiActivityLogEntity apiActivityLogEntity) {
    if (apiActivityLogEntity.getApiLogBodyEntity() != null) {
      this.apiActivityLogBodyRepository.save(apiActivityLogEntity.getApiLogBodyEntity());
    }
    return this.repository.save(apiActivityLogEntity);
  }

  public void setSynced(List<ApiActivityLogEntity> logEntities, ZonedDateTime now) {
    logEntities.forEach(
        logEntity -> {
          logEntity.setSyncStatus(SyncStatusEnum.SYNCED);
          logEntity.setSyncedAt(now);
        });
    apiActivityLogBodyRepository.saveAll(
        logEntities.stream().map(ApiActivityLogEntity::getApiLogBodyEntity).toList());
    repository.saveAll(logEntities);
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
