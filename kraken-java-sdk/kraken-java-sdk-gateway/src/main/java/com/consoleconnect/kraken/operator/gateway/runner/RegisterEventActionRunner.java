package com.consoleconnect.kraken.operator.gateway.runner;

import com.consoleconnect.kraken.operator.core.enums.ActionTypeEnum;
import com.consoleconnect.kraken.operator.core.enums.RegisterActionTypeEnum;
import com.consoleconnect.kraken.operator.core.exception.KrakenException;
import com.consoleconnect.kraken.operator.core.model.AppProperty;
import com.consoleconnect.kraken.operator.core.model.facet.ComponentAPIFacets;
import com.consoleconnect.kraken.operator.core.toolkit.JsonToolkit;
import com.consoleconnect.kraken.operator.gateway.entity.EventEntity;
import com.consoleconnect.kraken.operator.gateway.filter.KrakenFilterConstants;
import com.consoleconnect.kraken.operator.gateway.model.RegisterState;
import com.consoleconnect.kraken.operator.gateway.repo.EventRepository;
import java.util.*;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.server.ServerWebExchange;

@Service
@Slf4j
public class RegisterEventActionRunner extends AbstractActionRunner {
  public static final String BUYER_ID = "buyer-id";
  private final EventRepository repository;

  public RegisterEventActionRunner(AppProperty appProperty, EventRepository repository) {
    super(appProperty);
    this.repository = repository;
  }

  @Override
  public boolean canHandle(ComponentAPIFacets.Action action) {
    return ActionTypeEnum.REGISTER_EVENT == ActionTypeEnum.fromString(action.getActionType());
  }

  @Override
  protected Optional<ServerWebExchange> runIt(
      ServerWebExchange exchange,
      ComponentAPIFacets.Action action,
      Map<String, Object> inputs,
      Map<String, Object> outputs) {
    Config config = Config.of(action, inputs);
    onPersist(exchange, config);
    return Optional.empty();
  }

  public void onPersist(ServerWebExchange exchange, Config config) {
    log.info("Persist event,url:{},config{}", exchange.getRequest().getURI(), config);
    switch (config.getAction()) {
      case REGISTER:
        onRegister(exchange, config);
        break;
      case UNREGISTER:
        this.onUnRegister(config);
        break;
      case READ:
        this.readEvent(exchange, config.getEventType(), BUYER_ID);
    }
  }

  public void onRegister(ServerWebExchange exchange, Config config) {
    List<EventEntity> eventList =
        search(config.getEventTypes(), config.getEventType(), BUYER_ID, RegisterState.ACTIVE);
    if (CollectionUtils.isNotEmpty(eventList)) {
      throw KrakenException.badRequest(
          "eventType: " + config.getEventTypes() + " is already registered!");
    }
    EventEntity entity = new EventEntity();
    entity.setBuyerId(BUYER_ID);
    entity.setState(RegisterState.ACTIVE);
    entity.setEventTypes(new HashSet<>(config.getEventTypes()));
    readCachedData(KrakenFilterConstants.X_ORIGINAL_REQUEST_BODY, exchange)
        .ifPresent(entity::setRegisterInfo);
    repository.save(entity);
    exchange
        .getAttributes()
        .put(KrakenFilterConstants.X_EVENT_ENTITY_ID, entity.getId().toString());
    exchange.getAttributes().put(KrakenFilterConstants.X_EVENT_ENTITY, JsonToolkit.toJson(entity));
  }

  public void onUnRegister(Config config) {
    EventEntity entity = new EventEntity();
    entity.setId(UUID.fromString(config.getId()));
    entity.setState(RegisterState.INACTIVE);
    repository.save(entity);
  }

  public void readEvent(ServerWebExchange exchange, String eventType, String buyerId) {
    List<EventEntity> eventList = search(null, eventType, buyerId, RegisterState.ACTIVE);
    if (CollectionUtils.isEmpty(eventList)) {
      throw KrakenException.notFound("event:" + eventType + "not found!");
    }
    exchange
        .getAttributes()
        .put(KrakenFilterConstants.X_EVENT_ENTITY_ID, eventList.get(0).getId().toString());
    exchange
        .getAttributes()
        .put(KrakenFilterConstants.X_EVENT_ENTITY, JsonToolkit.toJson(eventList.get(0)));
  }

  public List<EventEntity> search(
      List<String> eventTypeList, String eventTypes, String buyerId, RegisterState state) {
    final List<String> typeList =
        CollectionUtils.isEmpty(eventTypeList) ? new ArrayList<>() : new ArrayList<>(eventTypeList);
    typeList.add(eventTypes);
    List<EventEntity> eventList = repository.findEventEntitiesByBuyerIdAndState(buyerId, state);
    eventList.removeIf(event -> Collections.disjoint(event.getEventTypes(), typeList));
    return eventList;
  }

  @Data
  @Validated
  public static class Config {
    private RegisterActionTypeEnum action = RegisterActionTypeEnum.REGISTER;
    private String bizType = "undefined";
    private String id;
    private List<String> eventTypes;
    private String eventType;

    public static Config of(ComponentAPIFacets.Action action, Map<String, Object> inputs) {
      Map<String, Object> with = action.getWith();
      Config config = JsonToolkit.fromJson(JsonToolkit.toJson(with), Config.class);
      if (config.getId() == null) {
        config.setId((String) inputs.get("id"));
      }
      List<String> eventTypes = (List<String>) inputs.get("eventTypes");
      config.setEventTypes(eventTypes);
      config.setEventType((String) inputs.get("eventType"));
      return config;
    }
  }
}
