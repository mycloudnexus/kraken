package com.consoleconnect.kraken.operator.controller.dto;

import com.consoleconnect.kraken.operator.controller.entity.EnvironmentEntity;
import java.io.Serializable;
import lombok.Data;

/** DTO for {@link EnvironmentEntity} */
@Data
public class EnvironmentDto implements Serializable {
  private String id;
  private String name;

  private String status;
}
