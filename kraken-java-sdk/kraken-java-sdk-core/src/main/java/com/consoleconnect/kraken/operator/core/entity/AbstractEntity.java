package com.consoleconnect.kraken.operator.core.entity;

import jakarta.persistence.*;
import java.time.ZonedDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

@Getter
@Setter
@MappedSuperclass
@EntityListeners({MyEntityListener.class})
public class AbstractEntity {
  @Id private UUID id;

  @Column(name = "created_by", nullable = true, unique = false)
  private String createdBy;

  @Column(name = "updated_by", nullable = true, unique = false)
  private String updatedBy;

  @Column(name = "deleted_by", nullable = true, unique = false)
  private String deletedBy;

  @Column(name = "created_at", nullable = true, unique = false)
  @CreatedDate
  private ZonedDateTime createdAt;

  @Column(name = "updated_at", nullable = true, unique = false)
  @LastModifiedDate
  private ZonedDateTime updatedAt;

  @Column(name = "deleted_at", nullable = true, unique = false)
  private ZonedDateTime deletedAt;
}
