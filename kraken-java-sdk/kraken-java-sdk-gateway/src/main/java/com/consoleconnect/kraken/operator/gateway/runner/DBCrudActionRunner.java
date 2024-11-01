package com.consoleconnect.kraken.operator.gateway.runner;

import com.consoleconnect.kraken.operator.core.enums.ActionTypeEnum;
import com.consoleconnect.kraken.operator.core.enums.DBActionTypeEnum;
import com.consoleconnect.kraken.operator.core.exception.KrakenException;
import com.consoleconnect.kraken.operator.core.model.AppProperty;
import com.consoleconnect.kraken.operator.core.model.facet.ComponentAPIFacets;
import com.consoleconnect.kraken.operator.core.toolkit.JsonToolkit;
import com.consoleconnect.kraken.operator.gateway.entity.HttpRequestEntity;
import com.consoleconnect.kraken.operator.gateway.filter.KrakenFilterConstants;
import com.consoleconnect.kraken.operator.gateway.repo.HttpRequestRepository;
import com.consoleconnect.kraken.operator.gateway.service.FilterHeaderService;
import java.util.*;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.server.ServerWebExchange;

@Service
@Slf4j
public class DBCrudActionRunner extends AbstractActionRunner {
  private static final String ENTITY_NOT_FOUND_ERR = "DBCrudFilterFactory: entity not found";
  private static final String ENTITY_ID_ERROR = ENTITY_NOT_FOUND_ERR + ", entityId:%s";
  private final HttpRequestRepository repository;
  private final FilterHeaderService filterHeaderService;

  public DBCrudActionRunner(
      AppProperty appProperty,
      HttpRequestRepository repository,
      FilterHeaderService filterHeaderService) {
    super(appProperty);
    this.repository = repository;
    this.filterHeaderService = filterHeaderService;
  }

  @Override
  public boolean canHandle(ComponentAPIFacets.Action action) {
    return ActionTypeEnum.DB == ActionTypeEnum.fromString(action.getActionType());
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
    log.info("Persist request,url:{},config{}", exchange.getRequest().getURI(), config);
    switch (config.getAction()) {
      case CREATE:
        onCreate(exchange, config);
        break;
      case UPDATE:
        this.onUpdate(exchange, config);
        break;
      case READ:
        this.onRead(exchange, config);
        break;
    }
  }

  public static HttpRequestEntity generateHttpRequestEntity(ServerWebExchange exchange) {
    HttpRequestEntity entity = new HttpRequestEntity();
    entity.setRequestId((String) exchange.getAttribute(KrakenFilterConstants.X_REQUEST_ID));
    entity.setPath(exchange.getRequest().getURI().getPath());
    entity.setMethod(exchange.getRequest().getMethod().name());
    entity.setUri(exchange.getRequest().getURI().getHost());
    return entity;
  }

  private void onCreate(ServerWebExchange exchange, Config config) {
    final HttpRequestEntity entity = generateHttpRequestEntity(exchange);
    entity.setBizType(config.getBizType());
    entity.setExternalId(config.getExternalId());

    if (config.getProperties() == null) {
      entity.setQueryParameters(exchange.getRequest().getQueryParams().toSingleValueMap());
      entity.setHeaders(
          filterHeaderService.filterHeaders(exchange.getRequest().getHeaders().toSingleValueMap()));
    } else {
      if (config.getProperties().contains("queryParameters")) {
        entity.setQueryParameters(exchange.getRequest().getQueryParams().toSingleValueMap());
      }
      if (config.getProperties().contains("headers")) {
        entity.setHeaders(
            filterHeaderService.filterHeaders(
                exchange.getRequest().getHeaders().toSingleValueMap()));
      }
      if (config.getProperties().contains("requestBody")) {
        readCachedData(KrakenFilterConstants.X_ORIGINAL_REQUEST_BODY, exchange)
            .ifPresent(entity::setRequest);
      }
      if (config.getProperties().contains("responseBody")) {
        readCachedData(KrakenFilterConstants.X_KRAKEN_RESPONSE_BODY, exchange)
            .ifPresent(entity::setRequest);
      }
    }
    Optional.ofNullable(exchange.getAttributes().get(KrakenFilterConstants.X_KRAKEN_BUYER_ID))
        .ifPresent(v -> entity.setBuyerId(v.toString()));
    HttpRequestEntity createdEntity = repository.save(entity);
    log.info("createdEntity:{}", createdEntity.getId());
    exchange
        .getAttributes()
        .put(KrakenFilterConstants.X_ENTITY_ID, createdEntity.getId().toString());

    exchange.getAttributes().put(KrakenFilterConstants.X_ENTITY, JsonToolkit.toJson(createdEntity));
  }

  private void onUpdate(ServerWebExchange exchange, Config config) {
    String entityId = exchange.getAttribute(KrakenFilterConstants.X_ENTITY_ID);
    Optional<HttpRequestEntity> optionalEntity = readRequestEntity(entityId);
    if (optionalEntity.isEmpty()) {
      return;
    }
    HttpRequestEntity updatedEntity = optionalEntity.get();
    if (config.getProperties() == null || config.getProperties().contains("requestBody")) {
      readCachedData(KrakenFilterConstants.X_ORIGINAL_REQUEST_BODY, exchange)
          .ifPresent(updatedEntity::setRequest);
    }
    if (config.getProperties() == null || config.getProperties().contains("responseBody")) {
      readCachedData(KrakenFilterConstants.X_KRAKEN_RESPONSE_BODY, exchange)
          .ifPresent(updatedEntity::setResponse);
    }

    if (config.getProperties() == null || config.getProperties().contains("responseStatusCode")) {
      updatedEntity.setHttpStatusCode(
          Objects.requireNonNull(exchange.getResponse().getStatusCode()).value());
    }
    readCachedData(KrakenFilterConstants.X_KRAKEN_RENDERED_RESPONSE_BODY, exchange)
        .ifPresent(updatedEntity::setRenderedResponse);
    repository.save(updatedEntity);
  }

  private void onRead(ServerWebExchange exchange, Config config) {
    if (StringUtils.isBlank(config.getId()) && StringUtils.isNotBlank(config.getBlankIdErrMsg())) {
      throw KrakenException.badRequest(config.getBlankIdErrMsg());
    }
    Optional<HttpRequestEntity> optionalEntity = readRequestEntity(config.getId());
    if (optionalEntity.isEmpty()) {
      log.error(ENTITY_NOT_FOUND_ERR);
      if (StringUtils.isNotBlank(config.getNotExistedErrMsg())) {
        throw KrakenException.notFound(config.getNotExistedErrMsg());
      } else {
        throw KrakenException.notFound(String.format(ENTITY_ID_ERROR, config.getId()));
      }
    }
    HttpRequestEntity entity = optionalEntity.get();
    exchange.getAttributes().put(KrakenFilterConstants.X_ENTITY_ID, entity.getId().toString());
    exchange.getAttributes().put(KrakenFilterConstants.X_ENTITY, JsonToolkit.toJson(entity));
  }

  private Optional<HttpRequestEntity> readRequestEntity(String entityId) {
    if (StringUtils.isBlank(entityId)) {
      return Optional.empty();
    }
    UUID entityUUID = null;
    try {
      entityUUID = UUID.fromString(entityId);
    } catch (Exception e) {
      String error = String.format(ENTITY_ID_ERROR, entityId);
      log.error(error, e);
      return Optional.empty();
    }
    return repository.findById(entityUUID);
  }

  @Data
  @Validated
  public static class Config {
    private DBActionTypeEnum action = DBActionTypeEnum.CREATE;
    private String bizType = "undefined";
    private String id;
    private List<String> properties;
    private String externalId;
    private String blankIdErrMsg;
    private String notExistedErrMsg;

    public static Config of(ComponentAPIFacets.Action action, Map<String, Object> inputs) {
      Map<String, Object> with = action.getWith();
      Config config = JsonToolkit.fromJson(JsonToolkit.toJson(with), Config.class);
      if (config.getId() == null) {
        config.setId((String) inputs.get("id"));
      }
      if ("undefined".equalsIgnoreCase(config.getBizType())) {
        config.setBizType((String) inputs.get("bizType"));
      }
      if (config.getBlankIdErrMsg() == null) {
        config.setBlankIdErrMsg((String) inputs.getOrDefault("blankIdErrMsg", ""));
      }
      if (config.getNotExistedErrMsg() == null) {
        config.setNotExistedErrMsg((String) inputs.getOrDefault("notExistedErrMsg", ""));
      }
      return config;
    }
  }
}
