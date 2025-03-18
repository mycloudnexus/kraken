package com.consoleconnect.kraken.operator.core.entity;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;

@Entity
@Getter
@Setter
@Table(name = "kraken_workflow_instance")
public class WorkflowInstanceEntity extends AbstractEntity {
  @Column(name = "workflow_instance_id", nullable = false)
  private String workflowInstanceId;

  @Column(name = "status", nullable = false)
  private String status;

  @Column(name = "error_msg", nullable = true)
  private String errorMsg;

  @Column(name = "request_id", nullable = false)
  private String requestId;

  @Column(name = "synced", nullable = false)
  private boolean synced;

  @Type(JsonType.class)
  @Column(name = "payload", columnDefinition = "jsonb")
  private Object payload;
}
