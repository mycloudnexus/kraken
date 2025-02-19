package com.consoleconnect.kraken.operator.gateway.entity;

import com.consoleconnect.kraken.operator.core.entity.AbstractHttpEntity;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;

@Entity
@Getter
@Setter
@Table(
    name = "kraken_http_request_entity",
    uniqueConstraints = {
      @UniqueConstraint(
          name = "kraken_request_entity_uni_idx",
          columnNames = {"request_id", "call_seq"})
    })
public class HttpRequestEntity extends AbstractHttpEntity {

  @Column(name = "biz_type", nullable = false, unique = false)
  private String bizType;

  @Column(name = "external_id", nullable = true, unique = false)
  private String externalId;

  @Column(name = "product_instance_id", nullable = true, unique = false)
  private String productInstanceId;

  @Column(name = "buyer_id", nullable = true, unique = false)
  private String buyerId;

  @Column(name = "rendered_response", nullable = true, unique = false, columnDefinition = "jsonb")
  @Type(JsonType.class)
  private Object renderedResponse;
}
