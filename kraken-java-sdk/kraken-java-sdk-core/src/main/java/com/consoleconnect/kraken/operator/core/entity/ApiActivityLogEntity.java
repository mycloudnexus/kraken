package com.consoleconnect.kraken.operator.core.entity;

import com.consoleconnect.kraken.operator.core.enums.LifeStatusEnum;
import com.consoleconnect.kraken.operator.core.enums.SyncStatusEnum;
import jakarta.persistence.*;
import java.time.ZonedDateTime;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(
    name = "kraken_api_log_activity",
    indexes = {
      @Index(name = "kraken_api_log_activity_idx_createAt", columnList = "created_at"),
      @Index(name = "kraken_api_log_activity_idx_request_id", columnList = "request_id"),
      @Index(name = "kraken_api_log_activity_idx_env", columnList = "env")
    },
    uniqueConstraints = {
      @UniqueConstraint(
          name = "kraken_api_log_activity_uni_idx",
          columnNames = {"request_id", "call_seq"})
    })
@SuppressWarnings("javaarchitecture:S7027")
public class ApiActivityLogEntity extends AbstractHttpEntity {

  @Column(name = "request_ip", nullable = true, unique = false)
  private String requestIp;

  @Column(name = "response_ip", nullable = true, unique = false)
  private String responseIp;

  @Column(name = "call_seq", nullable = true, unique = false)
  private Integer callSeq;

  @Column(name = "triggered_at", nullable = true, unique = false)
  private ZonedDateTime triggeredAt;

  @Column(name = "synced_at", nullable = true, unique = false)
  private ZonedDateTime syncedAt;

  @Enumerated(EnumType.STRING)
  @Column(name = "sync_status", nullable = true, unique = false)
  private SyncStatusEnum syncStatus;

  @Enumerated(EnumType.STRING)
  @Column(name = "life_status")
  private LifeStatusEnum lifeStatus;

  @Column(name = "buyer", nullable = true, unique = false)
  private String buyer;

  @OneToOne
  @JoinColumn(name = "api_log_body_id")
  private ApiActivityLogBodyEntity apiLogBodyEntity;

  public ApiActivityLogBodyEntity getRawApiLogBodyEntity() {
    return this.apiLogBodyEntity;
  }

  public void setRawApiLogBodyEntity(ApiActivityLogBodyEntity bodyEntity) {
    this.apiLogBodyEntity = bodyEntity;
  }

  public ApiActivityLogBodyEntity getApiLogBodyEntity() {
    if (this.apiLogBodyEntity == null) {
      this.apiLogBodyEntity = new ApiActivityLogBodyEntity();
      this.apiLogBodyEntity.setRequest(this.getRequest());
      this.apiLogBodyEntity.setResponse(this.getResponse());
    }
    return this.apiLogBodyEntity;
  }

  @Override
  public void setRequest(Object request) {
    var bodyEntity = this.getApiLogBodyEntity();
    bodyEntity.setRequest(request);
  }

  @Override
  public void setResponse(Object response) {
    var bodyEntity = this.getApiLogBodyEntity();
    bodyEntity.setResponse(response);
  }

  public Object getRawRequest() {
    return this.request;
  }

  public Object getRawResponse() {
    return this.response;
  }

  public void setRawRequest(Object request) {
    this.request = request;
  }

  public void setRawResponse(Object response) {
    this.response = response;
  }
}
