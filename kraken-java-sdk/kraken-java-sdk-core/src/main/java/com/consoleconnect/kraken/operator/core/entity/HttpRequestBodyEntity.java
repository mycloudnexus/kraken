package com.consoleconnect.kraken.operator.core.entity;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;

import java.util.Map;

@Entity
@Getter
@Setter
@Table(
        name = "kraken_api_log_activity_request_response"
        )
public class HttpRequestBodyEntity extends AbstractEntity {

  @OneToOne
  @JoinColumn(name = "log_id")
  private ApiActivityLogEntity log;

  @Column(name = "request", nullable = true, unique = false, columnDefinition = "jsonb")
  @Type(JsonType.class)
  private Object request;

  @Column(name = "response", nullable = true, unique = false, columnDefinition = "jsonb")
  @Type(JsonType.class)
  private Object response;
}
