package com.consoleconnect.kraken.operator.controller.model;

import com.consoleconnect.kraken.operator.controller.entity.EnvironmentClientEntity;
import com.consoleconnect.kraken.operator.core.model.AbstractModel;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** DTO for {@link EnvironmentClientEntity} */
@Data
public class EnvironmentClient extends AbstractModel {
  private String envId;
  private String clientIp;
  private String kind;

  @Size(max = 255)
  private String status;

  private String reason;
}
