package com.consoleconnect.kraken.operator.controller.model;

import com.consoleconnect.kraken.operator.core.model.AbstractModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class Environment extends AbstractModel {
  private String name;
  private String description;
  private String status;
}
