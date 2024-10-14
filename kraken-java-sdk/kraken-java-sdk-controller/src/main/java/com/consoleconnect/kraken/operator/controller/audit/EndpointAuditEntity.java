package com.consoleconnect.kraken.operator.controller.audit;

import com.consoleconnect.kraken.operator.core.entity.AbstractEntity;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.Type;

@Entity
@Getter
@Setter
@ToString
@Table(
    name = "kraken_audit_endpoint",
    indexes = {@Index(columnList = "resource_id, user_id", unique = false)})
public class EndpointAuditEntity extends AbstractEntity {

  @Column(name = "user_id", nullable = true)
  private String userId;

  @Column(name = "email")
  private String email;

  @Column(name = "name")
  private String name;

  @Column(name = "path", nullable = false)
  private String path;

  @Column(name = "method", nullable = false)
  private String method;

  @Type(JsonType.class)
  @Column(columnDefinition = "json", name = "path_variables", nullable = false)
  private Map<String, String> pathVariables;

  @Column(name = "action", nullable = false)
  private String action;

  @Column(name = "description", nullable = true)
  private String description;

  @Column(name = "resource", nullable = false)
  private String resource;

  @Column(name = "resource_id", nullable = true)
  private String resourceId;

  @Column(name = "remote_address", nullable = false)
  private String remoteAddress;

  @Column(name = "status_code", nullable = true)
  private Integer statusCode;

  @Type(JsonType.class)
  @Column(columnDefinition = "json", name = "ignore_request_params", nullable = true)
  private Set<String> ignoreRequestParams = new HashSet<>();

  @Transient private Object previousData;

  @Type(JsonType.class)
  @Column(columnDefinition = "json", name = "request", nullable = true)
  private Object request;

  @Type(JsonType.class)
  @Column(columnDefinition = "json", name = "response", nullable = true)
  private Object response;

  public enum Action {
    CREATE,
    UPDATE,
    DELETE,
    READ,
    UNKNOWN
  }
}
