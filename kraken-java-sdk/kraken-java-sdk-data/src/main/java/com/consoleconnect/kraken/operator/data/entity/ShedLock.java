package com.consoleconnect.kraken.operator.data.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.ZonedDateTime;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "kraken_shed_lock")
@Getter
@Setter
public class ShedLock {
  @Id
  @Column(name = "name", length = 64, nullable = false)
  private String name;

  @Column(name = "lock_until", nullable = false)
  private ZonedDateTime lockUntil;

  @Column(name = "locked_at", nullable = false)
  private ZonedDateTime lockedAt;

  @Column(name = "locked_by", length = 255, nullable = false)
  private String lockedBy;
}
