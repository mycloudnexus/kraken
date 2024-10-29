package com.consoleconnect.kraken.operator.controller.model;

import com.consoleconnect.kraken.operator.core.model.AbstractModel;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SystemInfo extends AbstractModel {
  String controlProductVersion;
  String stageProductVersion;
  String productionProductVersion;
  String krakenAppVersion;
  String productKey;
  String productSpec;
  String key;
  String description;

  @Size(max = 255)
  String status;
}
