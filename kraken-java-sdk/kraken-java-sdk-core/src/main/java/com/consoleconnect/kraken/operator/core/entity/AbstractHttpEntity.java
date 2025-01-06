package com.consoleconnect.kraken.operator.core.entity;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.validation.constraints.Size;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;

@Getter
@Setter
@MappedSuperclass
public class AbstractHttpEntity extends AbstractEntity {

  @Size(max = 255)
  @Column(name = "env")
  private String env;

  @Column(name = "request_id", nullable = false, unique = false)
  private String requestId;

  @Column(name = "uri", nullable = false, unique = false)
  private String uri;

  @Column(name = "path", nullable = false, unique = false)
  private String path;

  @Column(name = "method", nullable = false, unique = false)
  private String method;

  @Column(name = "http_status_code", nullable = true, unique = false)
  private Integer httpStatusCode;

  @Column(name = "query_parameters", nullable = true, unique = false, columnDefinition = "jsonb")
  @Type(JsonType.class)
  private Map<String, String> queryParameters;

  @Column(name = "headers", nullable = true, unique = false, columnDefinition = "jsonb")
  @Type(JsonType.class)
  private Map<String, String> headers;

  @Column(name = "request", nullable = true, unique = false, columnDefinition = "jsonb")
  @Type(JsonType.class)
  protected Object request;

  @Column(name = "response", nullable = true, unique = false, columnDefinition = "jsonb")
  @Type(JsonType.class)
  protected Object response;
}
