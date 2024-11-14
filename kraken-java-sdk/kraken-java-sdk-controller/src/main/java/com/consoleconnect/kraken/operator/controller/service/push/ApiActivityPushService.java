package com.consoleconnect.kraken.operator.controller.service.push;

import com.consoleconnect.kraken.operator.controller.dto.push.ApiRequestActivityPushResult;
import com.consoleconnect.kraken.operator.controller.dto.push.CreatePushApiActivityRequest;
import com.consoleconnect.kraken.operator.controller.dto.push.PushApiActivityLogHistory;
import com.consoleconnect.kraken.operator.controller.model.Environment;
import com.consoleconnect.kraken.operator.controller.service.EnvironmentService;
import com.consoleconnect.kraken.operator.core.entity.MgmtEventEntity;
import com.consoleconnect.kraken.operator.core.enums.EventStatusType;
import com.consoleconnect.kraken.operator.core.enums.MgmtEventType;
import com.consoleconnect.kraken.operator.core.repo.MgmtEventRepository;
import com.consoleconnect.kraken.operator.core.request.PushLogSearchRequest;
import com.consoleconnect.kraken.operator.core.toolkit.JsonToolkit;
import com.consoleconnect.kraken.operator.core.toolkit.Paging;
import com.consoleconnect.kraken.operator.core.toolkit.PagingHelper;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class ApiActivityPushService {

  public static final String CREATED_AT = "createdAt";
  public static final String EVENT_TYPE = "eventType";

  private final MgmtEventRepository mgmtEventRepository;
  private final EnvironmentService environmentService;

  public ApiRequestActivityPushResult createPushApiActivityLogInfo(
      CreatePushApiActivityRequest request, String userId) {
    var environment = environmentService.findOne(request.getEnvId());
    var entity = new MgmtEventEntity();
    entity.setStatus(EventStatusType.ACK.name());
    entity.setEventType(MgmtEventType.PUSH_API_ACTIVITY_LOG.name());
    entity.setPayload(JsonToolkit.toJson(createData(request, userId, environment)));
    var saved = mgmtEventRepository.save(entity);
    return new ApiRequestActivityPushResult(saved.getId());
  }

  private PushLogActivityLogInfo createData(
      CreatePushApiActivityRequest request, String userId, Environment environment) {
    var data = new PushLogActivityLogInfo();
    data.setUser(userId);
    data.setEnvId(request.getEnvId());
    data.setEnvName(environment.getName());
    data.setStartTime(request.getStartTime());
    data.setEndTime(request.getEndTime());
    return data;
  }

  public Paging<PushApiActivityLogHistory> searchHistory(
      PushLogSearchRequest searchRequest, PageRequest pageRequest) {
    Page<MgmtEventEntity> pushEvents =
        mgmtEventRepository.findAll(getMgmtEventEntitySpecification(searchRequest), pageRequest);
    return PagingHelper.toPaging(
        pushEvents,
        e -> {
          var payload = JsonToolkit.fromJson(e.getPayload(), PushLogActivityLogInfo.class);
          return new PushApiActivityLogHistory(
              e.getId(),
              e.getCreatedAt(),
              payload.getEnvName(),
              payload.getStartTime(),
              payload.getEndTime(),
              payload.getUser(),
              e.getStatus());
        });
  }

  private static Specification<MgmtEventEntity> getMgmtEventEntitySpecification(
      PushLogSearchRequest searchRequest) {
    return (root, query, criteriaBuilder) -> {
      var predicateList = new ArrayList<Predicate>();
      predicateList.add(
          criteriaBuilder.equal(root.get(EVENT_TYPE), MgmtEventType.PUSH_API_ACTIVITY_LOG.name()));
      if (searchRequest.getQueryStart() != null) {
        predicateList.add(
            criteriaBuilder.greaterThanOrEqualTo(
                root.get(CREATED_AT), searchRequest.getQueryStart()));
      }
      if (searchRequest.getQueryEnd() != null) {
        predicateList.add(
            criteriaBuilder.lessThanOrEqualTo(root.get(CREATED_AT), searchRequest.getQueryEnd()));
      }
      return query.where(predicateList.toArray(new Predicate[0])).getRestriction();
    };
  }
}
