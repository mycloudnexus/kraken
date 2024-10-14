package com.consoleconnect.kraken.operator.controller.dto;

import com.consoleconnect.kraken.operator.controller.entity.EnvironmentEntity;
import com.consoleconnect.kraken.operator.core.model.AbstractModel;
import java.util.List;
import lombok.Data;

/** DTO for {@link EnvironmentEntity} */
@Data
public class EnvironmentComponentDto extends AbstractModel {
  private String id;
  private String name;
  private String status;
  private List<ComponentVersionDto> components;
}
