package com.consoleconnect.kraken.operator.core.entity;

import com.consoleconnect.kraken.operator.core.toolkit.DateTime;
import jakarta.persistence.PostRemove;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import java.time.ZonedDateTime;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MyEntityListener {

  @PrePersist
  public void onCreate(AbstractEntity entity) {
    if (entity.getCreatedAt() == null) {
      ZonedDateTime current = DateTime.nowInUTC();
      entity.setCreatedAt(current);
    }
  }

  @PreUpdate
  public void onUpdate(AbstractEntity entity) {
    ZonedDateTime current = DateTime.nowInUTC();
    entity.setUpdatedAt(current);
    if (entity.getCreatedAt() == null) {
      entity.setCreatedAt(current);
    }
  }

  @PostRemove
  public void onRemove(AbstractEntity entity) {
    log.info("onRemove,entityId:{},class={}", entity.getId(), entity.getClass().getName());
  }
}
