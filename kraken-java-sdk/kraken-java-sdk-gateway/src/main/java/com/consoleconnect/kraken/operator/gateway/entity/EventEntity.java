package com.consoleconnect.kraken.operator.gateway.entity;

import com.consoleconnect.kraken.operator.core.entity.AbstractEntity;
import com.consoleconnect.kraken.operator.gateway.model.RegisterState;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;

@Entity
@Getter
@Setter
@Table(name = "kraken_event_entity")
public class EventEntity extends AbstractEntity {
  @Column(name = "buyer_id", nullable = false)
  private String buyerId;

  @Type(JsonType.class)
  @Column(name = "register_info", columnDefinition = "json")
  private Object registerInfo;

  @Type(JsonType.class)
  @Column(name = "event_types", columnDefinition = "json")
  private Set<String> eventTypes = new HashSet<>();

  @Enumerated(EnumType.STRING)
  private RegisterState state;

  @Column(name = "notification_url")
  private String notificationUrl;
}
