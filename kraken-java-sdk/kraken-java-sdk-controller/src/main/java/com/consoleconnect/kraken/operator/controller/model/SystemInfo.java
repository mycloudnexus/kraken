package com.consoleconnect.kraken.operator.controller.model;

import com.consoleconnect.kraken.operator.core.model.AbstractModel;
import lombok.Data;

@Data
public class SystemInfo extends AbstractModel {
  String controlProductVersion;
  String stageProductVersion;
  String productionProductVersion;
  String controlAppVersion;
  String buildRevision;
  String productKey;
  String productSpec;
  String key;
  String description;
  String status;
  String productionAppVersion;
  String stageAppVersion;
  String productName;
}
