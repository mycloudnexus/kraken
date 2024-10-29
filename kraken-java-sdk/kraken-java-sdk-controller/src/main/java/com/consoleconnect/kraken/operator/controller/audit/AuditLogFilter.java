package com.consoleconnect.kraken.operator.controller.audit;

import static com.consoleconnect.kraken.operator.core.toolkit.AuditConstants.*;
import static com.consoleconnect.kraken.operator.core.toolkit.DataBufferUtil.convert2String;
import static org.springframework.core.io.buffer.DataBufferUtils.join;

import com.consoleconnect.kraken.operator.auth.model.AuthDataProperty;
import com.consoleconnect.kraken.operator.auth.repo.UserRepository;
import com.consoleconnect.kraken.operator.core.annotation.AuditAction;
import com.consoleconnect.kraken.operator.core.annotation.Auditable;
import com.consoleconnect.kraken.operator.core.toolkit.*;
import com.fasterxml.jackson.core.type.TypeReference;
import jakarta.validation.constraints.NotNull;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.StandardCharsets;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.reactive.result.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import org.springframework.web.util.UriTemplate;
import org.springframework.web.util.pattern.PathPattern;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class AuditLogFilter implements WebFilter, Ordered {

  private final RequestMappingHandlerMapping handlerMapping;
  private final EndpointAuditRepository repository;
  private final UserRepository userRepository;

  public AuditLogFilter(
      EndpointAuditRepository repository,
      @Qualifier("requestMappingHandlerMapping") RequestMappingHandlerMapping handlerMapping,
      UserRepository userRepository) {
    this.repository = repository;
    this.handlerMapping = handlerMapping;
    this.userRepository = userRepository;
  }

  private Optional<EndpointAuditEntity> generateAuditPayload(
      ServerWebExchange exchange, HandlerMethod handlerMethod) {
    final Class<?> cls = handlerMethod.getBean().getClass();
    if (!cls.isAnnotationPresent(Auditable.class)
        || !handlerMethod.hasMethodAnnotation(AuditAction.class)) {
      return Optional.empty();
    }

    EndpointAuditEntity auditEntity = this.initAuditPayload(exchange);

    log.info(
        "generateAuditPayload:{} {} by {}",
        auditEntity.getMethod(),
        auditEntity.getPath(),
        auditEntity.getUserId());

    final Auditable auditable = cls.getAnnotation(Auditable.class);
    final AuditAction auditAction = handlerMethod.getMethodAnnotation(AuditAction.class);

    // cache annotation
    exchange.getAttributes().put(AUDIT_ANNOTATION_KEY, auditAction);

    constructAuditEntity(auditEntity, auditable, auditAction);

    return Optional.of(this.renderPayload(auditEntity));
  }

  private void constructAuditEntity(
      EndpointAuditEntity auditEntity, Auditable auditable, AuditAction auditAction) {
    auditEntity.setResource(auditable.resource());
    auditEntity.setResourceId(auditable.resourceId());

    if (auditAction != null) {
      if (!auditAction.resource().isEmpty()) {
        auditEntity.setResource(auditAction.resource());
      }
      auditEntity.setResourceId(auditAction.resourceId());
      if (!auditAction.action().isEmpty()) {
        auditEntity.setAction(auditAction.action());
      } else {
        auditEntity.setAction(method2Action(auditEntity.getMethod()));
      }
      if (!auditAction.description().isEmpty()) {
        auditEntity.setDescription(auditAction.description());
      }
      if (auditAction.ignoreRequestParams() != null
          && auditAction.ignoreRequestParams().length > 0) {
        auditEntity.setIgnoreRequestParams(
            new HashSet<>(Arrays.asList(auditAction.ignoreRequestParams())));
      }
    }
  }

  private Boolean checkCondition(EndpointAuditEntity entity, ServerWebExchange exchange) {
    try {
      AuditAction auditAction = (AuditAction) exchange.getAttributes().get(AUDIT_ANNOTATION_KEY);
      return (Boolean) parseExpression(entity, exchange, auditAction.conditionOn(), Boolean.class);
    } catch (Exception e) {
      log.error("parse condition error", e);
      return Boolean.FALSE;
    }
  }

  private Object parseExpression(
      EndpointAuditEntity entity, ServerWebExchange exchange, String expression, Class<?> clazz) {
    SpelExpressionParser parser = new SpelExpressionParser();
    StandardEvaluationContext context = new StandardEvaluationContext();
    context.setVariables(
        Map.of(
            "requestBody",
            entity.getRequest() == null ? Collections.emptyMap() : entity.getRequest()));
    context.setVariables(
        Map.of(
            "responseBody",
            entity.getResponse() == null ? Collections.emptyMap() : entity.getResponse()));

    context.setVariables(
        Map.of("requestParam", exchange.getRequest().getQueryParams().toSingleValueMap()));
    context.setVariables(
        Map.of(
            "pathVariable",
            entity.getPathVariables() == null
                ? Collections.emptyMap()
                : entity.getPathVariables()));
    return parser.parseExpression(expression).getValue(context, clazz);
  }

  public static String method2Action(String method) {
    return switch (method) {
      case "GET" -> EndpointAuditEntity.Action.READ.name();
      case "POST" -> EndpointAuditEntity.Action.CREATE.name();
      case "PATCH", "PUT" -> EndpointAuditEntity.Action.UPDATE.name();
      case "DELETE" -> EndpointAuditEntity.Action.DELETE.name();
      default -> EndpointAuditEntity.Action.UNKNOWN.name();
    };
  }

  private EndpointAuditEntity renderPayload(EndpointAuditEntity auditPayload) {

    Map<String, String> context = auditPayload.getPathVariables();
    if (auditPayload.getResourceId().startsWith(VAR_PREFIX)) {
      String key = auditPayload.getResourceId().substring(VAR_PREFIX.length());
      String value = context.get(key);
      if (value != null) {
        auditPayload.setResourceId(value);
      }
    }
    return auditPayload;
  }

  private void parseRequestBody(
      DataBuffer dataBuffer, Optional<EndpointAuditEntity> endpointAudit) {
    try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        WritableByteChannel writableByteChannel = Channels.newChannel(byteArrayOutputStream)) {
      byte[] bytes = new byte[dataBuffer.readableByteCount()];
      ByteBuffer byteBuffer = ByteBuffer.wrap(bytes, 0, bytes.length);
      dataBuffer.toByteBuffer(byteBuffer);

      writableByteChannel.write(byteBuffer);
      endpointAudit.ifPresent(
          entity ->
              entity.setRequest(
                  JsonToolkit.fromJson(
                      new String(byteArrayOutputStream.toByteArray(), StandardCharsets.UTF_8),
                      Object.class)));
    } catch (IOException e) {
      log.error("get user error:", e);
    }
  }

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
    HandlerMethod handlerMethod =
        (HandlerMethod) this.handlerMapping.getHandler(exchange).toFuture().getNow(new Object());
    if (handlerMethod == null) {
      return chain.filter(exchange);
    }
    Optional<EndpointAuditEntity> endpointAuditEntity =
        generateAuditPayload(exchange, handlerMethod);
    if (endpointAuditEntity.isPresent()) {
      EndpointAuditEntity entity = endpointAuditEntity.get();
      if (exchange.getRequest().getMethod().name().equalsIgnoreCase(HttpMethod.GET.name())) {
        checkConditionAndSave(entity, exchange);
      }
      ServerHttpRequestDecorator requestMutated =
          getServerHttpRequestDecorator(exchange, endpointAuditEntity, entity);
      ServerHttpResponseDecorator responseMutated = getServerHttpResponseDecorator(exchange, chain);
      ServerWebExchange serverWebExchange =
          exchange.mutate().request(requestMutated).response(responseMutated).build();
      return chain.filter(serverWebExchange);
    }
    return chain.filter(exchange);
  }

  @NotNull
  private ServerHttpRequestDecorator getServerHttpRequestDecorator(
      ServerWebExchange exchange,
      Optional<EndpointAuditEntity> endpointAuditEntity,
      EndpointAuditEntity entity) {
    return new ServerHttpRequestDecorator(exchange.getRequest()) {
      @Override
      public Flux<DataBuffer> getBody() {
        return Flux.from(join(super.getBody()))
            .doOnNext(
                dataBuffer -> {
                  parseRequestBody(dataBuffer, endpointAuditEntity);
                  checkConditionAndSave(entity, exchange);
                });
      }
    };
  }

  @NotNull
  private ServerHttpResponseDecorator getServerHttpResponseDecorator(
      ServerWebExchange exchange, WebFilterChain chain) {
    return new ServerHttpResponseDecorator(exchange.getResponse()) {
      @Override
      public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
        String id = (String) exchange.getAttributes().get(AUDIT_KEY);
        AuditAction auditAction = (AuditAction) exchange.getAttributes().get(AUDIT_ANNOTATION_KEY);
        if (StringUtils.isBlank(id)) {
          log.error("can not find id in exchange attributes");
          return exchange.getResponse().writeWith(join(body));
        }
        if (auditAction == null || auditAction.ignoreResponse()) {
          return exchange.getResponse().writeWith(join(body));
        }
        return join(body)
            .flatMap(
                db -> {
                  updateResponse(id, db, exchange);
                  return this.getDelegate().writeWith(Mono.just(db));
                });
      }
    };
  }

  private void updateResponse(String id, DataBuffer db, ServerWebExchange exchange) {
    try {
      Optional<EndpointAuditEntity> endpointAudit = repository.findById(UUID.fromString(id));
      if (endpointAudit.isPresent()) {
        EndpointAuditEntity auditPayload = endpointAudit.get();
        String responseStr = convert2String(db, exchange);
        Object responseJson = JsonToolkit.fromJson(responseStr, new TypeReference<>() {});
        auditPayload.setResponse(responseJson);
        auditPayload.setStatusCode(exchange.getResponse().getStatusCode().value());
        parseResourceId(auditPayload, exchange).ifPresent(auditPayload::setResourceId);
        repository.save(auditPayload);
      }
    } catch (Exception e) {
      log.error("afterCompletion error： ", e);
    }
  }

  private void checkConditionAndSave(EndpointAuditEntity entity, ServerWebExchange exchange) {
    if (Boolean.FALSE.equals(checkCondition(entity, exchange))) {
      log.info("condition not be satisfied!");
    } else {
      parseResourceId(entity, exchange).ifPresent(entity::setResourceId);
      EndpointAuditEntity result = repository.save(entity);
      exchange.getAttributes().put(AUDIT_KEY, result.getId().toString());
    }
  }

  private Optional<String> parseResourceId(
      EndpointAuditEntity endpointAudit, ServerWebExchange exchange) {
    try {
      AuditAction auditAction = (AuditAction) exchange.getAttributes().get(AUDIT_ANNOTATION_KEY);
      if (auditAction == null) {
        return Optional.empty();
      }
      return Optional.ofNullable(
          (String)
              parseExpression(endpointAudit, exchange, auditAction.resourceId(), String.class));
    } catch (Exception e) {
      log.error("parseResourceId error:{}", e.getMessage());
    }
    return Optional.empty();
  }

  private EndpointAuditEntity initAuditPayload(ServerWebExchange exchange) {
    EndpointAuditEntity auditEntity = new EndpointAuditEntity();
    ServerHttpRequest request = exchange.getRequest();
    try {
      String userId =
          (String) exchange.getAttributes().get(new AuthDataProperty.ResourceServer().getUserId());
      if (StringUtils.isNotBlank(userId)) {
        auditEntity.setUserId(userId);
        userRepository
            .findById(UUID.fromString(userId))
            .ifPresent(
                user -> {
                  auditEntity.setEmail(user.getEmail());
                  auditEntity.setName(user.getName());
                });
      }
    } catch (Exception e) {
      log.error("query user error", e);
    }
    auditEntity.setMethod(request.getMethod().name());
    auditEntity.setPath(request.getURI().getPath());
    auditEntity.setPathVariables(parsePathVariables(exchange));
    auditEntity.setRemoteAddress(IpUtils.getIP(request));
    auditEntity.setCreatedBy(auditEntity.getUserId());
    auditEntity.setCreatedAt(DateTime.nowInUTC());
    return auditEntity;
  }

  private Map<String, String> parsePathVariables(ServerWebExchange exchange) {
    PathPattern pattern =
        (PathPattern)
            exchange.getAttribute(
                "org.springframework.web.reactive.HandlerMapping.bestMatchingPattern");
    if (pattern == null) {
      return Collections.emptyMap();
    }
    String patternString = pattern.getPatternString();
    UriTemplate template = new UriTemplate(patternString);
    return template.match(exchange.getRequest().getURI().getPath());
  }

  @Override
  public int getOrder() {
    return Ordered.LOWEST_PRECEDENCE;
  }
}
